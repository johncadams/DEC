package com.smadanhoj.dec.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class Hacker {
	public static void main(String[] args) throws Exception {
//		String      jdbc   = "DBF_JDBC30/"+com.hxtt.global.SQLState.class.getName();
		String      hacker = "build/"+Constants.class.getName();
		String      name   = hacker;
		File        file   = new File(name.replace(".", "/")+".class");
		InputStream is     = new FileInputStream(file);
		int         cnt    = -2;
		boolean     found  = false;
		int         delta  = 59;
		int         a      = 535;
		int         b      = a + delta;		
		int         c      = b + delta;
		int         d      = c + delta;
		int         e      = d + delta;
		int         f      = e + delta;
		byte[]      bites  = new byte[2];
		int         b7     = Integer.parseInt("b7", 16);
		
		while (is.available() > 0) {						
			cnt+=2;				
			is.read(bites);

			if (cnt == a) System.err.println(Constants.TwentyFive()+") "+"25/"+Integer.toHexString(25));
			if (cnt == b) System.err.println(Constants.Fifty()     +") "+"50/"+Integer.toHexString(50));
			if (cnt == c) System.err.println(Constants.FiftyOne()  +") "+"51/"+Integer.toHexString(51));
			if (cnt == d) System.err.println(Constants.Hundred27() +") "+"127/"+Integer.toHexString(127));
			if (cnt == e) System.err.println(Constants.Hundred28() +") "+"128/"+Integer.toHexString(128));
			if (cnt == f) System.err.println(Constants.Thousand()  +") "+"1000/"+Integer.toHexString(1000));

			if (bites[0]==Constants.TwentyFive() && bites[1]==b7) {
				System.err.println("Found "+Constants.TwentyFive()+": "+Integer.toHexString(cnt));
				found = true;
			} else if (bites[0]==Constants.Fifty() && bites[1]==b7) {
				System.err.println("Found "+Constants.Fifty()+": "+Integer.toHexString(cnt));
				found = true;
			} else if (bites[0]==Constants.FiftyOne() && bites[1]==b7) {
				System.err.println("Found "+Constants.FiftyOne()+": "+Integer.toHexString(cnt));
				found = true;
			} else if (bites[0]==Constants.Hundred27() && bites[1]==b7) {
				System.err.println("Found "+Constants.Hundred27()+": "+Integer.toHexString(cnt));
				found = true;
			} else if (bites[0]==Constants.Hundred28() && bites[1]==b7) {
				System.err.println("Found "+Constants.Hundred28()+": "+Integer.toHexString(cnt));
				found = true;	
			} else if (bites[0]==Constants.Thousand() && bites[1]==b7) {
				System.err.println("Found "+Constants.Thousand()+": "+Integer.toHexString(cnt));
				found = true;
			}
		}
		is.close();
		if (!found) System.err.println("Not found");
	}
	
	
	public static class Constants {
		protected static byte TwentyFive() {
			return new Integer(25).byteValue();
		}
		protected static byte Fifty() {
			return new Integer(50).byteValue();
		}
	
		protected static byte FiftyOne() {
			return new Integer(51).byteValue();
		}

		protected static byte Hundred27() {
			return new Integer(127).byteValue();
		}
		
		protected static byte Hundred28() {
			return new Integer(128).byteValue();
		}
		
		protected static byte Thousand() {
			return new Integer(1000).byteValue();
		}	
	}
}