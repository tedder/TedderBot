package net.inervo.Wiki.Cache;

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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.inervo.WMFWiki11;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class ArticleCache {
	private static final String CACHE_NAME = "wiki-articles";
	protected CacheManager cacheManager;
	protected Cache cache;
	protected WMFWiki11 wiki;
	private static final Logger logger = Logger.getLogger( ArticleCache.class.getCanonicalName() );

	public ArticleCache( WMFWiki11 wiki ) throws Exception {
		this.wiki = wiki;
		init();
	}

	public void init() throws Exception {
		//URL configXML = this.getClass().getClassLoader().getResource( "ehcache.xml" );
		// I need to fix this classloader.
		URL configXML = new File("/mnt/readynas/documents/code/TedderBot/ehcache.xml").toURI().toURL();
		info( "configuration location: " + configXML );
		cacheManager = CacheManager.create( configXML );

		if ( !cacheManager.cacheExists( CACHE_NAME ) ) {
			throw new Exception( CACHE_NAME + " cache didn't exist." );
		}

		cache = cacheManager.getCache( CACHE_NAME );
	}

	public void shutdown() {
		cacheManager.shutdown();
		info( "done with shutdown" );
	}

	public String fetchPage( String articleName ) throws IOException {
		return fetchPage( articleName, false );
	}

	public String fetchPage( String articleName, boolean disableCache ) throws IOException {
		// if we have a cached copy, return it.
		if ( !disableCache && cache.isKeyInCache( articleName ) && cache.get( articleName ) != null ) {
			return (String) cache.get( articleName ).getValue();
		}

		info( "uncached article: " + articleName );
		String articleText = null;
		try {
			articleText = wiki.getPageText( articleName );
		} catch ( FileNotFoundException ex ) {
			articleText = "[DELETED]";
		}

		cache.put( new Element( articleName, articleText ) );

		return articleText;
	}

	protected void info( String str ) {
		logger.log( Level.INFO, str );
	}

}
