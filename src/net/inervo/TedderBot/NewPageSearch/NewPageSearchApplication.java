package net.inervo.TedderBot.NewPageSearch;

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
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.inervo.WMFWiki11;
import net.inervo.TedderBot.BotFlag;
import net.inervo.TedderBot.Configuration;
import net.inervo.Wiki.PageEditor;
import net.inervo.Wiki.RetryEditor;
import net.inervo.Wiki.WikiFetcher;
import net.inervo.Wiki.WikiHelpers;
import net.inervo.Wiki.Cache.ArticleCache;
import net.inervo.Wiki.Cache.CachedFetcher;

public class NewPageSearchApplication {
	private static final long DAY_IN_MILLISECONDS = 86400000;
	// consider this an "oversearch" period.
	public static final int PREPEND_SEARCH_DAYS = -7;
	private static final Logger logger = Logger.getLogger(NewPageSearchApplication.class.getCanonicalName()); // only

	private static final String DEBUG_SEARCH = "Astro";

	public static void main( String[] args ) throws Exception
	{
		if ( args.length < 2 ) {
			print( "need params given in this order: AWS prop file, wiki prop file" );
		}

		for (Handler h : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getHandlers()) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).removeHandler(h);
		}
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(new FileHandler("newpagesearch.log"));

		// shutdown hook. Enable early so we don't blow up the cache.
		System.setProperty("net.sf.ehcache.enableShutdownHook", "true");

		print("hello world!");
		ArticleCache ac = null;

		String amazonProps = "AwsCredentials.properties";
		String wikiProps = "wiki.properties";

		if ( args.length >= 2 ) {
			amazonProps = args[0];
			wikiProps = args[1];
		}

		try {
<<<<<<< HEAD
			NewPageSearchApplication npsa = new NewPageSearchApplication();
			PersistentKeystore.initialize(npsa.getClass().getResourceAsStream("/AwsCredentials.properties"));
			Configuration config = new Configuration(npsa.getClass().getResourceAsStream("/wiki.properties"));
=======
			PersistentKeystore.initialize( amazonProps );
			Configuration config = new Configuration( wikiProps );
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf

			WMFWiki11 wiki = new WMFWiki11("en.wikipedia.org");
			wiki.setMaxLag(15);
			wiki.setLogLevel(Level.WARNING);

			// wiki.setThrottle( 5000 );
			wiki.login(config.getWikipediaUser(), config.getWikipediaPassword().toCharArray());
			print("db lag (seconds): " + wiki.getCurrentDatabaseLag());
			BotFlag.check(wiki);

			ac = new ArticleCache(wiki);
			WikiFetcher fetcher = new CachedFetcher(ac);

			PageRules rules = new PageRules(fetcher, "User:AlexNewArtBot/Master", debugSearch);

			PageEditor editor = new RetryEditor(wiki);

			String lastProcessed = PersistentKeystore.get(PersistentKeystore.DEFAULT_KEY,
					PersistentKeystore.LAST_PROCESSED);
			print("last processed: " + lastProcessed);

			List<PageRule> ruleList = rules.getRules();
			Comparator<PageRule> sorter = new SortRulesByRuleNameAlpha();
			if (lastProcessed != null) {
				sorter = new SortRulesByRuleNameAlphaOnPivot(lastProcessed);
			}
			Collections.sort(ruleList, sorter);

			NewPageFetcher npp = new NewPageFetcher(wiki, fetcher, editor);

			for (PageRule rule : ruleList) {
				BotFlag.check(wiki);

				String searchName = rule.getSearchName();

<<<<<<< HEAD
				print("processing rule "
						+ searchName
						+ ", current time: "
						+ WikiHelpers.calendarToTimestamp(new GregorianCalendar(TimeZone
								.getTimeZone("America/Los_Angeles"))));
=======
				print( "processing rule " + searchName + ", current time: "
					+ WikiHelpers.calendarToTimestamp( new GregorianCalendar( TimeZone.getTimeZone( "America/Los_Angeles" ) ) ) );
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf
				long startClock = System.currentTimeMillis();

				// store it before we run. That way we'll begin at n+1 even if this one frequently fails.
				PersistentKeystore.put(PersistentKeystore.DEFAULT_KEY, PersistentKeystore.LAST_PROCESSED,
						rule.getSearchName(), true);

				String startTime = getStartTime(searchName);
				if (!isDeltaGreaterThanOneDay(WikiHelpers.timestampToCalendar(startTime), new GregorianCalendar())) {
					print("rule " + rule.getSearchName() + " has been run recently, skipping.");
					continue;
				}

				String endTime = npp.runFetcher(startTime, rule);
				storeStartTime(searchName, endTime);

				long endClock = System.currentTimeMillis();
				print("done processing rule " + searchName + ", time: "
						+ deltaMillisecondsToString(endClock - startClock));

			}

		} finally {
			if (ac != null) {
				ac.shutdown();
			}
		}
	}

	public static class SortRulesByRuleNameAlphaOnPivot implements Comparator<PageRule> {
		private String pivot;

<<<<<<< HEAD
		public SortRulesByRuleNameAlphaOnPivot(String pivot) {
=======
		public SortRulesByRuleNameAlphaOnPivot( String pivot )
		{
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf
			this.pivot = pivot.toLowerCase();
		};

		@Override
<<<<<<< HEAD
		public int compare(PageRule arg0, PageRule arg1) {
			int zeroComp = pivot.compareTo(arg0.getSearchName().toLowerCase());
			int oneComp = pivot.compareTo(arg1.getSearchName().toLowerCase());
			int directComp = arg0.getSearchName().toLowerCase().compareTo(arg1.getSearchName().toLowerCase());
			// System.out.println(pivot + " - " + arg0.getSearchName() + " - " + arg1.getSearchName() + " == "
			// + zeroComp + " / " + oneComp + " / " + directComp + " / abs: " + (zeroComp * oneComp <= 0));

			if (zeroComp * oneComp <= 0) {
				// removed this, it broke the Comparator "contract" in Java7 or an update to Java6. Is it
				// needed for this to work? Hmm.
				// return Math.abs( directComp );
=======
		public int compare( PageRule arg0, PageRule arg1 )
		{
			int zeroComp = pivot.compareTo( arg0.getSearchName().toLowerCase() );
			int oneComp = pivot.compareTo( arg1.getSearchName().toLowerCase() );
			int directComp = arg0.getSearchName().toLowerCase().compareTo( arg1.getSearchName().toLowerCase() );

			if ( zeroComp * oneComp <= 0 ) {
				return Math.abs( directComp );
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf
			}

			return directComp;
		}
	}

<<<<<<< HEAD
	public static boolean isDeltaGreaterThanOneDay(Calendar obj1, Calendar obj2) {
		long delta = Math.abs(obj1.getTimeInMillis() - obj2.getTimeInMillis());
		if (delta > DAY_IN_MILLISECONDS) {
=======
	public static boolean isDeltaGreaterThanOneDay( Calendar obj1, Calendar obj2 )
	{
		long delta = Math.abs( obj1.getTimeInMillis() - obj2.getTimeInMillis() );
		if ( delta > DAY_IN_MILLISECONDS ) {
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf
			return true;
		}
		return false;
	}

<<<<<<< HEAD
	public static String deltaMillisecondsToString(long delta) {
		long deltaSeconds = (delta / 1000) % 60;
		long deltaMinutes = (deltaSeconds / 60) % 60;
		long deltaHours = (deltaMinutes / 60) % 60;
=======
	public static String deltaMillisecondsToString( long delta )
	{
		long deltaSeconds = ( delta / 1000 ) % 60;
		long deltaMinutes = ( deltaSeconds / 60 ) % 60;
		long deltaHours = ( deltaMinutes / 60 ) % 60;
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf

		return String.format("%d hours, %d minutes, %d seconds", deltaHours, deltaMinutes, deltaSeconds);
	}

	public static class SortRulesByRuleNameAlpha implements Comparator<PageRule> {
		public SortRulesByRuleNameAlpha()
		{
		};

		@Override
<<<<<<< HEAD
		public int compare(PageRule arg0, PageRule arg1) {
			return arg0.getSearchName().compareTo(arg1.getSearchName());
		}
	}

	private static void print(String s) {
		logger.log(Level.INFO, s);
	}

	public static String getStartTime(String searchName) throws FileNotFoundException, IllegalArgumentException,
			IOException {
		String startTime = PersistentKeystore.get(searchName, "lastRunTime");
=======
		public int compare( PageRule arg0, PageRule arg1 )
		{
			return arg0.getSearchName().compareTo( arg1.getSearchName() );
		}
	}

	private static void print( String s )
	{
		logger.log( Level.INFO, s );
	}

	public static String getStartTime( String searchName ) throws FileNotFoundException, IllegalArgumentException, IOException
	{
		String startTime = PersistentKeystore.get( searchName, "lastRunTime" );
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf

		if (startTime == null || startTime.isEmpty()) {
			startTime = getDefaultStartTime();
		} else {
			Calendar start = WikiHelpers.timestampToCalendar(startTime);
			start.add(Calendar.DAY_OF_MONTH, PREPEND_SEARCH_DAYS);
			startTime = WikiHelpers.calendarToTimestamp(start);
		}

		return startTime;
	}

<<<<<<< HEAD
	public static void storeStartTime(String searchName, String lastStamp) {
		PersistentKeystore.put(searchName, "lastRunTime", lastStamp, true);
	}

	public static String getDefaultStartTime() throws FileNotFoundException, IllegalArgumentException, IOException {
		String startTime = PersistentKeystore.get("default", "lastRunTime");
=======
	public static void storeStartTime( String searchName, String lastStamp )
	{
		PersistentKeystore.put( searchName, "lastRunTime", lastStamp, true );
	}

	public static String getDefaultStartTime() throws FileNotFoundException, IllegalArgumentException, IOException
	{
		String startTime = PersistentKeystore.get( "default", "lastRunTime" );
>>>>>>> 270457b2f36dc965756fce6b01e09a59e4f1a5cf

		Calendar start = null;
		if (startTime == null || startTime.isEmpty()) {
			// if we didn't have the key in our keystore, use a default of today minus our padding.
			start = new GregorianCalendar();
		} else {
			start = WikiHelpers.timestampToCalendar(startTime);
		}

		// pad back the start time.
		start.add(Calendar.DAY_OF_MONTH, PREPEND_SEARCH_DAYS);

		// given our Calendar object, get a String (again).
		startTime = WikiHelpers.calendarToTimestamp(start);

		return startTime;
	}

}
