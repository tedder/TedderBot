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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wikipedia.WMFWiki;

public class PageRules {
	private WMFWiki wiki = null;
	List<PageRule> pages = new ArrayList<PageRule>();

	public PageRules( WMFWiki wiki, String pageName, String ruleOverride ) throws IOException {
		this.wiki = wiki;
		parseMaster( pageName, ruleOverride );
		// writeErrors();
	}

	protected void parseMaster( String masterPage, String ruleOverride ) throws IOException {
		String input = wiki.getPageText( masterPage );
		Scanner s = new Scanner( input ).useDelimiter( "\\n" );

		// process each line into a page.
		LINE: while ( s.hasNext() ) {
			String line = s.next();
			// print( "line: " + line + ", ro: "+ ruleOverride );

			if ( ruleOverride != null ) {
				if ( !line.contains( ruleOverride ) ) {
					continue LINE;
				}
			}

			PageRule rule = parseLine( line );

			if ( rule != null ) {
				pages.add( rule );
			}
		}
	}

	protected PageRule parseLine( String line ) throws IOException {
		Pattern rexLine = Pattern.compile( "^(.+?)\\s*(=>\\s*(.*))?$" );
		Matcher rexMatcher = rexLine.matcher( line );
		PageRule prp = null;

		if ( rexMatcher.matches() ) {
			String rule = rexMatcher.group( 1 ).trim();
			String rulePage = "User:AlexNewArtBot/" + rule;

			try {
				prp = new PageRule( wiki, rulePage, rule );
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

	protected List<PageRule> getRules() {
		return pages;
	}

	private static void print( String s ) {
		System.out.println( s );
	}

}
