package net.inervo;

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

	// String itemKey = null;

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
