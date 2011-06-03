package net.inervo.TedderBot.NewPageSearch.test;

import java.util.logging.Level;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.Configuration;
import net.inervo.TedderBot.NewPageSearch.PageRules;
import net.inervo.Wiki.BasicFetcher;
import net.inervo.Wiki.WikiFetcher;

public class TestPageRules {

	@SuppressWarnings( "unused" )
	public static void main( String[] args ) throws Exception {
		Configuration config = new Configuration( "wiki.properties" );
		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 15 );
		wiki.setLogLevel( Level.WARNING );
		// wiki.setThrottle( 5000 );
		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );

		WikiFetcher wf = new BasicFetcher( wiki );
		PageRules pr = new PageRules( wf, "User:AlexNewArtBot/Master", "Conservatism" );
		// pr.getRules().get( 0 )
	}
}
