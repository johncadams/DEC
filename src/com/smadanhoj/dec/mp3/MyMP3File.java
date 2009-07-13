package com.smadanhoj.dec.mp3;

import helliker.id3.CorruptHeaderException;
import helliker.id3.ID3FieldDataException;
import helliker.id3.ID3v1Tag;
import helliker.id3.ID3v2FormatException;
import helliker.id3.ID3v2Tag;
import helliker.id3.MPEGAudioFrameHeader;
import helliker.id3.NoMPEGFramesException;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;


public class MyMP3File implements MP3FileInterface {
	private File                 file;
	private MPEGAudioFrameHeader head;
	private ID3v1Tag             id3v1;
	private ID3v2Tag             id3v2;


	public MyMP3File(File file) throws Id3Exception,Mp3Exception,IOException {	    
		try {
			this.file  = file;
			this.head  = new MPEGAudioFrameHeader(file, 0);
			this.id3v1 = new ID3v1Tag(file);
			this.id3v2 = new ID3v2Tag(file, head.getLocation() );

		} catch (EOFException ex) {
			if (this.head == null) throw new NotAnMp3Exception();
			else                   throw ex;
			
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);

		} catch (NoMPEGFramesException ex) {
			throw new Mp3Exception(ex);

		} catch (CorruptHeaderException ex) {
			throw new Id3Exception(ex);	
		}
	}

	public String getAlbum() {
		String str = null;
		try {
			str = getId3v2Album();
		} catch (Id3Exception e1) {		
		}

		if (str==null || str.length()==0) {
			str = getId3v1Album();			
		}
		return str;
	}

	public String getArtist() {
		String str = null;
		try {
			str = getId3v2Artist();
		} catch (Id3Exception e) {			
		}

		if (str==null || str.length()==0) {
			str = getId3v1Artist();
		}
		return str;
	}

	public String getComment() {
		String str = null;
		try {
			str = getId3v2Comment();
		} catch (Id3Exception e) {			
		}

		if (str==null || str.length()==0) {
			str = getId3v1Comment();
		}
		return str;
	}
	
	public String getFilename() {
		return file.getName();
	}

	public String getGenre() {
		String str = null;
		try {
			str = getId3v2Genre();
			// Fix up the genre string
			if (str.startsWith("(") && str.length()>4) {
				str = str.replaceFirst("^\\([0-9]+\\)", "");			
			}
		} catch (Id3Exception e) {			
		}

		if (str==null || str.length()==0) {
			str = getId3v1Genre();
		}		
		return str;
	}

	public String getTitle() {
		String str = null;
		try {
			str = getId3v2Title();
		} catch (Id3Exception e) {			
		}

		if (str==null || str.length()==0) {
			str = getId3v1Title();
		}
		return str;
	}

	public String getTrack() {
		int num = -1;
		try {
			num = getId3v2Track();
		} catch (Id3Exception e) {			
		}

		if (num==-1) {
			num = getId3v1Track();
			if (num==0) return "";
		}
		
		return ""+num;		
	}

	public String getYear() {
		String str = null;
		try {
			str = getId3v2Year();
		} catch (Id3Exception e) {			
		}

		if (str==null || str.length()==0) {
			str = getId3v1Year();
		}
		return str;
	}

	public long getPlayingTime() {
		if (head.isVBR()) {
			return head.getVBRPlayingTime();

		} else {
			long datasize = (file.length() * 8) - id3v2.getSize() - id3v1.getSize();
			long bps      = head.getBitRate() * 1000;

			if (bps == 0) return 0;
			else          return datasize / bps;
		}
	}
	
	
	public String getLabel() {
		return getArtist() +" - "+ getTitle();
	}
	
	public boolean hasId3v1() {
		return id3v1.tagExists();
	}
	
	
	public boolean hasId3v2() {
		return id3v2.tagExists();
	}


	public String getId3v1Album() {
		return id3v1.getAlbum();	   
	}

	public String getId3v1Artist() {	
		return id3v1.getArtist();
	}

	public String getId3v1Comment() {
		return id3v1.getComment();
	}

	public String getId3v1Genre() {	
		return id3v1.getGenreString();
	}
	
	public void setId3v1Genre(String genre) {
		if (genre==null || genre.length()==0) {
			try {
				id3v1.setGenre(255);
				return;
			} catch (ID3FieldDataException e) { }
		}
		
		id3v1.setGenreString(genre);
	}

	public String getId3v1Title() {	
		return id3v1.getTitle();
	}

	public int getId3v1Track() {		
		return id3v1.getTrack();
	}

	public String getId3v1Year() {		
		return id3v1.getYear();
	}


	public String getId3v2Album() throws Id3Exception {
		try {
			return id3v2.getFrameDataString("TALB");
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}

	public String getId3v2Artist() throws Id3Exception {	
		try {
			return id3v2.getFrameDataString("TPE1");
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}

	public String getId3v2Comment() throws Id3Exception {
		try {
			String comment = id3v2.getFrameDataString("COMM");
			byte[] bytes   = comment.getBytes();
			if (bytes.length>0 && bytes[0]==0) comment = comment.substring(1);
			return comment;
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}

	public String getId3v2Genre() throws Id3Exception {	
		try {
			return id3v2.getFrameDataString("TCON");
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}

	public String getId3v2Title() throws Id3Exception {	
		try {
			return id3v2.getFrameDataString("TIT2");
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}

	public int getId3v2Track() throws Id3Exception {				
		String str   = getId3v2TrackStr();
		int    track = -1;
		int    loc   = str.indexOf("/");

		try {
			if (loc != -1) {
				str = str.substring(0, loc);
			}
			track = Integer.parseInt(str);
		} catch (NumberFormatException e) {	     
		}

		return track;
	}
	
	public String getId3v2TrackStr() throws Id3Exception {
		try {
			return id3v2.getFrameDataString("TRCK");
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}
	
	public String getId3v2Disc() throws Id3Exception {
		try {
			return id3v2.getFrameDataString("TPOS");			
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}		
	}

	public String getId3v2Year() throws Id3Exception {		
		try {
			return id3v2.getFrameDataString("TYER");
		} catch (ID3v2FormatException ex) {
			throw new Id3Exception(ex);
		}
	}
	
	public int getId3v2Artwork() throws Id3Exception {
		return id3v2.getFrameData2("APIC");
	}
	
	public void writeId3v1Tag() throws IOException {
		id3v1.writeTag();
	}



	public static class Id3Exception extends Exception {
		Id3Exception(ID3v2FormatException ex) {
			super(ex);
		}
	
		Id3Exception(CorruptHeaderException ex) {
			super(ex);
		}
	}
	
	
	
	public static class Mp3Exception extends Exception {
		Mp3Exception(NoMPEGFramesException ex) {
			super(ex);
		}
		
		protected Mp3Exception() {
			super();
		}
	}
	
	
	
	public static class NotAnMp3Exception extends Mp3Exception {
		NotAnMp3Exception() {
			super();
		}
	}
}