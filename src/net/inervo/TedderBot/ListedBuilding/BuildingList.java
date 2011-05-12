package net.inervo.TedderBot.ListedBuilding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildingList {
	private String category = "";
	private URL listingUrl = null;
	private ArrayList<BuildingList> children = new ArrayList<BuildingList>();

	public BuildingList(URL listingUrl, String category) {
		this.setListingUrl(listingUrl);
		this.category = category;
	}

	public ArrayList<BuildingList> getChildren() throws Exception {
		if (! children.isEmpty() ) { return children; }
		
		System.err.println("about to look at URL: " + listingUrl.toExternalForm() );
		BufferedReader in = new BufferedReader(new InputStreamReader(listingUrl.openStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
//			System.err.println("line: " + inputLine);
			Matcher entryMatcher = Pattern.compile("<h3><a href=\"(\\S+?)\">Listed buildings in (.*?)</a></h3>|<li><a href='(\\S+?)'>(.*?)</a>").matcher(inputLine);
			while (entryMatcher.find()) {
				String urlSegment = entryMatcher.group(1);
				if (urlSegment == null || urlSegment.length() == 0) { urlSegment = entryMatcher.group(3); }
				String label = entryMatcher.group(2);
				if (label == null || label.length() == 0) { label = entryMatcher.group(4); }
				
				URL url = new URL( this.listingUrl.getProtocol(), this.listingUrl.getHost(), this.listingUrl.getPort(), urlSegment );

				
				BuildingList currChild = new BuildingList(url, label);
				children.add(currChild);
			}
//			System.out.println(inputLine);
		}

		in.close();

		return children;
	}

	private BuildingList getListingEntry(String inputLine) throws MalformedURLException {
		Matcher entryMatcher = Pattern.compile("<h3><a href=\"(\\S+?)\">Listed buildings in (.*?)</a></h3>").matcher(inputLine);
		if (entryMatcher.find()) {
			URL url = new URL( this.listingUrl.getProtocol(), this.listingUrl.getHost(), this.listingUrl.getPort(), entryMatcher.group(1) );
			String label = new String(entryMatcher.group(2));
			return new BuildingList(url, label);
//			System.out.println( entryMatcher.group(1) + " " + entryMatcher.group(2) );
		}
		
		return null;
	}
	
	public String toString() {
		return category + ":" + this.listingUrl.toExternalForm();
	}

	/*
	 * Getters and setters
	 */

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setListingUrl(URL listingUrl) {
		this.listingUrl = listingUrl;
	}

	public URL getListingUrl() {
		return listingUrl;
	}

}
