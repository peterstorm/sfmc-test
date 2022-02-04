package domain

import domain.Attributes._
import munit.CatsEffectSuite
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import io.circe.parser._

class AttributesSuite extends CatsEffectSuite:

  val adtOutput =
    AMap(
      Map(
        "FIRST_NAME" -> AString("Foo A/S"),
        "PAYMENT_LINK" -> AString("https://www.foo.dk/payment/?paymentLinkId=202111081306432489"),
        "USERNAME" -> AString("202009231634210996@GDPR.FOO.DK"),
        "BALANCE" -> AString("-104,00"),
        "CONDITION_MORE_THAN_ONE_SUB" -> AString("true")
      )
    )

  val json = """
  {
    "FIRST_NAME" : "Foo A/S",
    "PAYMENT_LINK" : "https://www.foo.dk/payment/?paymentLinkId=202111081306432489",
    "USERNAME" : "202009231634210996@GDPR.FOO.DK",
    "BALANCE" : "-104,00",
    "CONDITION_MORE_THAN_ONE_SUB" : "true"
  }
  """

  val invalidJson = """
  {
    "FIRST_NAME" : "Foo A/S"
    "PAYMENT_LINK" : "https://www.foo.dk/payment/?paymentLinkId=202111081306432489",
    "USERNAME" : "202009231634210996@GDPR.FOO.DK",
    "BALANCE" : "-104,00",
    "CONDITION_MORE_THAN_ONE_SUB" : "true"
  }
  """

  def parseJson(input: String, json: Json) =
    parse(input) match
      case Right(res) =>
        assertEquals(res, json)
      case Left(errs) =>
        assert(false, errs.toString)

  test("print out json of ADT") {
    IO.println(adtOutput.asJson.toString)
  }

  test("encoder works") {
    parseJson(json, adtOutput.asJson)
  }

  test("fails on invalid json".fail) {
    parseJson(invalidJson, adtOutput.asJson)
  }
