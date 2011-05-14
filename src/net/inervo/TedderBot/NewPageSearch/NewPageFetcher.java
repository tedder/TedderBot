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
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.inervo.WMFWiki11;
import net.inervo.WMFWiki11.Revisions;
import net.inervo.TedderBot.Configuration;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class NewPageFetcher {
	protected WMFWiki11 wiki = null;

	public NewPageFetcher( WMFWiki11 wiki ) {
		this.wiki = wiki;
	}

	public Revisions fetch( int fetchPages ) throws Exception {
		Revisions revs = wiki.newPages( fetchPages, Wiki.MAIN_NAMESPACE, 0, new GregorianCalendar( 2011, 04, 01, 0, 01, 03 ) );
		return revs;
	}

	public static void main( String[] args ) throws Exception {

		print( "hello world!" );

		Configuration config = new Configuration( new File( "wiki.properties" ) );

		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 15 );
		print( "db lag (seconds): " + wiki.getCurrentDatabaseLag() );

		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		PageRules rules = new PageRules( wiki );

		NewPageFetcher npp = new NewPageFetcher( wiki );
		Revisions revs = npp.fetch( 5 );
		// print( npp.revisionsToString( revs ) );

		// PageRule rule = new PageRule( wiki, "User:AlexNewArtBot/Oregon", "Oregon", null );

		for ( Revision rev : revs.getRevisionList() ) {
			String article = rev.getPage();
			String pageText = wiki.getPageText( article );

			for ( PageRule rule : rules.getRules() ) {

				int score = new ArticleScorer( wiki, rule, article ).score( pageText );
				if ( score >= rule.getThreshold() ) {
					// print( "article: " + rev.getPage() );
					print( "score is above threshold! Article: " + article );
				}
				// TODO: catch these errors so we can go on, report them at the end. Maybe skip this rule too, leave a
				// note on the rule/search result talk pages?
				//
				// } catch (PatternSyntaxException ex) {
			}
		}

		// parser.parseRule( );
		// print( wiki.getPageText( "User:AlexNewArtBot/LosAngeles" ) );
	}

	private static void print( String s ) {
		System.out.println( s );
	}

	public String revisionsToString( Revisions revs ) {
		StringBuilder ret = new StringBuilder();
		for ( Revision rev : revs.getRevisionList() ) {
			ret.append( "revid: " + rev.getRevid() + ", page: " + rev.getPage() + " " + calendarToTimestamp( rev.getTimestamp() ) + "\n" );
		}
		ret.append( "next start: " + revs.getRcStart() + "\n" );

		return ret.toString();
	}

	// pay no mind, just a helper function.
	private static String calendarToTimestamp( Calendar c ) {
		return String.format( "%04d%02d%02d%02d%02d%02d", c.get( Calendar.YEAR ), c.get( Calendar.MONTH ) + 1, c.get( Calendar.DAY_OF_MONTH ),
				c.get( Calendar.HOUR_OF_DAY ), c.get( Calendar.MINUTE ), c.get( Calendar.SECOND ) );
	}
}
