package net.inervo.Wiki;

import java.io.IOException;

import net.inervo.WMFWiki11;

public class RetryEditor implements PageEditor {
	protected WMFWiki11 wiki;

	public RetryEditor( WMFWiki11 wiki ) {
		this.wiki = wiki;
	}

	public void edit( String title, String text, String summary, boolean minor ) throws Exception {

		boolean success = false;
		for ( int i = 0; i < 5; ++i ) {
			try {
				wiki.edit( title, text, summary, minor );
				success = true;
			} catch ( IOException ex ) {
				// retry once.
				System.err.println( "Sleeping, couldn't edit page: " + title );
				Thread.sleep( 1000 );
			}
		}

		if ( success == false ) {
			throw new IOException( "failed to update page: " + title );
		}
	}
}
