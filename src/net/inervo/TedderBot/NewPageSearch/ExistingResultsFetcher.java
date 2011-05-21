package net.inervo.TedderBot.NewPageSearch;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.Configuration;
import net.inervo.Wiki.BasicFetcher;
import net.inervo.Wiki.WikiFetcher;

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

public class ExistingResultsFetcher {
	protected WikiFetcher fetcher;

	public ExistingResultsFetcher( WikiFetcher fetcher ) {
		this.fetcher = fetcher;
	}

	public Map<String, String> getExistingResults( String pageName ) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();

		String pageContents = fetcher.getPageText( pageName, true );
		Pattern pattern = Pattern.compile( "^\\s*\\*\\s*\\{\\{la\\|(.*?)\\}\\}\\s+by.*$", Pattern.MULTILINE );
		Matcher matcher = pattern.matcher( pageContents );

		while ( matcher.find() ) {
			String article = matcher.group( 1 );
			String line = matcher.group( 0 );
			ret.put( article, line );
		}

		return ret;
	}

	public static void main( String[] args ) throws Exception {
		PersistentKeystore.initialize( "AwsCredentials.properties" );
		Configuration config = new Configuration( "wiki.properties" );

		WMFWiki11 wiki = new WMFWiki11( "en.wikipedia.org" );
		wiki.setMaxLag( 45 );
		wiki.setLogLevel( Level.WARNING );

		// wiki.setThrottle( 5000 );
		wiki.login( config.getWikipediaUser(), config.getWikipediaPassword().toCharArray() );
		WikiFetcher fetcher = new BasicFetcher( wiki );

		ExistingResultsFetcher erf = new ExistingResultsFetcher( fetcher );
		erf.getExistingResults( "User:AlexNewArtBot/LosAngelesSearchResult" );
	}
}
