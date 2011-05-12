package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.inervo.TedderBot.Configuration;

import org.wikipedia.WMFWiki;

public class PageRuleParser {
	private WMFWiki wiki = null;
	private String target = null;
	private String searchName = null;
	private String pageName = null;
	private List<MatchRule> patterns = new ArrayList<MatchRule>();
	private List<String> errors = new LinkedList<String>();

	public PageRuleParser( WMFWiki wiki, String pageName, String searchName, String target ) throws IOException {
		this.wiki = wiki;
		this.target = target;
		this.searchName = searchName;
		this.pageName = pageName;
		parseRule( pageName );
	}

	private void parseRule( String pageName ) throws IOException {
		int defaultScore = 10;

		Pattern rexLine = Pattern.compile( "^\\s*([\\-\\d]*)\\s*\\/(.*?)\\/(.*)\\s*$" );
		Pattern defaultScorePattern = Pattern.compile( "^\\s*@@(\\d+)@@\\s*$" );
		Pattern categoryPattern = Pattern.compile( "^\\s*(\\d*)\\s*\\$\\$(.*)\\$\\$\\s*$" );

		String input = wiki.getPageText( pageName );
		// print( "text: |" + input + "|" );
		Scanner s = new Scanner( input ).useDelimiter( "\\n" );

		while ( s.hasNext() ) {
			String line = stripComments( s.next() );

			Matcher rexMatcher = rexLine.matcher( line );
			Matcher scoreMatcher = defaultScorePattern.matcher( line );
			Matcher categoryMatcher = categoryPattern.matcher( line );

			MatchRule rule = new MatchRule();

			if ( rexMatcher.matches() ) {
				String scoreString = rexMatcher.group( 1 );
				rule.score = defaultScore;
				if ( scoreString.length() > 0 ) {
					rule.score = Integer.parseInt( scoreString );
				}

				rule.pattern = rexMatcher.group( 2 );

				if ( rexMatcher.groupCount() >= 3 ) {
					String inhibitors = rexMatcher.group( 3 ).trim();
					// print( "inhib: " + inhibitors );
					rule.ignore = parseInhibitors( inhibitors );
				}
				// if ( rexMatcher.groupCount() > 2 ) {
				// String extra = rexMatcher.group( 3 );
				// if ( extra.length() > 0 ) {
				// print( "extra: |" + extra + "|, whole line: " + line );
				// }
				// }

				// print( "match: " + rule.pattern + " / " + rule.score );
				patterns.add( rule );
			} else if ( scoreMatcher.matches() ) {
				String score = scoreMatcher.group( 1 );
				defaultScore = Integer.parseInt( score );
			} else if ( categoryMatcher.matches() ) {
				// print( "category: " + line + " count: " + categoryMatcher.groupCount() );
				String scoreString = categoryMatcher.group( 1 );
				rule.score = defaultScore;
				if ( scoreString.length() > 0 ) {
					rule.score = Integer.parseInt( scoreString );
				}

				rule.pattern = categoryMatcher.group( 2 );
				patterns.add( rule );
			} else {
				print( "no match: " + line );
				errors.add( "no match: " + line );
			}
		}

		// pattern:

	}

	private String stripComments( String next ) {
		return next.replaceAll( "<!--.*?-->", "" );
	}

	private List<String> parseInhibitors( String inhibitors ) {
		if ( inhibitors == null || inhibitors.length() == 0 ) {
			return null;
		}
		List<String> list = new ArrayList<String>();

		while ( true ) {
			Pattern inhibitorPattern = Pattern.compile( "^\\,\\s*\\/(.*?)\\/\\s*" );
			Matcher matcher = inhibitorPattern.matcher( inhibitors );
			if ( !matcher.matches() ) {
				break;
			}

			String pattern = matcher.group( 1 );

			list.add( pattern );

			inhibitors = matcher.replaceFirst( "" );
		}

		return list;
	}

	public static void main( String[] args ) throws Exception {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki wiki = new WMFWiki( "en.wikipedia.org" );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		PageRuleParser parser = new PageRuleParser( wiki, "User:AlexNewArtBot/Oregon", "Oregon", null );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );
		// parser.parseRule( );
		// print( wiki.getPageText( "User:AlexNewArtBot/LosAngeles" ) );
	}

	private static void print( String s ) {
		System.out.println( s );
	}

	public static class MatchRule {

		protected List<String> ignore;
		protected String pattern;
		protected int score;

		public List<String> getIgnore() {
			return ignore;
		}

		public String getPattern() {
			return pattern;
		}

		public int getScore() {
			return score;
		}

	}

	/** getters and setters **/

	public String getSearchName() {
		return searchName;
	}

	public List<MatchRule> getPatterns() {
		return patterns;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getPageName() {
		return pageName;
	}

	public String getTarget() {
		if ( target == null ) {
			return "User:AlexNewArtBot/" + searchName + "SearchResult";
		}
		return target;
	}

}
