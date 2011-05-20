package net.inervo.TedderBot.NewPageSearch;

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
