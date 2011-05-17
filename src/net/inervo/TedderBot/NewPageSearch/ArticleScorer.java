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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.Configuration;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.Cache.ArticleCache;
import net.inervo.Wiki.Cache.CachedFetcher;

public class ArticleScorer {
	private PageRule ruleset;
	private String article;
	private WikiFetcher fetcher;

	public ArticleScorer( WikiFetcher fetcher, PageRule ruleset, String article ) {
		this.ruleset = ruleset;
		this.article = article;
		this.fetcher = fetcher;
	}

	protected String fetch() throws Exception {
		return fetcher.getPageText( article );
	}

	public int score() throws Exception {
		return score( fetch() );
	}

	// Use a cached copy of the page.
	public int score( String articleText ) {
		int score = 0;

		for ( PageRule.MatchRule rule : ruleset.getPatterns() ) {
			// print( "pattern: " + rule.getPattern().toString() );
			score += scoreRule( articleText, rule );
		}

		return score;
	}

	protected int scoreRule( String articleText, PageRule.MatchRule rule ) {
		boolean matchedArticle = ruleMatches( articleText, rule.getPattern() );
		boolean foundIgnore = false;

		if ( rule.getIgnore() != null && rule.getIgnore().size() > 0 ) {
			for ( Pattern pattern : rule.getIgnore() ) {
				if ( ruleMatches( articleText, pattern ) ) {
					foundIgnore = true;
					break;
				}
			}
		}

		int score = 0;
		if ( matchedArticle && !foundIgnore ) {
			score = rule.getScore();

			String ledeText = getLede( articleText );
			if ( ruleMatches( ledeText, rule.getPattern() ) ) {
				score *= 2;
			}
		}

		return score;
	}

	protected String getLede( String articleText ) {
		// lede is at start
		// there's an infobox first

		// Pattern ledeText = Pattern.compile("[^\\=]");
		Pattern ledePattern = Pattern.compile( "^(.*?)([\\n\\r]{2}|==).*", Pattern.MULTILINE | Pattern.DOTALL );
		Matcher ledeMatcher = ledePattern.matcher( articleText );

		String ledeText = "";
		if ( ledeMatcher.matches() ) {
			ledeText = ledeMatcher.group( 1 );

		}

		return ledeText;
	}

	protected boolean ruleMatches( String articleText, Pattern pattern ) {
		boolean found = false;

		Matcher matcher = pattern.matcher( articleText );

		if ( matcher.find() ) {
			// print("matcher matches.");
			found = true;
		}

		return found;
	}

	public static void main( String[] args ) throws Exception {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 25 );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		PageRule parser = new PageRule( wiki, "User:AlexNewArtBot/Oregon", "Oregon" );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

		ArticleCache ac = new ArticleCache( wiki );
		// WikiFetcher fetcher = new CachedFetcher(ac);
		WikiFetcher fetcher = new CachedFetcher( ac );

		ArticleScorer scorer = new ArticleScorer( fetcher, parser, "Joseph Gramley" );
		print( "lede: " + scorer.getLede( fetcher.getPageText( "Joseph Gramley" ) ) + "(end lede)" );
		// print( "score: " + scorer.score( "This is a test with Portland, Oregon mentioned." ) );
		// print( "score: " + scorer.score() );

		ac.shutdown();

	}

	protected static void print( String s ) {
		System.out.println( s );
	}

}
