package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.inervo.TedderBot.Configuration;

import org.wikipedia.WMFWiki;
import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class PageFetcher {

	public static void main( String[] args ) throws Exception {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki wiki = new WMFWiki( "en.wikipedia.org" );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

		Revision[] revs = wiki.recentChangesFFF( 5, Wiki.MAIN_NAMESPACE, 0, false, new GregorianCalendar( 2011, 04, 01, 0, 1 ) );
		for ( Revision rev : revs ) {
			print( rev.getPage() + " " + calendarToTimestamp( rev.getTimestamp() ) );
		}

		// parser.parseRule( );
		// print( wiki.getPageText( "User:AlexNewArtBot/LosAngeles" ) );
	}

	private static void print( String s ) {
		System.out.println( s );
	}

	private static String calendarToTimestamp( Calendar c ) {
		StringBuilder x = new StringBuilder( 16 );
		x.append( c.get( Calendar.YEAR ) );
		int i = c.get( Calendar.MONTH ) + 1; // January == 0!
		if ( i < 10 )
			x.append( "0" ); // add a zero if required
		x.append( i );
		i = c.get( Calendar.DATE );
		if ( i < 10 )
			x.append( "0" );
		x.append( i );
		i = c.get( Calendar.HOUR_OF_DAY );
		if ( i < 10 )
			x.append( "0" );
		x.append( i );
		i = c.get( Calendar.MINUTE );
		if ( i < 10 )
			x.append( "0" );
		x.append( i );
		i = c.get( Calendar.SECOND );
		if ( i < 10 )
			x.append( "0" );
		x.append( i );
		return x.toString();
	}
}
