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
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.inervo.WMFWiki11;
import net.inervo.WMFWiki11.Revisions;
import net.inervo.TedderBot.NewPageSearch.ArticleScorer.ScoreResults;
import net.inervo.Wiki.PageEditor;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.WikiHelpers;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class NewPageFetcher {
	protected WMFWiki11 wiki = null;
	protected WikiFetcher fetcher = null;
	protected PageEditor editor = null;
	private static final Logger logger = Logger.getLogger( NewPageFetcher.class.getCanonicalName() );

	// protected List<String> errors = new ArrayList<String>();

	/**
	 * 
	 * @param wiki
	 * @param fetcher
	 * @param editor
	 * @param rule
	 * @param debug
	 */
	public NewPageFetcher( WMFWiki11 wiki, WikiFetcher fetcher, PageEditor editor )
	{
		this.wiki = wiki;
		this.fetcher = fetcher;
		this.editor = editor;
	}

	public String runFetcher( String startTimestamp, PageRule rule ) throws Exception
	{
		// String debugOverride = debug ? "Oregon" : null;
		String lastTimestamp = null;

		Revisions revs = null;

		ArrayList<RuleResultPage> outputList = new ArrayList<RuleResultPage>();
		Map<String, List<ScoreResults>> scoreNotes = new HashMap<String, List<ScoreResults>>();

		SortedMap<Integer, Integer> outputByDay = getZeroFilledOutputMap( startTimestamp );

		// String start = calendarToTimestamp( new GregorianCalendar( 2011, 04, 01, 0, 01, 03 ) );
		do {
			info( "about to start fetch of stamp: " + startTimestamp );
			revs = fetch( 5000, startTimestamp );
			info( "done with fetch of stamp: " + startTimestamp );
			lastTimestamp = processRevisions( rule, revs, outputList, scoreNotes, outputByDay );

			startTimestamp = revs.getRcStart();
			info( "rcstart: " + startTimestamp );
		} while ( startTimestamp != null && startTimestamp.length() > 0 );

		int errorCount = writeRuleErrors( rule, scoreNotes );
		outputResultsForRule( rule, errorCount, outputList, scoreNotes, outputByDay );

		return lastTimestamp;
	}

	protected TreeMap<Integer, Integer> getZeroFilledOutputMap( String startTimestamp )
	{
		TreeMap<Integer, Integer> ret = new TreeMap<Integer, Integer>();
		Calendar startCal = WikiHelpers.timestampToCalendar( startTimestamp );

		Calendar endCal = new GregorianCalendar();
		for ( Calendar today = startCal; today.before( endCal ); today.add( Calendar.HOUR, 24 ) ) {
			Integer datestamp = Integer.valueOf( WikiHelpers.calendarToDatestamp( today ) );
			ret.put( datestamp, 0 );
		}

		// the last one won't get inserted by the above loop, do it manually.
		{
			Integer datestamp = Integer.valueOf( WikiHelpers.calendarToDatestamp( endCal ) );
			ret.put( datestamp, 0 );
		}

		return ret;
	}

	protected void addEntryToDayList( Revision rev, SortedMap<Integer, Integer> outputByDay )
	{
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

	protected String getResultOutputLine( RuleResultPage result )
	{
		Revision rev = result.getRev();
		SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
		return "*{{la|" + rev.getPage() + "}} by {{User|" + rev.getUser() + "}} started at <span class=\"mw-newpages-time\">"
			+ sdf.format( rev.getTimestamp().getTime() ) + "</span>, score: " + result.getScore();
	}

	protected void archiveResults( String archivePage, Collection<String> lines ) throws Exception
	{

		StringBuilder text = new StringBuilder();

		for ( String line : lines ) {
			text.append( line );
			text.append( "\n" );
		}

		try {
			editor.edit( archivePage, text.toString(), "archived entries", false, -1 );
		} catch ( IOException ex ) {
			info( "failed updating " + archivePage );
		}
	}

	protected void outputResultsForRule( PageRule rule, int searchErrorCount, List<RuleResultPage> results,
		Map<String, List<ScoreResults>> scoreNotes, SortedMap<Integer, Integer> outputByDay ) throws Exception
	{

		// get the existing articles
		Map<String, String> erf = new ExistingResultsFetcher( fetcher ).getExistingResults( rule.getSearchResultPage() );

		// we have the list of articles already on the page. Archive all of them except the ones we are adding/keeping
		// now.
		for ( RuleResultPage result : results ) {
			erf.remove( result.getRev().getPage() );
		}

		// do the archiving
		String archivedSummary = "";
		if ( erf.size() > 0 ) {
			archivedSummary += " " + erf.size() + " ";
			archivedSummary += erf.size() == 1 ? "page" : "pages";
			archivedSummary += " archived";
			archiveResults( rule.getArchivePage(), erf.values() );
		}

		StringBuilder searchResultText = new StringBuilder();
		StringBuilder subject = new StringBuilder( "most recent results" );

		if ( searchErrorCount > 0 ) {
			String errorLabel = searchErrorCount == 0 ? "error" : "errors";
			subject.append( ", " + searchErrorCount + " [[" + rule.getErrorPage() + "|" + errorLabel + "]]" );
			searchResultText.append( "'''There were [[" + rule.getErrorPage() + "|" + searchErrorCount + " " + errorLabel
				+ " encountered]] while parsing the [[" + rule.getRulePage() + "|" + "rules for this search]].''' " );
		}

		searchResultText.append( "This list was generated from [[" + rule.getRulePage() + "|these rules]] ({{oldid|1=" + rule.getRulePage()
			+ "|2=" + rule.getRevisionID() + "|3=ruleset version}}). Questions and feedback [[User talk:Tedder|are always welcome]]! "
			+ "The search is being run manually, but eventually will run ~daily with the most recent ~7 days of results.\n\n" + "[["
			+ rule.getOldArchivePage() + "|AlexNewArtBot archives]] | [[" + rule.getArchivePage() + "|TedderBot archives]] | [["
			+ rule.getRulePage() + "|Rules]] | [[" + rule.getErrorPage() + "|Match log and errors]]\n\n" );

		if ( results.size() > 0 ) {
			// sort the list
			Collections.sort( results, new RuleResultsByDate() );
			// and reverse it, so we have newest = top
			Collections.reverse( results );

			String countLabel = results.size() == 1 ? "article" : "articles";
			subject.append( ", " + results.size() + " " + countLabel );
			for ( RuleResultPage result : results ) {
				searchResultText.append( getResultOutputLine( result ) );
				searchResultText.append( "\n" );
			}
		} else {
			searchResultText.append( "There are no current results for this search, sorry." );
		}

		subject.append( ", daily counts: " + getSparkline( outputByDay ) );
		if ( !archivedSummary.isEmpty() ) {
			subject.append( archivedSummary );
		}

		try {
			editor.edit( rule.getSearchResultPage(), searchResultText.toString(), subject.toString(), false );
		} catch ( IOException ex ) {
			info( "failed updating " + rule.getSearchResultPage() );
		}
	}

	protected String getSparkline( SortedMap<Integer, Integer> resultCounts )
	{
		List<Double> numbers = new ArrayList<Double>();

		if ( resultCounts == null || resultCounts.values() == null ) {
			return "";
		}
		for ( Integer value : resultCounts.values() ) {
			numbers.add( (double) value );
		}

		return new Sparkline().getSparkline( numbers );
	}

	protected int writeRuleErrors( PageRule rule, Map<String, List<ScoreResults>> scoreNotes )
	{
		int patterncount = rule.getPatterns().size();
		// rule.

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

		errorBuilder.append( "==Scoring notes==\n\n" );

		for ( Entry<String, List<ScoreResults>> noteEntry : scoreNotes.entrySet() ) {
			String article = noteEntry.getKey();
			List<ScoreResults> notes = noteEntry.getValue();
			errorBuilder.append( "*{{la|" + article + "}}\n" );
			for ( ScoreResults note : notes ) {
				errorBuilder.append( "** Score: " + note.getScore() + ", pattern: <nowiki>" + note.getRule().getPattern().pattern()
					+ "</nowiki>, inhibitor count: " + note.getRule().getIgnoreCount() + "\n" );
			}

		}

		try {
			editor.edit( rule.getErrorPage(), errorBuilder.toString(), "most recent errors and scoring", false );
		} catch ( Exception e ) {
			info( "failed writing error page: " + rule.getErrorPage() );
			// do nothing, we don't really care if the log fails.
		}

		return errorcount;
	}

	protected String processRevisions( PageRule rule, Revisions revs, List<RuleResultPage> outputList,
		Map<String, List<ScoreResults>> scoreNotes, SortedMap<Integer, Integer> outputByDay ) throws Exception
	{
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

			ArticleScorer as = new ArticleScorer( fetcher, rule, article );
			int score = as.score( pageText );
			if ( score >= rule.getThreshold() ) {
				info( "score is above threshold! Article: " + article + ", score: " + score + ", search: " + rule.getSearchName()
					+ ", time: " + WikiHelpers.calendarToTimestamp( rev.getTimestamp() ) );

				outputList.add( new RuleResultPage( rev, score ) );
				scoreNotes.put( article, as.getScoreNotes() );
				addEntryToDayList( rev, outputByDay );
			}

			lastRevisionTime = WikiHelpers.calendarToTimestamp( rev.getTimestamp() );
		}

		return lastRevisionTime;
	}

	public Revisions fetch( int fetchPageCount, String rcstart ) throws Exception
	{
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
			info( "trying to fetch new pages again. Previous error: " + retry );
			revs = wiki.newPages( fetchPageCount, Wiki.MAIN_NAMESPACE, 0, rcstart );
		}

		return revs;
	}

	/*** helper functions ***/

	protected static void info( String s )
	{
		logger.log( Level.INFO, s );
	}

	protected static String join( String delim, String... arr )
	{
		StringBuilder ret = new StringBuilder();
		for ( String row : arr ) {
			if ( ret.length() != 0 ) {
				ret.append( delim );
			}
			ret.append( row );
		}

		return ret.toString();
	}

	public static class RuleResultsByDate implements Comparator<RuleResultPage> {
		public RuleResultsByDate()
		{
		};

		@Override
		public int compare( RuleResultPage arg0, RuleResultPage arg1 )
		{
			return arg0.getRev().getTimestamp().compareTo( arg1.getRev().getTimestamp() );
		}
	}
}
