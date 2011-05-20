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

import net.inervo.Keystore;

public class PersistentKeystore {
	public static final String DEFAULT_KEY = "default";
	public static final String LAST_PROCESSED = "lastProcessed";

	protected static Keystore keystoreInstance = null;

	private PersistentKeystore() {
	}

	// private PersistentKeystore( String keyName ) throws Exception {
	// this( "AwsCredentials.properties", keyName );
	// }
	//
	// private PersistentKeystore( String fileName, String keyName ) throws Exception {
	// File file = new File( PersistentKeystore.class.getClassLoader().getSystemResource( fileName ).toURI() );
	// keystoreInstance = new Keystore( file, keyName );
	// }

	protected static void verifyInstantiation() throws Exception {
		if ( keystoreInstance == null ) {
			throw new Exception( "Persistent keystore was not properly created. It requires an explicit initialization." );
		}
	}

	protected static void initialize( String propFile ) throws Exception {
		keystoreInstance = new Keystore( propFile, "WikiNewPageFetcher" );
	}

	protected static Keystore getKeystore() {
		return keystoreInstance;
	}

	public static String get( String key, String attribute ) {
		return getKeystore().getKey( key, attribute );
	}

	public static void put( String key, String attribute, String value, boolean overwrite ) {
		getKeystore().put( key, attribute, value, overwrite );
	}

}
