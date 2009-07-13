package com.smadanhoj.dec;

import com.smadanhoj.dec.database.DB;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if (args.length==0) {
			usage();
		}
		
		String   arg0  = args[0];
		String[] nargs = new String[args.length-1];
		
		System.arraycopy(args, 1, nargs, 0, args.length-1); 
		if      (arg0.equalsIgnoreCase("DB"))        DB       .main(nargs);
		else if (arg0.equalsIgnoreCase("Library"))   LibraryFile  .main(nargs);
		else if (arg0.equalsIgnoreCase("Checker"))   Checker  .main(nargs);
		else if (arg0.equalsIgnoreCase("TagFixer"))  TagFixer .main(nargs);
		else if (arg0.equalsIgnoreCase("Validator")) Validator.main(nargs);
		else                                         usage();
	}
	
	
	private static void usage() {
		System.err.println("Usage:");
		System.err.println("  DB        <sql>,<url>");
		System.err.println("  Checker   <library>,<library>");
		System.err.println("  Library   <path>[,<path>...]");		
		System.err.println("  TagFixer  <path>[,<path>...]");
		System.err.println("  Validator <path>[,<path>...]");
		System.exit(-1);
	}
}