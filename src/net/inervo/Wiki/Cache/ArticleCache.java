package net.inervo.Wiki.Cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import net.inervo.WMFWiki11;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class ArticleCache {
	private static final String CACHE_NAME = "wiki-articles";
	protected CacheManager cacheManager;
	protected Cache cache;
	protected WMFWiki11 wiki;

	public ArticleCache( WMFWiki11 wiki ) throws Exception {
		this.wiki = wiki;
		init();
	}

	public void init() throws Exception {
		URL configXML = this.getClass().getClassLoader().getResource( "ehcache.xml" );
		System.out.println( "configuration location: " + configXML );
		cacheManager = CacheManager.create( configXML );

		if ( !cacheManager.cacheExists( CACHE_NAME ) ) {
			throw new Exception( CACHE_NAME + " cache didn't exist." );
		}

		cache = cacheManager.getCache( CACHE_NAME );
	}

	public void shutdown() {
		cacheManager.shutdown();
		System.out.println( "done with shutdown" );
	}

	public String fetchPage( String articleName ) throws IOException {
		// if we have a cached copy, return it.
		if ( cache.isKeyInCache( articleName ) ) {
			return (String) cache.get( articleName ).getValue();
		}

		System.out.println( "uncached article: " + articleName );
		String articleText = null;
		try {
			articleText = wiki.getPageText( articleName );
		} catch (FileNotFoundException ex) {
			articleText = "[DELETED]";
		}

		cache.put( new Element( articleName, articleText ) );

		return articleText;
	}

}
