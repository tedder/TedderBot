package net.inervo.Wiki;

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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.inervo.WMFWiki11;

public class RetryEditor implements PageEditor {
	protected WMFWiki11 wiki;
	private static final Logger logger = Logger.getLogger( RetryEditor.class.getCanonicalName() );

	public RetryEditor( WMFWiki11 wiki ) {
		this.wiki = wiki;
	}

	public void edit( String title, String text, String summary, boolean minor ) throws Exception {
		edit( title, text, summary, minor, -2 );
	}

	public void edit( String title, String text, String summary, boolean minor, int section ) throws Exception {

		boolean success = false;
		for ( int i = 0; success == false && i < 5; ++i ) {
			try {
				wiki.edit( title, text, summary, minor, section );
				success = true;
			} catch ( IOException ex ) {
				warn( "Sleeping, couldn't edit page (IO exception): " + title );
				Thread.sleep( 1000 );
				warn( "Done sleeping: " + title );
			}catch ( UnknownError ex ) {
				warn( "Sleeping, couldn't edit page (unknown error): " + title );
				Thread.sleep( 1000 );
				warn( "Done sleeping: " + title );
			}
		}

		if ( success == false ) {
			throw new IOException( "failed to update page: " + title );
		}
	}

	protected void warn( String str ) {
		logger.log( Level.WARNING, str );
	}
}
