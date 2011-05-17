package net.inervo.Wiki;


public interface PageEditor {
	public void edit( String title, String text, String summary, boolean minor ) throws Exception;
}
