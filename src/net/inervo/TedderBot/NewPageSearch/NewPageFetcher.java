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

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.inervo.WMFWiki11;
import net.inervo.WMFWiki11.Revisions;
import net.inervo.Wiki.PageEditor;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.WikiHelpers;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class NewPageFetcher {
	// consider this an "oversearch" period.
	public static final int PREPEND_SEARCH_DAYS = -7;

	protected WMFWiki11 wiki = null;
	protected WikiFetcher fetcher = null;
	protected PageEditor editor = null;

	// protected List<String> errors = new ArrayList<String>();

	/**
	 * Requires PersistentKeystore to be properly initialized.
	 * 
	 * @param wiki
	 * @param fetcher
	 * @param editor
	 * @param rule
	 * @param debug
	 */
	public NewPageFetcher( WMFWiki11 wiki, WikiFetcher fetcher, PageEditor editor ) {
		this.wiki = wiki;
		this.fetcher = fetcher;
		this.editor = editor;
	}

	public String getStartTime( String searchName ) throws FileNotFoundException, IllegalArgumentException, IOException {
		String startTime = PersistentKeystore.get( searchName, "lastRunTime" );

		if ( startTime == null || startTime.isEmpty() ) {
			startTime = getDefaultStartTime();
		}

		return startTime;
	}

	public void storeStartTime( String searchName, String lastStamp ) {
		PersistentKeystore.put( searchName, "lastRunTime", lastStamp, true );
	}

	public String getDefaultStartTime() throws FileNotFoundException, IllegalArgumentException, IOException {
		String startTime = PersistentKeystore.get( "default", "lastRunTime" );

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

	public void run( PageRule rule ) throws Exception {
		String endTime = runFetcher( getStartTime( rule.getSearchResultPage() ), rule );
		storeStartTime( rule.getSearchResultPage(), endTime );
	}

	public String runFetcher( String startTimestamp, PageRule rule ) throws Exception {
		// String debugOverride = debug ? "Oregon" : null;
		String lastTimestamp = null;

		Revisions revs = null;

		ArrayList<String> outputList = new ArrayList<String>();
		SortedMap<Integer, Integer> outputByDay = new TreeMap<Integer, Integer>();

		// String start = calendarToTimestamp( new GregorianCalendar( 2011, 04, 01, 0, 01, 03 ) );
		do {
			print( "about to start fetch of stamp: " + startTimestamp );
			revs = fetch( 5000, startTimestamp );
			print( "done with fetch of stamp: " + startTimestamp );
			lastTimestamp = processRevisions( rule, revs, outputList, outputByDay );

			startTimestamp = revs.getRcStart();
			print( "rcstart: " + startTimestamp );
		} while ( startTimestamp != null && startTimestamp.length() > 0 );

		int errorCount = writeRuleErrors( rule );
		outputResultsForRule( rule, errorCount, outputList, outputByDay );

		return lastTimestamp;
	}

	protected void addEntryToOutputLists( Revision rev, String searchName, int score, List<String> outputList, SortedMap<Integer, Integer> outputByDay ) {
		String text = getResultOutputLine( rev, score );

		// main outputlist
		outputList.add( text );

		// by search by day list
		Integer datestamp = Integer.valueOf( WikiHelpers.calendarToDatestamp( rev.getTimestamp() ) );

		if ( !outputByDay.containsKey( datestamp ) ) {
			outputByDay.put( datestamp, 1 );
		} else {
			Integer count = outputByDay.get( datestamp );
			if ( count == null ) {
				count = 0;
			}
			++count;
			outputByDay.put( datestamp, count );
		}
	}

	protected String getResultOutputLine( Revision rev, int score ) {
		SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
		return "*{{la|" + rev.getPage() + "}} by {{User|" + rev.getUser() + "}} started at <span class=\"mw-newpages-time\">"
				+ sdf.format( rev.getTimestamp().getTime() ) + "</span>, score: " + score;
	}

	protected void outputResultsForRule( PageRule rule, int searchErrorCount, List<String> results, SortedMap<Integer, Integer> outputByDay ) throws Exception {

		StringBuilder searchResultText = new StringBuilder();
		StringBuilder subject = new StringBuilder( "most recent results" );

		if ( searchErrorCount > 0 ) {
			subject.append( ", " + searchErrorCount + " [[User:TedderBot/SearchBotErrors|errors]]" );

			String errorLabel = searchErrorCount == 0 ? "error" : "errors";

			searchResultText.append( "'''There were [[" + rule.getErrorPage() + "|" + searchErrorCount + " " + errorLabel
					+ " encountered]] while parsing the [[" + rule.getRulePage() + "|" + "rules for this search]].''' " );
		}

		searchResultText.append( "This list was generated from [[" + rule.getRulePage()
				+ "|these rules]]. Questions and feedback [[User talk:Tedder|are always welcome]]! "
				+ "The search is being run manually, but eventually will run ~daily with the most ~7 days of results.\n\n" );

		if ( results.size() > 0 ) {
			Collections.reverse( results );

			subject.append( ", " + results.size() + " results" );
			for ( String line : results ) {
				searchResultText.append( line );
				searchResultText.append( "\n" );
			}
		} else {
			searchResultText.append( "There are no current results for this search, sorry." );
		}

		subject.append( ", daily counts: " + getSparkline( rule.getSearchName(), outputByDay ) );

		try {
			editor.edit( rule.getSearchResultPage(), searchResultText.toString(), subject.toString(), false );
		} catch ( IOException ex ) {
			print( "failed updating " + rule.getSearchResultPage() );
		}
	}

	protected String getSparkline( String searchName, SortedMap<Integer, Integer> resultCounts ) {
		List<Double> numbers = new ArrayList<Double>();

		if ( resultCounts == null || resultCounts.values() == null ) {
			return "";
		}
		for ( Integer value : resultCounts.values() ) {
			numbers.add( (double) value );
		}

		return new Sparkline().getSparkline( numbers );
	}

	protected int writeRuleErrors( PageRule rule ) {
		int patterncount = rule.getPatterns().size();

		StringBuilder errorBuilder = new StringBuilder( patterncount + " search patterns processed for this rule.\n\n" );
		errorBuilder.append( "==Errors==\n" );

		int errorcount = 0;

		if ( rule.getErrors() != null && rule.getErrors().size() > 0 ) {
			errorcount = rule.getErrors().size();

			errorBuilder.append( "<pre>" );
			errorBuilder.append( join( "\n", rule.getErrors().toArray( new String[rule.getErrors().size()] ) ) );
			errorBuilder.append( "</pre>\n" );

		} else {
			errorBuilder.append( "There were no errors. Hooray!\n" );
		}

		try {
			editor.edit( rule.getErrorPage(), errorBuilder.toString(), "most recent errors", false );
		} catch ( Exception e ) {
			print( "failed writing error page: " + rule.getErrorPage() );
			// do nothing, we don't really care if the log fails.
		}

		return errorcount;
	}

	protected String processRevisions( PageRule rule, Revisions revs, List<String> outputList, SortedMap<Integer, Integer> outputByDay ) throws Exception {
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

			int score = new ArticleScorer( fetcher, rule, article ).score( pageText );
			// print( "Article: " + article + ", score: " + score + ", search: " + rule.getSearchName() );

			if ( score >= rule.getThreshold() ) {
				print( "score is above threshold! Article: " + article + ", score: " + score + ", search: " + rule.getSearchName() + ", time: "
						+ WikiHelpers.calendarToTimestamp( rev.getTimestamp() ) );

				addEntryToOutputLists( rev, rule.getSearchName(), score, outputList, outputByDay );
			}

			lastRevisionTime = WikiHelpers.calendarToTimestamp( rev.getTimestamp() );
		}

		return lastRevisionTime;
	}

	public Revisions fetch( int fetchPageCount, String rcstart ) throws Exception {
		Revisions revs = null;
		String retry = null;

		try {
			revs = wiki.newPages( fetchPageCount, Wiki.MAIN_NAMESPACE, 0, rcstart );
		} catch ( SocketTimeoutException ex ) {
			retry = ex.getMessage();
		} catch ( EOFException ex ) {
			retry = ex.getMessage();
		}

		if ( retry != null ) {
			print( "trying to fetch new pages again. Previous error: " + retry );
			revs = wiki.newPages( fetchPageCount, Wiki.MAIN_NAMESPACE, 0, rcstart );
		}

		return revs;
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
