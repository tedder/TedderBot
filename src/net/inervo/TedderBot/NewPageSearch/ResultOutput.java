package net.inervo.TedderBot.NewPageSearch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.inervo.TedderBot.NewPageSearch.ArticleScorer.ScoreResults;
import net.inervo.TedderBot.NewPageSearch.NewPageFetcher.RuleResultsByDate;
import net.inervo.Wiki.PageEditor;
import net.inervo.Wiki.WikiFetcher;

import org.wikipedia.Wiki.Revision;

public class ResultOutput {
	protected static final Logger logger = Logger.getLogger( NewPageFetcher.class.getCanonicalName() );
	protected WikiFetcher fetcher;
	protected PageEditor editor;

	public ResultOutput( WikiFetcher fetcher, PageEditor editor ) {
		this.fetcher = fetcher;
		this.editor = editor;
	}

	public void outputResultsForRule( PageRule rule, int searchErrorCount, List<RuleResultPage> results, Map<String, List<ScoreResults>> scoreNotes,
			SortedMap<Integer, Integer> outputByDay ) throws Exception {

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

		searchResultText.append( "This list was generated from [[" + rule.getRulePage()
				+ "|these rules]]. Questions and feedback [[User talk:Tedder|are always welcome]]! "
				+ "The search is being run manually, but eventually will run ~daily with the most recent ~7 days of results.\n\n" + "[["
				+ rule.getOldArchivePage() + "|AlexNewArtBot archives]] | [[" + rule.getArchivePage() + "|TedderBot archives]] | [[" + rule.getRulePage()
				+ "|Rules]] | [[" + rule.getErrorPage() + "|Match log and errors]]\n\n" );

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
			editor.edit( rule.getSearchResultPage(), searchResultText.toString(), subject.toString() );
		} catch ( IOException ex ) {
			info( "failed updating " + rule.getSearchResultPage() );
		}
	}

	protected String getSparkline( SortedMap<Integer, Integer> resultCounts ) {
		List<Double> numbers = new ArrayList<Double>();

		if ( resultCounts == null || resultCounts.values() == null ) {
			return "";
		}
		for ( Integer value : resultCounts.values() ) {
			numbers.add( (double) value );
		}

		return new Sparkline().getSparkline( numbers );
	}

	protected String getResultOutputLine( RuleResultPage result ) {
		Revision rev = result.getRev();
		SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
		return "*{{la|" + rev.getPage() + "}} by {{User|" + rev.getUser() + "}} started at <span class=\"mw-newpages-time\">"
				+ sdf.format( rev.getTimestamp().getTime() ) + "</span>, score: " + result.getScore();
	}

	protected void archiveResults( String archivePage, Collection<String> lines ) throws Exception {

		StringBuilder text = new StringBuilder();

		for ( String line : lines ) {
			text.append( line );
			text.append( "\n" );
		}

		try {
			editor.edit( archivePage, text.toString(), "archived entries", -1 );
		} catch ( IOException ex ) {
			info( "failed updating " + archivePage );
		}
	}

	/*** helper functions ***/

	protected static void info( String s ) {
		logger.log( Level.INFO, s );
	}
}