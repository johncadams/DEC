package com.smadanhoj.dec.mp3;


public interface MP3FileInterface {
	public String  getAlbum      ();
	public String  getArtist     ();
	public String  getComment    ();
	public String  getGenre      ();
	public String  getTitle      ();
	public String  getTrack      ();
	public String  getYear       ();
	public String  getFilename   ();
	public long    getPlayingTime();
	public boolean hasId3v1      ();
	public boolean hasId3v2      ();
}