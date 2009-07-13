package com.smadanhoj.dec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;


public class Checker {

	private static final boolean chkDuration = false;
	private static final boolean chkUrl      = true;
	
	public static void main(String[] args) throws Exception {
		URL        url1   = new File(args[0]).toURL();
		URL        url2   = new File(args[1]).toURL();
		HashMap    songs1 = load(url1);
		HashMap    songs2 = load(url2);	

		Iterator it = songs1.keySet().iterator();
		while (it.hasNext()) {
			Object  url   = it.next();
			Song    song1 = (Song)songs1.get(url);
			Song    song2 = (Song)songs2.get(url);
			boolean diff  = true;
			if (song2 != null) {
				diff = false                                                 ||
					                  !song1.title   .equals(song2.title   ) ||
				                      !song1.album   .equals(song2.album   ) ||
				                      !song1.artist  .equals(song2.artist  ) ||
				                      !song1.genre   .equals(song2.genre   ) ||
//				       chkDuration &&  song1.duration !=     song2.duration) ||
				                      !song1.track   .equals(song2.track   ) ||
				                      !song1.year    .equals(song2.year    ) ||
				                      !song1.comment .equals(song2.comment ) ||
				       chkUrl      && !song1.url     .equals(song2.url     ) ||
				       false;
				
				if (diff) {
					System.err.println(url);
					if (               !song1.title   .equals(song2.title   )) System.err.println("  Title: '"  +song1.title   +"'\n         '"  +song2.title+"'");
					if (               !song1.album   .equals(song2.album   )) System.err.println("  Album: '"  +song1.album   +"'\n         '"  +song2.album+"'");
					if (               !song1.artist  .equals(song2.artist  )) System.err.println("  Artist: '" +song1.artist  +"'\n          '" +song2.artist+"'");
					if (               !song1.genre   .equals(song2.genre   )) System.err.println("  Genre: '"  +song1.genre   +"'\n         '"   +song2.genre+"'");
					if (chkDuration &&  song1.duration !=     song2.duration ) System.err.println("  Time: "    +song1.duration+"'\n        '"    +song2.duration);
					if (               !song1.track   .equals(song2.track   )) System.err.println("  Track: '"  +song1.track   +"'\n         '"   +song2.track+"'");
					if (               !song1.year    .equals(song2.year    )) System.err.println("  Year: '"   +song1.year    +"'\n        '"    +song2.year+"'");
					if (               !song1.comment .equals(song2.comment )) System.err.println("  Comment: '"+song1.comment +"'\n           '"+song2.comment+"'");
					if (chkUrl      && !song1.url     .equals(song2.url     )) System.err.println("  URL: '"    +song1.url     +"'\n       '"    +song2.url+"'");
				}
				it.remove();
				songs2.remove(url);
			}
		}

		it = songs1.keySet().iterator();
		if (it.hasNext()) System.err.println("\nMissing from right:");
		while (it.hasNext()) {
			Object url = it.next();
			System.err.println("  "+url);
		}
		
		it = songs2.keySet().iterator();
		if (it.hasNext()) System.err.println("\nMissing from left:");
		while (it.hasNext()) {
			Object url = it.next();
			System.err.println("  "+url);
		}		
	}	


	private static HashMap load(URL libraryURL) throws IOException {
		HashMap            songs   = new HashMap();
		InputStream        stream  = null;
		LineNumberReader   reader  = null;
		String             line    = null;

		try {       			
			URLConnection conn   = libraryURL.openConnection();

			songs.clear();
			stream  = conn.getInputStream();
			reader  = new LineNumberReader( new InputStreamReader(stream) );
			while ((line=reader.readLine())!=null) {				
				Song            song   = new Song();
				String[]        elems  = line.split("\\|", 9);
				if (elems.length<9) {
					System.err.println("Too few columns, row: "+reader.getLineNumber());
					continue;
				}
				
				song.title    = elems[0];
				song.artist   = elems[1];
				song.album    = elems[2];
				song.genre    = elems[3];
				song.year     = elems[4];
				song.track    = elems[5];
				song.duration = Integer.parseInt(elems[6]);
				song.url      = URLDecoder.decode(elems[7], "ASCII");
				song.comment  = elems[8].trim();				
				songs.put(song.url, song);								
			}
			return songs;

		} finally {
			if (stream!=null) stream.close();
		}
	}


	private static class Song {
		String title;
		String artist;
		String album;
		String genre;
		String year;
		String track;
		int    duration;
		String url;
		String comment;		
	}
}