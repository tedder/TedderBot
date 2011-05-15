package net.inervo.Wiki;

import java.util.Calendar;

public abstract class WikiHelpers {
	public static String calendarToTimestamp( Calendar c ) {
		return String.format( "%04d%02d%02d%02d%02d%02d", c.get( Calendar.YEAR ), c.get( Calendar.MONTH ) + 1, c.get( Calendar.DAY_OF_MONTH ),
				c.get( Calendar.HOUR_OF_DAY ), c.get( Calendar.MINUTE ), c.get( Calendar.SECOND ) );
	}
}
