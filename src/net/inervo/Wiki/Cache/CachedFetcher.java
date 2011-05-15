package net.inervo.Wiki.Cache;

import net.inervo.Wiki.WikiFetcher;


public class CachedFetcher implements WikiFetcher {
	private ArticleCache cache;

	public CachedFetcher( ArticleCache cache ) {
		this.cache = cache;
	}

	@Override
	public String getPageText( String articleName ) throws Exception {
		return cache.fetchPage( articleName );
	}

}
