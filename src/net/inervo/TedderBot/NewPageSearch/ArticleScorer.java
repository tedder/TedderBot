package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.inervo.TedderBot.Configuration;

import org.wikipedia.WMFWiki;

public class ArticleScorer {
	private PageRuleParser ruleset;
	private String article;
	private WMFWiki wiki;

	public ArticleScorer( WMFWiki wiki, PageRuleParser ruleset, String article ) {
		this.ruleset = ruleset;
		this.article = article;
		this.wiki = wiki;
	}

	private String fetch() throws IOException {
		return wiki.getPageText( article );
	}

	public int score() throws IOException {
		return score( fetch() );
	}

	public int score( String articleText ) {
		int score = 0;

		for ( PageRuleParser.MatchRule rule : ruleset.getPatterns() ) {
			score += scoreRule( articleText, rule );
		}

		return score;

		// Scanner s = new Scanner( articleText ).useDelimiter( "\\n" );
		//
		// while ( s.hasNext() ) {
		// String line = s.next();
		// // print( "line: " + line );
		//
		// PageRuleParser rule = parseLine( line );
		// if ( rule != null ) {
		// pages.add( rule );
		// }
		// }
	}

	private int scoreRule( String articleText, PageRuleParser.MatchRule rule ) {
		boolean foundMatch = ruleMatches( articleText, rule.getPattern() );
		boolean foundIgnore = false;

		if ( rule.getIgnore() != null && rule.getIgnore().size() > 0 ) {
			for ( String pattern : rule.getIgnore() ) {
				if ( ruleMatches( articleText, pattern ) ) {
					foundIgnore = true;
					break;
				}
			}
		}

		int score = 0;
		if ( foundMatch && !foundIgnore ) {
			score = rule.getScore();
		}

		return score;
	}

	private boolean ruleMatches( String articleText, String rulePattern ) {
		boolean found = false;
		print("pattern: " + rulePattern);
		Pattern pattern = Pattern.compile( rulePattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );
		Matcher matcher = pattern.matcher( articleText );

		if ( matcher.find() ) {
			print("matcher matches.");
			found = true;
		}

		return found;
	}

	public static void main( String[] args ) throws Exception {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki wiki = new WMFWiki( "en.wikipedia.org" );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		PageRuleParser parser = new PageRuleParser( wiki, "User:AlexNewArtBot/Oregon", "Oregon", null );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

		ArticleScorer scorer = new ArticleScorer( wiki, parser, "Joseph Gramley" );
		print( "score: " + scorer.score("This is a test with Portland, Oregon mentioned.") );
		print( "score: " + scorer.score() );

	}

	private static void print( String s ) {
		System.out.println( s );
	}

}
