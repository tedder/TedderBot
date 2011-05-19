package net.inervo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

public class Keystore {

	private static final String DEFAULT_DATA_DOMAIN = "generic";
	AmazonSimpleDB sdb = null;
	String itemKey = null;

	// public Keystore( String itemKey ) throws IOException {
	// this( new File( "AwsCredentials.properties" ), itemKey );
	// }

	public Keystore( String propFilename, String itemKey ) throws FileNotFoundException, IllegalArgumentException, IOException {
		this( new File( propFilename ), itemKey );
	}

	public Keystore( File propFile, String itemKey ) throws FileNotFoundException, IllegalArgumentException, IOException {
		sdb = new AmazonSimpleDBClient( new PropertiesCredentials( propFile ) );
		createDataDomainIfNecessary( DEFAULT_DATA_DOMAIN );
		this.itemKey = itemKey;
	}

	public void replace( String key, String value ) {
		put( key, value, true );
	}

	public void append( String key, String value ) {
		put( key, value, false );
	}

	public String getKey( String key ) {
		// List<Attribute> result = sdb.getAttributes( new GetAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey )
		// ).getAttributes();
		List<Attribute> result = sdb.getAttributes( new GetAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey ).withAttributeNames( key ) ).getAttributes();
		if ( result.size() == 0 ) {
			return null;
		}

		return result.get( 0 ).getValue();
	}

	public String getKey( String key, String defaultValue ) {
		String ret = getKey( key );
		return ret == null ? defaultValue : ret;
	}

	public void put( String key, String value, boolean replace ) {
		List<ReplaceableAttribute> values = new ArrayList<ReplaceableAttribute>();
		values.add( new ReplaceableAttribute().withReplace( replace ).withName( key ).withValue( value ) );

		sdb.putAttributes( new PutAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey, values ) );
	}

	private void createDataDomainIfNecessary( String domain ) {
		if ( dataDomainExists( domain ) == false ) {
			sdb.createDomain( new CreateDomainRequest( domain ) );
		}
	}

	private boolean dataDomainExists( String domain ) {
		boolean exists = false;

		ListDomainsRequest dr = new ListDomainsRequest().withNextToken( domain ).withMaxNumberOfDomains( 1 );
		for ( String domainName : sdb.listDomains( dr ).getDomainNames() ) {
			if ( domainName.equals( domain ) ) {
				exists = true;
				break;
			}
		}

		return exists;
	}

}
