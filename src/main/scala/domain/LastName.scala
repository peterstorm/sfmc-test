package domain

import io.circe._
import io.circe.syntax._

opaque type LastName = String

object LastName:

  def apply(v: String): LastName = v

  given Decoder[LastName] =
    Decoder
      .instance(c => c.downField("lastName").as[LastName](using Decoder.decodeString))


  given Encoder[LastName] =
    Encoder
      .instance(v => Json.obj("lastName" -> Json.fromString(v)))
