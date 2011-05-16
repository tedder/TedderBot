package net.inervo.TedderBot.NewPageSearch;

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

import java.io.File;
import java.util.logging.Level;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.Configuration;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.Cache.ArticleCache;
import net.inervo.Wiki.Cache.CachedFetcher;
import net.inervo.data.Keystore;

public class Application {
	private static final boolean DEBUG_MODE = false;

	public static void main( String[] args ) throws Exception {
		// shutdown hook. Enable early so we don't blow up the cache.
		System.setProperty( "net.sf.ehcache.enableShutdownHook", "true" );

		print( "hello world!" );
		ArticleCache ac = null;

		try {
			Keystore keystore = new Keystore( new File( "AwsCredentials.properties" ), "TedderBot.NewPageSearch" );
			Configuration config = new Configuration( new File( "wiki.properties" ) );

			WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
			wiki.setMaxLag( 15 );
			wiki.setLogLevel( Level.WARNING );

			// wiki.setThrottle( 5000 );
			wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
			print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

			ac = new ArticleCache( wiki );
			WikiFetcher fetcher = new CachedFetcher( ac );

			NewPageFetcher npp = new NewPageFetcher( wiki, fetcher, keystore, DEBUG_MODE );
			npp.run();

		} finally {
			if ( ac != null ) {
				ac.shutdown();
			}
		}
	}

	private static void print( String s ) {
		System.out.println( s );
	}

}
