package com.smadanhoj.dec;

import java.io.File;

import com.smadanhoj.dec.mp3.MyMP3File;


public class TagFixer extends Mp3Walker {

	public TagFixer(File[] files, boolean descend) {
		super(files, descend);
	}

	
	public void processFile(MyMP3File mp3File) {		
		try {
			fixGenre(mp3File);
			
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}
	
	
	private void fixGenre(MyMP3File mp3File) throws Exception {
		if (mp3File.hasId3v2()) {
			String genre1 = mp3File.getId3v1Genre();
			String genre2 = mp3File.getId3v2Genre();
			String genre  = null;

			// The 'root' ID3v1 genres (Ethnic/Jazz/Rock) are too big for DEC DB tools 
			// to wade through so instead we'll set their ID3v1 genre to nothing 
			// All other tools will always show the v2 tag.
			// THIS IS A HP DEC FIX
			if      (genre2.equals("Cajun"))     genre = ""; // Ethnic
			else if (genre2.equals("Zydeco"))    genre = ""; // Ethnic
			else if (genre2.equals("World"))     genre = ""; // Ethnic
			else if (genre2.equals("Dixieland")) genre = ""; // Jazz
			else if (genre2.equals("Ragtime"))   genre = ""; // Jazz
			else if (genre2.equals("Jam Band"))  genre = ""; // Rock

			if (genre!=null && !genre.equals(genre1)) {
				System.out.println(mp3File.getFilename()+": Genre '"+genre1+"'->'"+genre+"'("+genre2+")");
				mp3File.setId3v1Genre(genre);
				mp3File.writeId3v1Tag();
			}
		}
	}
	
	
	public static void main(String[] args) {
		boolean descend = false;
		if (args.length == 0) {
			System.err.println("TagFixer <path>[,<path>...]");
			System.exit(-1);
		}	
		
		if (args.length >1 && args[0].equals("-r")) {
			descend = true;
			System.arraycopy(args,1, args, 0, args.length-1);
		}
		
		File[] dirs = new File[args.length];		
		for (int i=0; i<args.length; i++) dirs[i] = new File(CWD, args[i]);
		
		TagFixer tagFixer = new TagFixer(dirs, descend);
		tagFixer.walkit();		
	}
}