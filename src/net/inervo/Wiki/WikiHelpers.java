package net.inervo.Wiki;

/*
 * Copyright (c) 2011, Ted Timmons, Inervo Networks All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * Inervo Networks nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class WikiHelpers {
	public static String calendarToTimestamp( Calendar c ) {
		return String.format( "%04d%02d%02d%02d%02d%02d", c.get( Calendar.YEAR ), c.get( Calendar.MONTH ) + 1, c.get( Calendar.DAY_OF_MONTH ),
				c.get( Calendar.HOUR_OF_DAY ), c.get( Calendar.MINUTE ), c.get( Calendar.SECOND ) );
	}

	public static String calendarToDatestamp( Calendar c ) {
		return String.format( "%04d%02d%02d", c.get( Calendar.YEAR ), c.get( Calendar.MONTH ) + 1, c.get( Calendar.DAY_OF_MONTH ) );
	}

	public static Calendar timestampToCalendar( String timestamp ) {
		GregorianCalendar calendar = new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) );
		int year = Integer.parseInt( timestamp.substring( 0, 4 ) );
		int month = Integer.parseInt( timestamp.substring( 4, 6 ) ) - 1; // January == 0!
		int day = Integer.parseInt( timestamp.substring( 6, 8 ) );
		int hour = Integer.parseInt( timestamp.substring( 8, 10 ) );
		int minute = Integer.parseInt( timestamp.substring( 10, 12 ) );
		int second = Integer.parseInt( timestamp.substring( 12, 14 ) );
		calendar.set( year, month, day, hour, minute, second );
		return calendar;
	}
}
