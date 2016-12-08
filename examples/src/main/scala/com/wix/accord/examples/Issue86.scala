package com.wix.accord.examples

import scala.util.Try

/**
  * Created by tomerga on 18/11/2016.
  */
object Issue86 extends App {

  import com.wix.accord.dsl._
  import com.wix.accord._

  case class GuestDetail(paxNumber: String)
  case class GuestBooking(guestDetails: Seq[GuestDetail])


  val validForBookingNumber =
    validator[GuestDetail] { gd => gd.paxNumber is notEmpty }

  implicit val guestBookingValidator =
    validator[GuestBooking] { p: GuestBooking => p.guestDetails.each is validForBookingNumber }


  val gb = GuestBooking(Seq(GuestDetail("")))
  validate(gb)
}
