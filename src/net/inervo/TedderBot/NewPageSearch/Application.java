package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.io.IOException;

import javax.security.auth.login.FailedLoginException;

import net.inervo.TedderBot.Configuration;

import org.wikipedia.WMFWiki;
import org.wikipedia.Wiki;

public class Application {
	// SELECT min(rc_timestamp),count(rc_id),page_title FROM page AS p,recentchanges AS rc WHERE rc.rc_cur_id=p.page_id AND page_id=31367574 LIMIT 5\G

	/**
	 * @param args
	 * @throws IOException
	 * @throws FailedLoginException
	 */
	public static void main( String[] args ) throws FailedLoginException, IOException {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		Wiki wiki;
		wiki = new WMFWiki( "en.wikipedia.org" );
		// wiki.setThrottle( 5000 );
		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );
		print( wiki.getPageText( "Special:NewPages" ) );
	}

	private static void print( String s ) {
		System.out.println( s );
	}

}
