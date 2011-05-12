package net.inervo.TedderBot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	private Properties configFile = null;
	private File filename = null;

	public Configuration( File filename ) throws IOException {
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
		configFile.load( new FileReader( filename ) );

	}
}
