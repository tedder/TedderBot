package net.inervo.Wiki;

import net.inervo.WMFWiki11;

public class BasicFetcher implements WikiFetcher {
	private WMFWiki11 wiki;

	public BasicFetcher( WMFWiki11 wiki ) {
		this.wiki = wiki;
	}

	@Override
	public String getPageText( String articleName ) throws Exception {
		return wiki.getPageText( articleName );
	}

}
