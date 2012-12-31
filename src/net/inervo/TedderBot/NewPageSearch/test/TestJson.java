package net.inervo.TedderBot.NewPageSearch.test;

import java.util.logging.Level;

import net.inervo.WMFWiki11;
import net.inervo.WMFWiki11RevisionText;
import net.inervo.TedderBot.Configuration;

public class TestJson {

	/**
	 * @param args
	 */

	public static void main( String[] args ) throws Exception
	{
		Configuration config = new Configuration( "wiki.properties" );
		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 15 );
		wiki.setLogLevel( Level.WARNING );
		// wiki.setThrottle( 5000 );
		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		WMFWiki11RevisionText rev = wiki.getTopRevision( "Oregon" );

		System.out.println( "revid: " + rev.getRevid() );
		System.out.println( "summary: ||" + rev.getSummary() + "||" );
		System.out.println( "content length: ||" + rev.getContent().length() + "||" );
	}

}
