/*
  Copyright 2013-2016 Wix.com

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.wix.accord.java8

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal._
import java.time.{Duration, LocalDateTime}

/**
  * Converted wholesale from http://stackoverflow.com/a/28131899/11558
  */
import TemporalDuration._
class TemporalDuration( val duration: Duration ) extends TemporalAccessor {
  private val temporal: Temporal = duration.addTo( baseTemporal )

  override def isSupported( field: TemporalField ): Boolean =
    temporal.isSupported( field ) &&
    ( temporal.getLong( field ) - baseTemporal.getLong( field ) ) != 0L

  override def getLong( field: TemporalField ): Long = {
    if ( !isSupported( field ) )
      throw new UnsupportedTemporalTypeException( field.toString )
    temporal.getLong( field ) - baseTemporal.getLong( field )
  }

  override def toString: String = TemporalDuration.formatter.format( this )
}

object TemporalDuration {
  val baseTemporal: Temporal = LocalDateTime.of( 0, 1, 1, 0, 0 )

  val formatter: DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .optionalStart //second
      .optionalStart //minute
      .optionalStart //hour
      .optionalStart //day
      .optionalStart //month
      .optionalStart //year
      .appendValue( ChronoField.YEAR ).appendLiteral( " Years " ).optionalEnd
      .appendValue( ChronoField.MONTH_OF_YEAR ).appendLiteral( " Months " ).optionalEnd
      .appendValue( ChronoField.DAY_OF_MONTH ).appendLiteral( " Days " ).optionalEnd
      .appendValue( ChronoField.HOUR_OF_DAY ).appendLiteral( " Hours " ).optionalEnd
      .appendValue( ChronoField.MINUTE_OF_HOUR ).appendLiteral(" Minutes " ).optionalEnd
      .appendValue( ChronoField.SECOND_OF_MINUTE ).appendLiteral(" Seconds" ).optionalEnd
      .toFormatter
}
