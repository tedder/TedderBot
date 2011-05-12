package net.inervo.TedderBot.ListedBuilding;

import java.net.URL;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			BuildingList bl = new BuildingList( new URL("http://www.britishlistedbuildings.co.uk/"), "United Kingdom");
			for (BuildingList blc : bl.getChildren() ) {
				print(blc.toString());
				
				for(BuildingList blcc : blc.getChildren() ) {
					print(blcc.toString());
				}
			}
//			bl.getChildren();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		print("success!");
	}

	private static void print(String string) {
		System.out.println(string);
	}

}
