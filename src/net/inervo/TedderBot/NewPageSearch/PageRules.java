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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import net.inervo.TedderBot.Configuration;

import org.wikipedia.WMFWiki;

public class PageRules {
	private WMFWiki wiki = null;
	List<PageRule> pages = new ArrayList<PageRule>();

	public PageRules( WMFWiki wiki ) throws IOException {
		this( wiki, "User:AlexNewArtBot/Master" );
	}

	public PageRules( WMFWiki wiki, String pageName ) throws IOException {
		this.wiki = wiki;
		parseMaster( pageName );
		writeErrors();
	}

	private void parseMaster( String masterPage ) throws IOException {
		String input = wiki.getPageText( masterPage );
		Scanner s = new Scanner( input ).useDelimiter( "\\n" );

		while ( s.hasNext() ) {
			String line = s.next();
			// print( "line: " + line );

			PageRule rule = parseLine( line );
			if ( rule != null ) {
				pages.add( rule );
			}
		}
	}

	private PageRule parseLine( String line ) throws IOException {
		Pattern rexLine = Pattern.compile( "^(.+?)\\s*(=>\\s*(.*))?$" );
		Matcher rexMatcher = rexLine.matcher( line );
		PageRule prp = null;

		if ( rexMatcher.matches() ) {
			String rule = rexMatcher.group( 1 ).trim();
			String target = null;

			if ( rexMatcher.groupCount() >= 3 ) {
				target = rexMatcher.group( 3 );
			}

			String rulePage = "User:AlexNewArtBot/" + rule;
			// print( "rp: " + rulePage + ", r: " + rule + ", target: " + target );
			if ( target != null && target.length() > 0 ) {
				// throw new IOException("fuck");
			}
			try {
				prp = new PageRule( wiki, rulePage, rule, target );
				// print( "WPRP: " + rulePage + " / " + rule + " / " + target + " / " + prp.getSearchName() );
			} catch ( FileNotFoundException ex ) {
				print( "ruleset doesn't exist: " + rulePage );
			}

			// print( "rule: " + rule + ", target: " + target );
		} else {
			print( "line didn't match: " + line );
		}

		return prp;
	}

	List<PageRule> getRules() {
		return pages;
	}

	private static void print( String s ) {
		System.out.println( s );
	}

	@SuppressWarnings( "unused" )
	public static void main( String[] args ) throws IOException, LoginException {
		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki wiki = new WMFWiki( "en.wikipedia.org" );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		PageRules rules = new PageRules( wiki );

		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );
	}

	protected void writeErrors() {
		StringBuilder errorBuilder = new StringBuilder();
		errorBuilder.append( "{| class=\"wikitable\"\n! search\n! errors\n" );

		for ( PageRule rule : getRules() ) {
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

	@SuppressWarnings( "unused" )
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
