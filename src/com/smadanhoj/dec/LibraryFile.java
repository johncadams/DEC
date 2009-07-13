package com.smadanhoj.dec;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.smadanhoj.dec.mp3.MyMP3File;


public class LibraryFile extends Mp3Walker {

	private static final String  PATH     = "/mcaster/web/";
	private static final boolean OldSkool = false;
	
	
	public LibraryFile(File[] files, boolean descend) {
		super(files, descend);
	}
	
	
	public void processFile(MyMP3File mp3File) throws UnsupportedEncodingException {
		String url = URLEncoder.encode(mp3File.getFilename(), "ASCII");
		// These are the character sequences the older Perl based
		// translation didn't handle. 
		// More importantly these characters show up in playlists.
		if (OldSkool) {
			url = url.replaceAll("\\+", "%20");
			url = url.replaceAll("%21", "!");
			url = url.replaceAll("%26", "&");
			url = url.replaceAll("%27", "'");
			url = url.replaceAll("%28", "(");
			url = url.replaceAll("%29", ")");											
			url = url.replaceAll("%2C", ",");				
		}
	
		System.out.print  (mp3File.getTitle()      +"|");
		System.out.print  (mp3File.getArtist()     +"|");
		System.out.print  (mp3File.getAlbum()      +"|");
		System.out.print  (mp3File.getGenre()      +"|");
		System.out.print  (mp3File.getYear()       +"|");
		System.out.print  (mp3File.getTrack()      +"|");
		System.out.print  (mp3File.getPlayingTime()+"|");
		System.out.print  (PATH+url                +"|");
		System.out.println(mp3File.getComment());
	}
	
	
	public static void main(String[] args) {
		boolean descend = false;
		if (args.length == 0) {
			System.err.println("Library <path>[,<path>...]");
			System.exit(-1);
		}		
		
		if (args.length >1 && args[0].equals("-r")) {
			descend = true;
			System.arraycopy(args,1, args, 0, args.length-1);
		}
		
		File[] dirs = new File[args.length];			
		for (int i=0; i<args.length; i++) dirs[i] = new File(CWD, args[i]);
		
		LibraryFile library = new LibraryFile(dirs, descend);
		library.walkit();
	}
}