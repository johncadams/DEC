package com.smadanhoj.dec.mp3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;


public class M3UFile extends Vector {
	private static final Logger logger = Logger.getLogger(M3UFile.class.getName());
	
	public M3UFile(URL url, String relocate, String to) throws IOException {
		InputStream       stream = null;
		InputStreamReader isr; 
		LineNumberReader  reader;	

		try {
			logger.fine("Loading: "+url);
			
			stream = url.openStream();
			isr    = new InputStreamReader(stream); 
			reader = new LineNumberReader (isr);
			
			String line;				
			while ( (line=reader.readLine()) != null) {
				if (line.startsWith("#EXTM3U")) {
					// Ignore the first line
					
				} else if (line.startsWith("#EXTINF")) {
					// Ignore the title/length line
					// int    ndx      = line.indexOf(",");
					// String duration = line.substring(8,ndx);
					// String title    = line.substring(ndx+1);
					
				} else if (line.startsWith("#")) {
					
				} else {
					line = URLDecoder.decode(line, "UTF-8");
					if (relocate!=null) line = line.replaceAll(relocate, to);
					
					File file = new File(line);
					if (file.exists()) {
						try {			
							MyMP3File mp3 = new MyMP3File(file);							
							super.add(mp3);
							logger.fine("Adding MP3: "+mp3.getLabel());
							
						} catch (Exception ex) {
							logger.warning("Cannot load: "+line);
						}
					}					
				}
			}
			
		} finally {
			if (stream!=null) try { stream.close(); } catch(Exception ex) {}
		}
	}
	
	
	public static void generate(PrintWriter writer, List songs, URL baseURL) throws IOException {						
		try {
			writer.print("#EXTM3U\n");
			for (int i=0; i<songs.size(); i++) {
				MyMP3File mp3 = (MyMP3File)songs.get(i);
				writer.println("#EXTINF:" +mp3.getPlayingTime() +"," +mp3.getLabel());
				writer.println(mp3.getFilename());
			}
		} finally {
			writer.close();
		}
	}
}