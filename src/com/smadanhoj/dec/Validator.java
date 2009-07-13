package com.smadanhoj.dec;

import java.io.File;
import java.lang.reflect.Method;

import com.smadanhoj.dec.mp3.MyMP3File;


public class Validator extends Mp3Walker {

	private boolean CheckDiff    = false;
	private boolean CheckMissing = false;
	private boolean CheckV1      = false;
	private boolean CheckV2      = false;
	private boolean CheckComment = false;
	private boolean CheckTrunc   = false;
	private boolean CheckArtwork = true;
	
	
	public Validator(File[] files, boolean descend) {
		super(files, descend);
	}

	
	public void processFile(MyMP3File mp3File) {
		try {
			boolean  flag  = false;	
			Class    klass = MyMP3File.class;
			String[] names = new String[]{
					"Album",
					"Artist",
					"Comment",
					"Genre",
					"Title",
					"Track",
					"Year",
			};			
						
			if (CheckDiff) {
				for (int i=0; i<names.length; i++) {
					String  name    = names[i];
					Method  method  = klass.getDeclaredMethod("get"     +name, null);
					Method  method1 = klass.getDeclaredMethod("getId3v1"+name, null);
					Method  method2 = klass.getDeclaredMethod("getId3v2"+name, null);
					Object  value   = method .invoke(mp3File, null);
					Object  value1  = method1.invoke(mp3File, null);
					Object  value2  = method2.invoke(mp3File, null);
					boolean same;
					
					if (name.equals("Track")) {
						int num1 = ((Integer)value1).intValue();
						int num2 = ((Integer)value2).intValue();
						if      (num2 == -1)                 same = (num1 == 0);
						else                                 same = value1.equals(value2);
						
						if (mp3File.getId3v2TrackStr().contains("/")) {
							if (!flag) System.err.println(mp3File.getFilename());
							System.err.println("  "+name+": Contains '/'");
							flag = true;
						}
						
						if (mp3File.getId3v2Disc()!=null && mp3File.getId3v2Disc().length()>0) {
							if (!flag) System.err.println(mp3File.getFilename());
							System.err.println("  Disc"+": "+mp3File.getId3v2Disc());
							flag = true;
						}
						
					} else if (CheckTrunc && name.equals("Title")) {
						String title1 = value1.toString();
						String title2 = value2.toString();
						if      (title1.length()==30)        same = title2.length()>30 && title2.startsWith(title1);
						else                                 same = value1.equals(value2);
																		
					} else if (name.equals("Genre")) {
						if      (value2.equals("Folk/Rock")) same = value1.equals("Folk-Rock");
						else if (value2.equals("Cajun"))     same = value1.equals("");
						else if (value2.equals("Zydeco"))    same = value1.equals("");
						else if (value2.equals("World"))     same = value1.equals("");
						else if (value2.equals("Dixieland")) same = value1.equals("");
						else if (value2.equals("Ragtime"))   same = value1.equals("");						
						else if (value2.equals("Jam Band"))  same = value1.equals("");
						else                                 same = value1.equals(value2);
						
					} else if (name.equals("Comment")) {
						String comm1 = value1.toString().trim();
						String comm2 = value2.toString().trim();						
						if (!CheckComment)                   same = true;						
						else                                 same = comm2.startsWith(comm1);
						
					} else if (value1 instanceof String) {						
						same = ((String)value2).startsWith((String)value1);
						
					} else {
						same = value1.equals(value2);
					}
					
					
					if (!name.equals("Comment") && value instanceof String && ((String)value).trim().length()==0) {
						if (CheckMissing) {
							if (!flag) System.err.println(mp3File.getFilename());
							System.err.println("  "+name+": MISSING");
							flag = true;
						}
						
					} else if (!same && mp3File.hasId3v2()) {
						if (!flag) System.err.println(mp3File.getFilename());
						System.err.println("  "+name+": '"+value+"'");
						System.err.println("    '"+value1+"'");
						System.err.println("    '"+value2+"'");
						flag = true;
					}											
				}
			}
			
			if (CheckV1 && !mp3File.hasId3v1()) {
				if (!flag)  System.err.println(mp3File.getFilename());
				System.err.println("  NO ID3V1 TAG");
				flag = true;
			}
			
			if (CheckV2 && !mp3File.hasId3v2()) {
				if (!flag)  System.err.println(mp3File.getFilename());
				System.err.println("  NO ID3V2 TAG");
				flag = true;
			}
			
			if (CheckArtwork && mp3File.hasId3v2()) {				
				int count = mp3File.getId3v2Artwork();
				if (count>1) {
					if (!flag)  System.err.println(mp3File.getFilename());
					System.err.println("  Multiple artworks");
					flag = true;
				}
			}

		} catch (Exception ex) {
			System.err.println(ex);
			System.exit(-1);
		}
	}
	
	
	public static void main(String[] args) {
		boolean descend = false;
		if (args.length == 0) {
			System.err.println("Validator <path>[,<path>...]");
			System.exit(-1);
		}	
		
		if (args.length >1 && args[0].equals("-r")) {
			descend = true;
			System.arraycopy(args,1, args, 0, args.length-1);
		}
		
		File[] dirs = new File[args.length];		
		for (int i=0; i<args.length; i++) dirs[i] = new File(CWD, args[i]);
		
		Validator validator = new Validator(dirs, descend);
		validator.walkit();		
	}
}