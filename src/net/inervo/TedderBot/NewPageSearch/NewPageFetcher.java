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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.inervo.WMFWiki11;
import net.inervo.WMFWiki11.Revisions;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.WikiHelpers;
import net.inervo.data.Keystore;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class NewPageFetcher {
	// consider this an "oversearch" period.
	public static final int PREPEND_SEARCH_DAYS = -7;

	protected static final String ORPHAN_BUCKET_NAME = "Orphan";

	protected WMFWiki11 wiki = null;
	protected Map<String, String> results = new HashMap<String, String>();
	protected WikiFetcher fetcher = null;
	protected boolean debug;
	protected HashMap<String, ArrayList<String>> outputList = new HashMap<String, ArrayList<String>>();
	protected HashMap<String, SortedMap<Integer, Integer>> outputBySearchByDay = new HashMap<String, SortedMap<Integer, Integer>>();
	protected Keystore keystore = null;

	public NewPageFetcher( WMFWiki11 wiki, WikiFetcher fetcher, Keystore keystore, boolean debug ) {
		this.wiki = wiki;
		this.fetcher = fetcher;
		this.keystore = keystore;
		this.debug = debug;
	}

	public String getStartTime() throws FileNotFoundException, IllegalArgumentException, IOException {
		String startTime = keystore.getKey( "lastRunTime" );

		Calendar start = null;
		if ( startTime == null || startTime.isEmpty() ) {
			// if we didn't have the key in our keystore, use a default of today minus our padding.
			start = new GregorianCalendar();
		} else {
			start = WikiHelpers.timestampToCalendar( startTime );
		}

		// pad back the start time.
		start.add( Calendar.DAY_OF_MONTH, PREPEND_SEARCH_DAYS );

		// given our Calendar object, get a String (again).
		startTime = WikiHelpers.calendarToTimestamp( start );

		return startTime;
	}

	public void run() throws Exception {
		String endTime = runFetcher( getStartTime() );
		storeStartTime( endTime );
	}

	public void storeStartTime( String lastStamp ) {
		keystore.put( "lastRunTime", lastStamp, true );
	}

	public String runFetcher( String startTimestamp ) throws Exception {
		String debugOverride = debug ? "Oregon" : null;
		String lastTimestamp = null;

		PageRules rules = new PageRules( wiki, "User:AlexNewArtBot/Master", debugOverride );

		Revisions revs = null;
		// String start = calendarToTimestamp( new GregorianCalendar( 2011, 04, 01, 0, 01, 03 ) );
		do {
			revs = fetch( 5000, startTimestamp );
			lastTimestamp = processRevisions( rules, revs );

			startTimestamp = revs.getRcStart();
			print( "rcstart: " + startTimestamp );
		} while ( startTimestamp != null && startTimestamp.length() > 0 );

		outputResults( rules );

		return lastTimestamp;
	}

	protected void addEntryToOutputLists( Revision rev, String searchName, int score ) {
		String text = getResultOutputLine( rev, score );

		// main outputlist
		if ( !outputList.containsKey( searchName ) ) {
			ArrayList<String> textlist = new ArrayList<String>();
			textlist.add( text );
			outputList.put( searchName, textlist );
		} else {
			outputList.get( searchName ).add( text );
		}

		// by search by day list
		Integer datestamp = Integer.valueOf( WikiHelpers.calendarToDatestamp( rev.getTimestamp() ) );
		if ( !outputBySearchByDay.containsKey( searchName ) ) {
			SortedMap<Integer, Integer> daymap = new TreeMap<Integer, Integer>();
			daymap.put( datestamp, 1 );
			outputBySearchByDay.put( searchName, daymap );
		} else {
			Integer count = outputBySearchByDay.get( searchName ).get( datestamp );
			if ( count == null ) {
				count = 0;
			}
			++count;
			outputBySearchByDay.get( searchName ).put( datestamp, count );
		}
	}

	protected String getResultOutputLine( Revision rev, int score ) {
		SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
		return "*{{la|" + rev.getPage() + "}} by {{User|" + rev.getUser() + "}} started at <span class=\"mw-newpages-time\">"
				+ sdf.format( rev.getTimestamp().getTime() ) + "</span>, score: " + score;

	}

	protected void outputResults( PageRules rules ) throws Exception {
		writeErrors( rules );
		for ( PageRule rule : rules.getRules() ) {
			int searchErrorCount = 0;
			if ( rule.getErrors() != null ) {
				searchErrorCount = rule.getErrors().size();
			}
			outputResultsForRule( rule.getSearchName(), rule.getPageName(), rule.getTarget(), searchErrorCount );
		}
	}

	protected void outputResultsForRule( String searchName, String pageName, String target, int searchErrorCount ) throws Exception {
		if ( debug && !searchName.equalsIgnoreCase( "oregon" ) ) {
			return;
		}

		StringBuilder searchResultText = new StringBuilder();
		StringBuilder subject = new StringBuilder( "most recent results" );

		if ( searchErrorCount > 0 ) {
			subject.append( ", " + searchErrorCount + " [[User:TedderBot/SearchBotErrors|errors]]" );
			searchResultText.append( "'''There were [[User:TedderBot/SearchBotErrors#" + searchName + "|" + searchErrorCount
					+ " encountered]] while parsing the [[" + pageName + "|" + "rules for this search]].''' " );
		}

		if ( outputList.containsKey( ORPHAN_BUCKET_NAME ) && outputList.get( ORPHAN_BUCKET_NAME ) != null ) {
			searchResultText.append( "There were also " + outputList.get( ORPHAN_BUCKET_NAME ).size()
					+ " new articles that weren't matched by any lists during this time period. " );
		}

		searchResultText.append( "This list was generated from [[" + pageName + "]]. Questions and feedback [[User talk:Tedder|are always welcome]]!\n\n" );

		if ( outputList.containsKey( searchName ) ) {
			ArrayList<String> results = outputList.get( searchName );
			subject.append( ", " + results.size() + " results" );
			for ( String line : results ) {
				searchResultText.append( line );
				searchResultText.append( "\n" );
			}
		} else {
			searchResultText.append( "There are no current results for this search, sorry." );
		}

		subject.append( ", daily counts: " + getSparkline( searchName ) );

		// int nums[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		// for (int i:nums) {
		// String s = String.format ("\\u%04x", 9600+c)
		// }

		wiki.edit( target, searchResultText.toString(), subject.toString(), false );
	}

	protected String getSparkline( String searchName ) {
		SortedMap<Integer, Integer> resultCounts = outputBySearchByDay.get( searchName );
		List<Double> numbers = new ArrayList<Double>();

		for ( Integer value : resultCounts.values() ) {
			numbers.add( (double) value );
		}

		return new Sparkline().getSparkline( numbers );
	}

	protected void writeErrors( PageRules rules ) {
		int rulecount = rules.getRules().size();

		StringBuilder errorBuilder = new StringBuilder( rulecount + " searches processed.\n" );
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

			boolean matched = false;
			for ( PageRule rule : rules.getRules() ) {

				int score = new ArticleScorer( fetcher, rule, article ).score( pageText );
				// print( "Article: " + article + ", score: " + score + ", search: " + rule.getSearchName() );

				if ( score >= rule.getThreshold() ) {
					print( "score is above threshold! Article: " + article + ", score: " + score + ", search: " + rule.getSearchName() + ", time: "
							+ WikiHelpers.calendarToTimestamp( rev.getTimestamp() ) );
					matched = true;
					addEntryToOutputLists( rev, rule.getSearchName(), score );
				}

			}

			if ( !matched ) {
				addEntryToOutputLists( rev, ORPHAN_BUCKET_NAME, 1 );
			}

			lastRevisionTime = WikiHelpers.calendarToTimestamp( rev.getTimestamp() );
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
