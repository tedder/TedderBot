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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.Configuration;
import net.inervo.Wiki.PageEditor;
import net.inervo.Wiki.RetryEditor;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.Cache.ArticleCache;
import net.inervo.Wiki.Cache.CachedFetcher;

public class Application {
	private static final boolean DEBUG_MODE = false;

	public static void main( String[] args ) throws Exception {
		// shutdown hook. Enable early so we don't blow up the cache.
		System.setProperty( "net.sf.ehcache.enableShutdownHook", "true" );

		print( "hello world!" );
		ArticleCache ac = null;

		try {
			PersistentKeystore.initialize( new File( "AwsCredentials.properties" ), "TedderBot.NewPageSearch" );
			Configuration config = new Configuration( new File( "wiki.properties" ) );

			WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
			wiki.setMaxLag( 15 );
			wiki.setLogLevel( Level.WARNING );

			// wiki.setThrottle( 5000 );
			wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
			print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

			String debugOverride = DEBUG_MODE ? "Oregon" : null;

			ac = new ArticleCache( wiki );
			WikiFetcher fetcher = new CachedFetcher( ac );

			PageRules rules = new PageRules( fetcher, "User:AlexNewArtBot/Master", debugOverride );

			PageEditor editor = new RetryEditor( wiki );

			String lastProcessed = PersistentKeystore.get( PersistentKeystore.LAST_PROCESSED );
			print( "last processed: " + lastProcessed );
			lastProcessed = "Oxford";

			List<PageRule> ruleList = rules.getRules();
			Comparator<PageRule> sorter = new SortRulesByRuleNameAlpha();
			if ( lastProcessed != null ) {
				sorter = new SortRulesByRuleNameAlphaOnPivot( lastProcessed );
			}
			Collections.sort( ruleList, sorter );

			NewPageFetcher npp = new NewPageFetcher( wiki, fetcher, editor, DEBUG_MODE );

			for ( PageRule rule : ruleList ) {
				print( "rule: " + rule.getSearchName() );
				// store it before we run. That way we'll begin at n+1 even if this one frequently fails.
				PersistentKeystore.put( PersistentKeystore.LAST_PROCESSED, rule.getSearchName(), true );

				npp.run( rule );
			}

		} finally {
			if ( ac != null ) {
				ac.shutdown();
			}
		}
	}

	public static class SortRulesByRuleNameAlphaOnPivot implements Comparator<PageRule> {
		private String pivot;

		public SortRulesByRuleNameAlphaOnPivot( String pivot ) {
			this.pivot = pivot.toLowerCase();
		};

		@Override
		public int compare( PageRule arg0, PageRule arg1 ) {
			int zeroComp = pivot.compareTo( arg0.getSearchName().toLowerCase() );
			int oneComp = pivot.compareTo( arg1.getSearchName().toLowerCase() );
			int directComp = arg0.getSearchName().toLowerCase().compareTo( arg1.getSearchName().toLowerCase() );

			if ( zeroComp * oneComp <= 0 ) {
				return Math.abs( directComp );
			}

			return directComp;
		}
	}

	public static class SortRulesByRuleNameAlpha implements Comparator<PageRule> {
		public SortRulesByRuleNameAlpha() {
		};

		@Override
		public int compare( PageRule arg0, PageRule arg1 ) {
			return arg0.getSearchName().compareTo( arg1.getSearchName() );
		}
	}

	private static void print( String s ) {
		System.out.println( s );
	}

}
