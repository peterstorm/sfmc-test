package interpreters.repos

import cats.effect.MonadCancel
import cats.syntax.all._
import capabilities.database.Database
import domain.auth._
import algebras.repos.AuthRepo
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.syntax.all._
import doobie.util.Read
import org.typelevel.log4cats.Logger

class AuthRepoInterpreter[F[_]: Logger](
  using F: MonadCancel[F, Throwable], DB: Database[F]
) extends AuthRepo[F]:

  import AuthRepoSQL._
  val xa = Database[F].transactor

  def getClientCredentials: F[ClientCredentials] =
    val action =
      for
        id <- getParamString[ClientId]("SALESFORCE.CLIENT_ID").unique
        secret <- getParamString[ClientSecret]("SALESFORCE.CLIENT_SECRET").unique
      yield ClientCredentials(id, secret)
    Logger[F].info("getClientCredentials ran") >> action.transact(xa)

  def getTestSettings: F[String] =
    getParamString[String]("SALESFORCE.IS_TEST").unique.transact(xa)

  def persistAccessToken(token: AccessToken): F[Unit] =
    setParamStr("SALESFORCE", "ACCESS_TOKEN", token.access_token).run.transact(xa).void

  def retrieveAccessToken: F[AccessToken] =
    getParamString[AccessToken]("SALESFORCE.ACCESS_TOKEN").unique.transact(xa)


object AuthRepoInterpreter:

  def apply[F[_]: Logger](
    using F: MonadCancel[F, Throwable], DB: Database[F]
  ): AuthRepo[F] =
    new AuthRepoInterpreter

object AuthRepoSQL:

  def getParamString[A: Read](key: String): Query0[A] = 
    sql"""
    SELECT CORE_PARAMETER.GET_STRING($key) PARAMETER_VALUE_STR FROM DUAL
    """.query[A]

  def setParamStr(group: String, key: String, value: String): Update0 = 
    sql"""
    UPDATE PARAMETER_VALUE PV
    SET PARAMETER_VALUE_STR = $value
    WHERE PARENT_ID = (SELECT ID FROM
      (SELECT ID, SYS_CONNECT_BY_PATH(name, '.') AS PATH FROM PARAMETER_TREE PT
        START WITH PARENT_ID = 0
        CONNECT BY PRIOR ID = PARENT_ID)
      WHERE PATH = '.' || UPPER($group))
    AND PV.NAME = $key
    """.update
