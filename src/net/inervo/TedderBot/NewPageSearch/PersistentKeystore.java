package net.inervo.TedderBot.NewPageSearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.inervo.data.Keystore;

public class PersistentKeystore {
	public static final String LAST_PROCESSED = "lastProcessed";

	protected static Keystore keystoreInstance = null;

	private PersistentKeystore() {
	}

	protected static void verifyInstantiation() throws Exception {
		if ( keystoreInstance == null ) {
			throw new Exception( "Persistent keystore was not properly created. It requires an explicit initialization." );
		}
	}

	protected static void initialize( File propFile, String keyName ) throws FileNotFoundException, IllegalArgumentException, IOException {
		keystoreInstance = new Keystore( new File( "AwsCredentials.properties" ), keyName );
	}

	protected static Keystore getKeystore() {
		return keystoreInstance;
	}

	public static String get( String key ) {
		return getKeystore().getKey( key );
	}

	public static void put( String key, String value, boolean overwrite ) {
		getKeystore().put( key, value, overwrite );
	}

}
