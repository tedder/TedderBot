package net.inervo;

/*
 * Copyright (c) 2011, Ted Timmons, Inervo Networks All rights reserved.
 * 
 * LICENSE EXCEPTION: User:MER-C on Wikipedia may remove this license and use the code however necessary to integrate
 * with the Wiki.java project: http://code.google.com/p/wiki-java/
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WMFWiki11 extends org.wikipedia.WMFWiki {
	private static final long serialVersionUID = 1L;

	// only here until this is merged.
	public WMFWiki11( String domain ) {
		super( domain );
	}

	/**
	 * Lazy helper function to check for the existence of a string and see if it is empty.
	 * 
	 * @param str
	 * @return boolean
	 */
	protected boolean isEmptyOrNull( String str ) {
		if ( str == null || str.isEmpty() ) {
			return true;
		}
		return false;
	}

	/**
	 * Helper function to wrap our Long.parseLong call.
	 * 
	 * @param input
	 * @return long
	 */
	protected long tryLongParse( String input ) {
		long ret = 0L;
		try {
			ret = Long.parseLong( input );
		} catch ( NumberFormatException ex ) {
			return -1L;
		}

		return ret;
	}

	/**
	 * From a DOM Element object, parse and return the Revision object
	 * 
	 * @param DOM
	 *            Element
	 * @return Revision object
	 */
	protected Revision parseRevision( Element ele ) {
		long oldid = tryLongParse( ele.getAttribute( "revid" ) );
		log( Level.INFO, ele.getAttribute( "timestamp" ), "timestamp" );
		Calendar timestamp = timestampToCalendar( convertTimestamp( ele.getAttribute( "timestamp" ) ) );
		String title = ele.getAttribute( "title" );
		String summary = ele.getAttribute( "commenthidden" ) == null ? ele.getAttribute( "comment" ) : null;
		String user2 = ele.getAttribute( "user" );
		boolean minor = isEmptyOrNull( ele.getAttribute( "minor" ) );
		boolean bot = isEmptyOrNull( ele.getAttribute( "bot" ) );

		Revision revision = new Revision( oldid, timestamp, title, summary, user2, minor, bot );
		revision.setRcid( tryLongParse( ele.getAttribute( "rcid" ) ) );
		return revision;
	}

	/**
	 * fetch and return new pages from a given time until the correct number of results are returned.
	 * 
	 * @param amount
	 *            , number of results requested
	 * @param namespace
	 *            the namespace to search
	 * @param rcoptions
	 *            a bitmask of HIDE_ANON etc that dictate which pages we return (e.g. exclude patrolled pages =>
	 *            rcoptions = HIDE_PATROLLED).
	 * @param GregorianCalendar
	 *            start time
	 * @return Revisions object, which contains the list of Revision objects and the rcstart continuation
	 * @throws IOException
	 *             on network error
	 * @throws ParserConfigurationException
	 *             on parsing error
	 * @throws SAXException
	 *             on parsing error
	 */
	public Revisions newPages( int amount, int namespace, int rcoptions, GregorianCalendar start ) throws IOException, ParserConfigurationException,
			SAXException {
		return newPages( amount, namespace, rcoptions, start, new GregorianCalendar() );
	}

	/**
	 * fetch and return new pages from a given time until the correct number of results are returned or the end time is
	 * reached.
	 * 
	 * @param amount
	 *            , number of results requested
	 * @param namespace
	 *            the namespace to search
	 * @param rcoptions
	 *            a bitmask of HIDE_ANON etc that dictate which pages we return (e.g. exclude patrolled pages =>
	 *            rcoptions = HIDE_PATROLLED).
	 * @param GregorianCalendar
	 *            start time
	 * @param GregorianCalendar
	 *            end time
	 * @return Revisions object, which contains the list of Revision objects and the rcstart continuation
	 * @throws IOException
	 *             on network error
	 * @throws ParserConfigurationException
	 *             on parsing error
	 * @throws SAXException
	 *             on parsing error
	 */
	public Revisions newPages( int amount, int namespace, int rcoptions, GregorianCalendar start, GregorianCalendar end ) throws IOException,
			ParserConfigurationException, SAXException {
		StringBuilder url = new StringBuilder( query );
		url.append( "action=query&list=recentchanges&rcprop=title%7Cids%7Cuser%7Ctimestamp%7Cflags%7Ccomment&rclimit=max&rcdir=newer&rctype=new&rcend=" );
		url.append( calendarToTimestamp( end ) );

		if ( namespace != ALL_NAMESPACES ) {
			url.append( "&rcnamespace=" );
			url.append( namespace );
		}
		// rc options
		if ( rcoptions > 0 ) {
			url.append( "&rcshow=" );
			if ( ( rcoptions & HIDE_ANON ) == HIDE_ANON )
				url.append( "!anon%7C" );
			if ( ( rcoptions & HIDE_SELF ) == HIDE_SELF )
				url.append( "!self%7C" );
			if ( ( rcoptions & HIDE_MINOR ) == HIDE_MINOR )
				url.append( "!minor%7C" );
			if ( ( rcoptions & HIDE_PATROLLED ) == HIDE_PATROLLED )
				url.append( "!patrolled%7C" );
			if ( ( rcoptions & HIDE_BOT ) == HIDE_BOT )
				url.append( "!bot" );
			// chop off last |
			url.delete( url.length() - 3, url.length() );
		}

		// fetch, parse
		url.append( "&rcstart=" );
		String rcstart = calendarToTimestamp( start );

		ArrayList<Revision> revisions = new ArrayList<Revision>( amount );
		do {
			String temp = url.toString();
			String line = fetch( temp + rcstart, "newPages", false );

			// DOM XML parser
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse( new ByteArrayInputStream( line.getBytes( "UTF-8" ) ) );

			Element docEle = dom.getDocumentElement();
			rcstart = parseRecentChangesDocument( docEle, revisions, amount );

			log( Level.INFO, "revsize: " + revisions.size() + ", amount: " + amount + ", rcstart: " + rcstart, "recentChangesFFF" );

		} while ( amount > revisions.size() && rcstart != null && rcstart.length() > 0 );

		return new Revisions( revisions, rcstart );
	}

	/**
	 * Parse the DOM tree associated with a Recent Changes listing.
	 * 
	 * @param docEle
	 * @param revisions
	 * @param amount
	 * @return rcstart string for our next set of results
	 */
	protected String parseRecentChangesDocument( Element docEle, ArrayList<Revision> revisions, int amount ) {
		NodeList rcitems = docEle.getElementsByTagName( "recentchanges" );

		OUTER: for ( int j = 0; j < rcitems.getLength(); ++j ) {
			Node rcitem = rcitems.item( j );
			NodeList items = rcitem.getChildNodes();
			String parent = rcitem.getParentNode().getNodeName();
			if ( !parent.equalsIgnoreCase( "query" ) ) {
				continue;
			}

			for ( int i = 0; i < items.getLength(); ++i ) {
				Node nodeItem = items.item( i );

				Revision rev = parseRevision( (Element) nodeItem );
				log( Level.INFO, "have rev: " + rev.getPage(), "parseRecentChangesDocument" );

				revisions.add( rev );
				if ( revisions.size() >= amount ) {
					log( Level.INFO, "have enough revs", "parseRecentChangesDocument" );
					break OUTER;
				}
			}
		}

		return parseQueryContinue( docEle.getElementsByTagName( "query-continue" ) );
	}

	/**
	 * parse the query-continue section of DOM, which is where the next rcstart value is.
	 * 
	 * @param qcitems
	 *            , a NodeList starting with the query-continue element(s).
	 * @return the rcstart String, parsed to a timestamp
	 */
	protected String parseQueryContinue( NodeList qcitems ) {
		String nextStart = null;

		OUTER: for ( int j = 0; j < qcitems.getLength(); ++j ) {
			Node rcitem = qcitems.item( j );
			NodeList items = rcitem.getChildNodes();

			for ( int i = 0; i < items.getLength(); ++i ) {
				Node nodeItem = items.item( i );
				String attribute = ( (Element) nodeItem ).getAttribute( "rcstart" );
				if ( attribute != null && attribute.length() > 0 ) {
					nextStart = convertTimestamp( attribute );
					log( Level.INFO, "query-continue rcstart: " + nextStart, "parseQueryContinue" );
					break OUTER;
				}
			}
		}
		return nextStart;
	}

	/**
	 * Fetches the <tt>amount</tt> most recent changes in the main namespace. WARNING: The recent changes table only
	 * stores new pages for about a month. It is not possible to retrieve changes before then. Equivalent to
	 * [[Special:Recentchanges]].
	 * <p>
	 * Note: Log entries in recent changes have a revid of 0!
	 * 
	 * @param amount
	 *            the number of entries to return
	 * @return the recent changes that satisfy these criteria
	 * @throws IOException
	 *             if a network error occurs
	 * @since 0.23
	 */
	public Revision[] recentChanges( int amount ) throws IOException {
		return recentChanges( amount, MAIN_NAMESPACE, 0, false );
	}

	/**
	 * Turns a calendar into a timestamp of the format yyyymmddhhmmss. Might be useful for subclasses.
	 * 
	 * @param c
	 *            the calendar to convert
	 * @return the converted calendar
	 * @see #timestampToCalendar
	 * @since 0.08
	 */
	protected String calendarToTimestamp( Calendar c ) {
		return String.format( "%04d%02d%02d%02d%02d%02d", c.get( Calendar.YEAR ), c.get( Calendar.MONTH ) + 1, c.get( Calendar.DAY_OF_MONTH ),
				c.get( Calendar.HOUR_OF_DAY ), c.get( Calendar.MINUTE ), c.get( Calendar.SECOND ) );
	}

	/**
	 * Stores a list of revisions and the rcstart for a continuation.
	 */
	public class Revisions {
		List<Revision> revs = null;
		String rcstart = null;

		public Revisions( List<Revision> revs, String rcstart ) {
			this.revs = revs;
			this.rcstart = rcstart;
		}

		public List<Revision> getRevisionList() {
			return revs;
		}

		public String getRcStart() {
			return rcstart;
		}
	}
}
