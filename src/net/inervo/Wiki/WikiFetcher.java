package net.inervo.Wiki;

public interface WikiFetcher {
	public String getPageText( String articleName ) throws Exception;
}
