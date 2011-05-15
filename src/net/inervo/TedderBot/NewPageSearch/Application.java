package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import net.inervo.WMFWiki11;
import net.inervo.WikiHelpers;
import net.inervo.TedderBot.Configuration;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.Cache.ArticleCache;
import net.inervo.Wiki.Cache.CachedFetcher;
import net.inervo.data.Keystore;

public class Application {
	// SELECT min(rc_timestamp),count(rc_id),page_title FROM page AS p,recentchanges AS rc WHERE rc.rc_cur_id=p.page_id
	// AND page_id=31367574 LIMIT 5\G

	public static void main( String[] args ) throws Exception {
		print( "hello world!" );

		Keystore keystore = new Keystore( new File( "AwsCredentials.properties" ), "TedderBot.NewPageSearch" );
		String startTime = keystore.getKey( "lastRunTime" );

		if ( startTime == null || startTime.isEmpty() ) {
			startTime = WikiHelpers.calendarToTimestamp( new GregorianCalendar( 2011, 04, 01, 0, 01, 03 ) );
		}

		print( "start time: " + startTime );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 15 );
		wiki.setLogLevel( Level.WARNING );

		// wiki.setThrottle( 5000 );
		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

		ArticleCache ac = new ArticleCache( wiki );
		// WikiFetcher fetcher = new CachedFetcher(ac);
		WikiFetcher fetcher = new CachedFetcher( ac );

		NewPageFetcher npp = new NewPageFetcher( wiki, fetcher, true );
		String lastStamp = npp.run( startTime );

		keystore.put( "lastRunTime", lastStamp, true );

		ac.shutdown();
	}

	private static void print( String s ) {
		System.out.println( s );
	}

}
