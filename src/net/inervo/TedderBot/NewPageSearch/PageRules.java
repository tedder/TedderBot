package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import net.inervo.TedderBot.Configuration;

import org.wikipedia.WMFWiki;

public class PageRules {
	private WMFWiki wiki = null;
	List<PageRuleParser> pages = new ArrayList<PageRuleParser>();

	public PageRules( WMFWiki wiki ) throws IOException {
		this( wiki, "User:AlexNewArtBot/Master" );
	}

	public PageRules( WMFWiki wiki, String pageName ) throws IOException {
		this.wiki = wiki;
		parseMaster( pageName );
	}

	private void parseMaster( String masterPage ) throws IOException {
		String input = wiki.getPageText( masterPage );
		Scanner s = new Scanner( input ).useDelimiter( "\\n" );

		while ( s.hasNext() ) {
			String line = s.next();
			// print( "line: " + line );

			PageRuleParser rule = parseLine( line );
			if ( rule != null ) {
				pages.add( rule );
			}
		}
	}

	private PageRuleParser parseLine( String line ) throws IOException {
		Pattern rexLine = Pattern.compile( "^(.+?)\\s*(=>\\s*(.*))?$" );
		Matcher rexMatcher = rexLine.matcher( line );
		PageRuleParser prp = null;

		if ( rexMatcher.matches() ) {
			String rule = rexMatcher.group( 1 ).trim();
			String target = null;

			if ( rexMatcher.groupCount() >= 3 ) {
				target = rexMatcher.group( 3 );
			}

			String rulePage = "User:AlexNewArtBot/" + rule;
			print( "rp: " + rulePage + ", r: " + rule + ", target: " + target );
			if ( target != null && target.length() > 0 ) {
				// throw new IOException("fuck");
			}
			try {
				prp = new PageRuleParser( wiki, rulePage, rule, target );
				print( "WPRP: " + rulePage + " / " + rule + " / " + target + " / " + prp.getSearchName() );
			} catch ( FileNotFoundException ex ) {
				print( "ruleset doesn't exist: " + rulePage );
			}

			// print( "rule: " + rule + ", target: " + target );
		} else {
			print( "line didn't match: " + line );
		}

		return prp;
	}

	List<PageRuleParser> getRules() {
		return pages;
	}

	private static void print( String s ) {
		System.out.println( s );
	}

	public static void main( String[] args ) throws IOException, LoginException {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki wiki = new WMFWiki( "en.wikipedia.org" );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		PageRules rules = new PageRules( wiki );
		StringBuilder errorBuilder = new StringBuilder();
		errorBuilder.append( "{| class=\"wikitable\"\n! search\n! errors\n" );

		for ( PageRuleParser rule : rules.getRules() ) {
			if ( rule.getErrors().size() == 0) {
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

		print( "text: " + errorBuilder.toString() );

		wiki.edit( "User:TedderBot/SearchBotErrors", errorBuilder.toString(), "most recent errors", false );

		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );
	}

	private static String join( String delim, String... arr ) {
		StringBuilder ret = new StringBuilder();
		for ( String row : arr ) {
			if ( ret.length() != 0 ) {
				ret.append( delim );
			}
			ret.append( row );
		}

		return ret.toString();
	}

	private static String buildString( String delim, List<String> list ) {
		StringBuilder ret = new StringBuilder();
		boolean first = true;
		for ( String line : list ) {
			ret.append( line );
			if ( first == true ) {
				delim = "";
			}
			ret.append( delim );

		}
		return ret.toString();
	}
}
