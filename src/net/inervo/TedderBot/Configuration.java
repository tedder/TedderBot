package net.inervo.TedderBot;

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
import java.util.Properties;

public class Configuration {
	private Properties configFile = null;
	private String filename = null;

	public Configuration( String filename ) throws IOException {
		this.filename = filename;
		configFile = new Properties();
		loadConfigurationFile();
	}

	public String getWikipediaUser() {
		return configFile.getProperty( "username" );
	}

	public String getWikipediaPassword() {
		return configFile.getProperty( "password" );
	}

	private void loadConfigurationFile() throws IOException {
		// configFile.load(
		// this.getClass().getClassLoader().getResourceAsStream(
		// "/wiki.properties" ) );
		// configFile.load( new FileReader( filename ) );
		// configFile.load( this.getClass().getClassLoader().getResourceAsStream( new FileReader(filename) ) );

		// ClassPathResource cpr = new ClassPathResource("ems-init.properties");
		// Properties properties = loadProps(emsInitResource.getInputStream());

		configFile.load( this.getClass().getClassLoader().getResourceAsStream( filename ) );

	}
}
