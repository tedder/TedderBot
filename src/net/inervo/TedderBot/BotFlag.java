package net.inervo.TedderBot;

import java.io.IOException;
import java.util.regex.Pattern;

import org.wikipedia.Wiki;

public class BotFlag {
	// private BotFlag instance = null;
	// private BotFlag() { }

	public static void check( Wiki wiki ) throws IOException, WikiPermissionException {
		String content = wiki.getPageText( "User:TedderBot/Bot_status" );

		boolean okayToRun = Pattern.compile( "^status: run\\b", Pattern.CASE_INSENSITIVE ).matcher( content ).find();
		if ( !okayToRun ) {
			throw new WikiPermissionException( "not authorized to run, according to bot status page.\nContents:\n" + content );
		}
	}

	public static void main( String[] args ) throws Exception {
	}

}