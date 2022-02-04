package domain

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.syntax.given
import io.circe.Encoder
import io.circe.Json
import cats.implicits._
import cats.kernel.Eq

enum Attributes:

  case AList(list: List[Attributes])

  case AMap(map: Map[String, Attributes])

  case AString(s: String)

object Attributes:

 given Encoder[Attributes] =
   Encoder.instance {
     case list @ Attributes.AList(l) =>
       Json.fromString(l.asJson.noSpaces)

     case map @ Attributes.AMap(m) =>
       m.asJson

     case string @ Attributes.AString(s) =>
       Json.fromString(s)
   }

 given Eq[Attributes] = Eq.fromUniversalEquals
