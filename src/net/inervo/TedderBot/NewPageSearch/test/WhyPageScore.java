package net.inervo.TedderBot.NewPageSearch.test;

import java.util.logging.Level;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.NewPageSearch.ArticleScorer;
import net.inervo.TedderBot.NewPageSearch.ArticleScorer.ScoreResults;
import net.inervo.TedderBot.NewPageSearch.PageRule;
import net.inervo.TedderBot.NewPageSearch.PageRules;
import net.inervo.Wiki.BasicFetcher;
import net.inervo.Wiki.WikiFetcher;

public class WhyPageScore {
	public final static String SEARCH = "Astro";
	public final static String ARTICLE = "HD 85512 b";

	public static void main( String[] args ) throws Exception
	{
		// PersistentKeystore.initialize( "AwsCredentials.properties" );
		// Configuration config = new Configuration( "wiki.properties" );

		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 45 );
		wiki.setLogLevel( Level.WARNING );

		WikiFetcher fetcher = new BasicFetcher( wiki );

		PageRules rules = new PageRules( fetcher, "User:AlexNewArtBot/Master", SEARCH );
		PageRule rule = rules.getRules().get( 0 );

		ArticleScorer as = new ArticleScorer( fetcher, rule, ARTICLE );

		// String articletext = new URL()

		int score = as.score();
		System.out.println( "score: " + score + ", article: " + ARTICLE + ", score: " + score + ", search: " + rule.getSearchName() );

		for ( ScoreResults note : as.getScoreNotes() ) {
			System.out.println( note.getRule().getPattern().toString() + "::" + note.getScore() );
		}
		// outputList.add( new RuleResultPage( rev, score ) );
		// scoreNotes.put( article, as.getScoreNotes() );
	}

}
