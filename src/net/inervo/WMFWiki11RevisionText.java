package net.inervo;

import java.util.Calendar;

import org.wikipedia.Wiki;
import org.wikipedia.Wiki.Revision;

public class WMFWiki11RevisionText extends Revision {
	String content = "";

	public WMFWiki11RevisionText( Wiki wiki, long revid, Calendar timestamp, String title, String summary, String user, boolean minor,
		boolean bot, String content )
	{

		wiki.super( revid, timestamp, title, summary, user, minor, bot );
		this.content = content;
	}

	public String getContent()
	{
		return content;
	}

}
