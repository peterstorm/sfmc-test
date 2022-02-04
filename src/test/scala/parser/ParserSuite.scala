package parser

import parser.MailParser
import domain.Attributes._
import cats.parse.Parser
import cats.kernel.Eq
import munit.CatsEffectSuite
import cats.effect.IO
import io.circe.syntax._

class ParserSuite extends CatsEffectSuite:

  val correctInput = """
    {
      FIRST_NAME¤Foo A/S;
      BALANCE¤-104,00;

      CONDITION_MORE_THAN_ONE_SUB¤true;
      USERNAME¤202009231634210996@GDPR.FOO.DK;
      PAYMENT_LINK¤https://www.foo.dk/payment/?paymentLinkId=202111081306432489
    }
    """

  val wrongDelimiter = """
    {
      FIRST_NAME¤Foo A/S,
      BALANCE¤-104,00,

      CONDITION_MORE_THAN_ONE_SUB¤true,
      USERNAME¤202009231634210996@GDPR.FOO.DK,
      PAYMENT_LINK¤https://www.foo.dk/payment/?paymentLinkId=202111081306432489
    }
    """

  val wrongSeparator = """
    {
      FIRST_NAME=Foo A/S;
      BALANCE=-104,00;

      CONDITION_MORE_THAN_ONE_SUB=true;
      USERNAME=202009231634210996@GDPR.FOO.DK;
      PAYMENT_LINK=https://www.foo.dk/payment/?paymentLinkId=202111081306432489
    }
    """

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

  def parseTest[A: Eq](p: Parser[A], input: String, a: A) =
    p.parse(input) match
      case Right((_, res)) =>
        assert(Eq[A].eqv(a, res), s"expected: $a got $res")
      case Left(errs) =>
        assert(false, errs.toString)

  def parseFail[A](p: Parser[A], input: String) =
    p.parse(input) match
      case Right(res) =>
        assert(false, s"expected to not parse, but found: $res")
      case Left(_) =>
        assert(true)

  test("print out ADT") {
    IO.println(MailParser.parseFile.parse(correctInput))
  }

  test("parser works real world example") {
    parseTest(MailParser.parseFile, correctInput, adtOutput)
  }

  test("comma delimiter fails") {
    parseFail(MailParser.parseFile, wrongDelimiter)
  }

  test("equals separator fails") {
    parseFail(MailParser.parseFile, wrongSeparator)
  }
