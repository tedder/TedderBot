package net.inervo.TedderBot;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;

public class User {
	private String user;
//	private WikiAccessor accessor;
//
//	public User( WikiAccessor accessor ) {
//		this.accessor = accessor;
//	}

	public User( String username ) {
		this.user = username;
	}

	public String getUsername() {
		return user;
	}

	public AbstractCollection<Revision> getContribs() throws IOException {
//		unknown.wiki.Wiki.Revision[] revs = accessor.contribs( user );
		return null;
	}

	public static void main( String[] args ) {
		User u = new User( "Tedder" );
		Collection<Revision> revs;
		try {
			revs = u.getContribs();

			if ( revs != null ) {
				for ( Revision r : revs ) {
					print( "revision: " + r );
					break;
				}
			}

		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void print( String string ) {
		System.out.println( string );
	}
}
