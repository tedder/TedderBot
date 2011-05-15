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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.inervo.WMFWiki11;
import net.inervo.WMFWiki11.Revisions;
import net.inervo.WikiHelpers;
import net.inervo.Wiki.WikiFetcher;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class NewPageFetcher {

	protected WMFWiki11 wiki = null;
	protected Map<String, String> results = new HashMap<String, String>();
	protected WikiFetcher fetcher = null;
	protected boolean debug;

	public NewPageFetcher( WMFWiki11 wiki, WikiFetcher fetcher, boolean debug ) {
		this.wiki = wiki;
		this.fetcher = fetcher;
		this.debug = debug;
	}

	public String run( String startTimestamp ) throws Exception {
		String debugOverride = debug ? "Oregon" : null;
		String lastTimestamp = null;

		PageRules rules = new PageRules( wiki, "User:AlexNewArtBot/Master", debugOverride );

		Revisions revs = null;
//		String start = calendarToTimestamp( new GregorianCalendar( 2011, 04, 01, 0, 01, 03 ) );
		do {
			revs = fetch( 5000, startTimestamp );
			lastTimestamp = processRevisions( rules, revs );
			
			startTimestamp = revs.getRcStart();
			print( "rcstart: " + startTimestamp );
		} while ( startTimestamp != null && startTimestamp.length() > 0 );
		
		outputResults( rules );
		
		return lastTimestamp;
	}

	HashMap<String, ArrayList<String>> alist = new HashMap<String, ArrayList<String>>();

	protected void addEntryToOutputList( Revision rev, PageRule rule, int score ) {
		String text = getResultOutputLine( rev, score );
		String sn = rule.getSearchName();

		if ( !alist.containsKey( sn ) ) {
			ArrayList<String> textlist = new ArrayList<String>();
			textlist.add( text );
			alist.put( sn, textlist );
		} else {
			alist.get( sn ).add( text );
		}
	}

	protected String getResultOutputLine( Revision rev, int score ) {
		SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
		return "*{{la|" + rev.getPage() + "}} by {{User|" + rev.getUser() + "}} started at <span class=\"mw-newpages-time\">"
				+ sdf.format( rev.getTimestamp().getTime() ) + "</span>, score: " + score + "</span>";

	}

	protected void outputResults( PageRules rules ) throws Exception {
		for ( PageRule rule : rules.getRules() ) {
			outputResultsForRule( rule );
		}
	}

	protected void outputResultsForRule( PageRule rule ) throws Exception {
		print( "pn: " + rule.getPageName() );
		print( "sn: " + rule.getSearchName() );
		print( "t:  " + rule.getTarget() );

		int errors = 0;
		if ( rule.getErrors() != null ) {
			errors = rule.getErrors().size();
		}

		// String pageName = rule.getPageName();
		String searchName = rule.getSearchName();
		if ( debug && !searchName.equalsIgnoreCase( "oregon" ) ) {
			return;
		}

		StringBuilder searchResultText = new StringBuilder();
		StringBuilder subject = new StringBuilder( "most recent results" );

		if ( errors > 0 ) {
			subject.append( ", " + errors + " [[User:TedderBot/SearchBotErrors|errors]]" );
			searchResultText.append( "'''There were [[User:TedderBot/SearchBotErrors#" + rule.getSearchName() + "|" + errors
					+ " encountered]] while parsing the [[" + rule.getPageName() + "|" + "rules for this search]].'''\n\n" );
		}

		if ( alist.containsKey( searchName ) ) {
			ArrayList<String> results = alist.get( searchName );
			subject.append( ": " + results.size() + " results" );
			for ( String line : results ) {
				searchResultText.append( line );
				searchResultText.append( "\n" );
			}
		} else {
			print( "search " + searchName + " had no results" );
			return;
		}

		wiki.edit( "User:TedderBot/TestSearchResults", searchResultText.toString(), subject.toString(), false );
	}

	protected void writeErrors( PageRules rules ) {
		StringBuilder errorBuilder = new StringBuilder();
		errorBuilder.append( "{| class=\"wikitable\"\n! search\n! errors\n" );

		for ( PageRule rule : rules.getRules() ) {
			if ( rule.getErrors().size() == 0 ) {
				continue;
			}
			String link = "[[" + rule.getPageName() + "|" + rule.getSearchName() + "]]";
			errorBuilder.append( "|-\n" );
			errorBuilder.append( "| " );
			errorBuilder.append( link );
			errorBuilder.append( "\n| <nowiki>" );
			errorBuilder.append( join( "</nowiki><br />\n<nowiki>", rule.getErrors().toArray( new String[rule.getErrors().size()] ) ) );
			errorBuilder.append( "</nowiki>\n" );
		}
		errorBuilder.append( "\n|}\n" );

		// print( "text: " + errorBuilder.toString() );

		try {
			wiki.edit( "User:TedderBot/SearchBotErrors", errorBuilder.toString(), "most recent errors", false );
		} catch ( Exception e ) {
			// do nothing, we don't really care if the log fails.
		}
	}

	protected String processRevisions( PageRules rules, Revisions revs ) throws Exception {
		String lastRevisionTime = null;
		
		for ( Revision rev : revs.getRevisionList() ) {

			String article = rev.getPage();
			String pageText = null;
			try {
				pageText = fetcher.getPageText( article );
			} catch ( IOException ex ) {
				// couldn't get the page, move on.
				continue;
			}

			for ( PageRule rule : rules.getRules() ) {

				int score = new ArticleScorer( fetcher, rule, article ).score( pageText );
				// print( "Article: " + article + ", score: " + score + ", search: " + rule.getSearchName() );

				if ( score >= rule.getThreshold() ) {
					print( "score is above threshold! Article: " + article + ", score: " + score + ", search: " + rule.getSearchName() );
					addEntryToOutputList( rev, rule, score );
				}

			}
			
			lastRevisionTime = WikiHelpers.calendarToTimestamp(rev.getTimestamp());
		}
		
		return lastRevisionTime;
	}

	public Revisions fetch( int fetchPageCount, String rcstart ) throws Exception {
		return wiki.newPages( fetchPageCount, Wiki.MAIN_NAMESPACE, 0, rcstart );
	}

	/*** helper functions ***/

	protected static void print( String s ) {
		System.out.println( s );
	}

	protected static String join( String delim, String... arr ) {
		StringBuilder ret = new StringBuilder();
		for ( String row : arr ) {
			if ( ret.length() != 0 ) {
				ret.append( delim );
			}
			ret.append( row );
		}

		return ret.toString();
	}
}
