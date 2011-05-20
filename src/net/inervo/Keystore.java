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

	// private static final String DEFAULT_DATA_DOMAIN = "generic";
	String domain = null;
	AmazonSimpleDB sdb = null;
//	String itemKey = null;

	// public Keystore( String itemKey ) throws IOException {
	// this( new File( "AwsCredentials.properties" ), itemKey );
	// }

	public Keystore( String propFilename, String domain ) throws FileNotFoundException, IllegalArgumentException, IOException {
		this( new File( propFilename ), domain );
	}

	public Keystore( File propFile, String domain ) throws FileNotFoundException, IllegalArgumentException, IOException {
		sdb = new AmazonSimpleDBClient( new PropertiesCredentials( propFile ) );
		this.domain = domain;
		createDataDomainIfNecessary( domain );
	}

	public void replace( String key, String attribute, String value ) {
		put( key, attribute, value, true );
	}

	public void append( String key, String attribute, String value ) {
		put( key, attribute, value, false );
	}

	public String getKey( String key, String attribute ) {
		// List<Attribute> result = sdb.getAttributes( new GetAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey )
		// ).getAttributes();
		List<Attribute> result = sdb.getAttributes( new GetAttributesRequest( domain, key ).withAttributeNames( attribute ) ).getAttributes();
		if ( result.size() == 0 ) {
			return null;
		}

		return result.get( 0 ).getValue();
	}

	public String getKey( String key, String attribute, String defaultValue ) {
		String ret = getKey( key, attribute );
		return ret == null ? defaultValue : ret;
	}

	public void put( String key, String attribute, String value, boolean replace ) {
		List<ReplaceableAttribute> values = new ArrayList<ReplaceableAttribute>();
		values.add( new ReplaceableAttribute().withReplace( replace ).withName( attribute ).withValue( value ) );

		sdb.putAttributes( new PutAttributesRequest( domain, key, values ) );
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
