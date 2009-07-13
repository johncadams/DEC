package com.smadanhoj.dec.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.smadanhoj.dec.mp3.MyMP3File;
import com.smadanhoj.dec.mp3.MyMP3File.Id3Exception;
import com.smadanhoj.dec.mp3.MyMP3File.Mp3Exception;


public class DB {
	private final static   String DRIVER          = com.hxtt.sql.dbf.DBFDriver.class.getName();
	private final static   String URL             = System.getProperty("URL", "jdbc:DBF:////content/db");
	
	private static final String GetSequenceIdSql  = "SELECT * FROM SequenceNumber WHERE \"table\"=?";
	private static final String IncrSequenceIdSql = "UPDATE SequenceNumber SET sequence=? WHERE \"table\"=?";
	private static final String ReindexAll        = "REINDEX \"{ALL}\" ON %s";		
	
	
	protected static final String AmgRelativeTable          = "AMGRelative";
	protected static final String AlbumTable                = "Album";
	protected static final String ArtistTable               = "Artist";
	protected static final String AudioCDTable              = "AudioCD";
	protected static final String DownloadsTable            = "Downloads";
	protected static final String GenreTable                = "Genre";
	protected static final String LibraryTrackTable         = "LibraryTrack";
	protected static final String PlayListTable             = "PlayList";
	protected static final String PlayListItemTable         = "PlayListItem";
	protected static final String RadioCategoryTable        = "RadioCategory";
	protected static final String RadioFavoritesTable       = "RadioFavorites";
	protected static final String RadioLanguageTable        = "RadioLanguage";
	protected static final String RadioLocaleTable          = "RadioLocale";
	protected static final String RadioStationTable         = "RadioStation";
	protected static final String RadioStationCategoryTable = "RadioStationCategory";
	protected static final String RadioStationImageTable    = "RadioStationImage";
	protected static final String RemoteFilesTable          = "RemoteFiles";
	protected static final String RemoteHostsTable          = "RemoteHosts";
	protected static final String SequenceNumberTable       = "SequenceNumber";
		
	protected Connection connection;
	
	
	public DB() throws ClassNotFoundException,SQLException {	
		Class.forName(DRIVER);
		connection = DriverManager.getConnection(URL, "", "");
	}	
	
	
	public void close(boolean commit) {
		if (commit) {
			try { connection.commit();   } catch (SQLException ex) { }
			
		} else {
			try { connection.rollback(); } catch (SQLException ex) {}
		}
	}
	
	
	protected void dumpTable(String table) throws SQLException {
		Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * from "+table);
        this.toString(AmgRelativeTable, resultSet, false);
	}
	
	
	protected void createLibraryFile() throws SQLException {
		Statement statement = null;
        ResultSet resultSet = null;
        String    sql       = "SELECT LibraryTrack.TITLE,"      +
        		              "       Artist.NAME,"             +
        		              "       Album.NAME,"              +
        		              "       Genre.GENRE,"             +
        		              "       LibraryTrack.YEAR,"       +
        		              "       LibraryTrack.CD_TRK_NUM," +
        		              "       LibraryTrack.DURATION,"   +
        		              "       LibraryTrack.FILE"        +
        		              " FROM LibraryTrack"              +
                              " LEFT JOIN Artist ON LibraryTrack.ARTIST_ID = Artist.ID" +
                              " LEFT JOIN Album  ON LibraryTrack.ALBUM_ID  = Album.ID"  +
                              " LEFT JOIN Genre  ON LibraryTrack.GENRE_ID  = Genre.ID"  +
                              ";";
		try {
	        statement = connection.createStatement();
	        resultSet = statement.executeQuery(sql);

			int count = 0;
	        while (resultSet.next()) {
	        	String   title    = ""+resultSet.getObject("LibraryTrack.TITLE");
	        	String   artist   = ""+resultSet.getObject("Artist.NAME");
	        	String   album    = ""+resultSet.getObject("Album.NAME");
	        	String   genre    = ""+resultSet.getObject("Genre.GENRE");
	        	String   year     = ""+resultSet.getObject("LibraryTrack.YEAR");
	        	String   track    = ""+resultSet.getObject("LibraryTrack.CD_TRK_NUM");
	        	String   duration = ""+resultSet.getObject("LibraryTrack.DURATION");
	        	String   file     = ""+resultSet.getObject("LibraryTrack.FILE");
	        	String   comment  = "";
	        	String[] vals     = new String[]{title,artist,album,genre,year,track,duration,file,comment};
/*
	        	for (int i=0; i<vals.length; i++) {
	        		System.out.print(vals[i]+"|");
	        	}
	        	System.out.println("");

	        	vals = load(file);
*/	        	
	        	System.out.print(++count +") ");
	        	for (int i=0; i<vals.length; i++) {
	        		System.out.print(vals[i]+"|");
	        	}
	        	System.out.println("");	        	        	
	        }
	        
		} finally {
			if (resultSet!=null) resultSet.close();
			if (statement!=null) statement.close();
		}
	}
	
	
	public boolean fixUnknown() throws SQLException,Id3Exception,Mp3Exception,IOException {
		Statement statement = null;
        ResultSet resultSet = null;
        boolean   commit    = false;
        String    sql       = "SELECT LibraryTrack.TITLE,"      +
        		              "       Artist.NAME,"             +
        		              "       Album.NAME,"              +
        		              "       Genre.GENRE,"             +
        		              "       LibraryTrack.YEAR,"       +
        		              "       LibraryTrack.CD_TRK_NUM," +
        		              "       LibraryTrack.DURATION,"   +
        		              "       LibraryTrack.FILE"        +
        		              " FROM LibraryTrack"              +
                              " LEFT JOIN Artist ON LibraryTrack.ARTIST_ID = Artist.ID" +
                              " LEFT JOIN Album  ON LibraryTrack.ALBUM_ID  = Album.ID"  +
                              " LEFT JOIN Genre  ON LibraryTrack.GENRE_ID  = Genre.ID"  +
                              " WHERE Genre.GENRE = 'Unknown'" +
                              ";";
		try {
	        statement = connection.createStatement();
	        resultSet = statement.executeQuery(sql);

	        while (resultSet.next()) {
	        	Object    path = resultSet.getObject("LibraryTrack.FILE");
	        	File      file = new File(""+path);
	        	MyMP3File mp3  = new MyMP3File(file);
	        	
	        	Logger.getLogger(getClass().getName()).info("Fixing: "+path);
	        	resultSet.updateString(LibraryTrackTable+".TITLE",      mp3.getTitle());	        	
	        	resultSet.updateString(ArtistTable      +".NAME",       mp3.getArtist());
	        	resultSet.updateString(AlbumTable       +".NAME",       mp3.getAlbum());
	        	resultSet.updateString(GenreTable       +".GENRE",      mp3.getGenre());
	        	resultSet.updateString(LibraryTrackTable+".YEAR",       mp3.getYear());
	        	resultSet.updateString(LibraryTrackTable+".CD_TRK_NUM", mp3.getTrack());	
	        	commit = true;
	        }
	        return commit;
	        
		} finally {
			if (resultSet!=null) resultSet.close();
			if (statement!=null) statement.close();
		}
	}
	
	
	protected long getNextId(String table) throws SQLException {
		PreparedStatement statement  = null;
		ResultSet         resultSet  = null;
		String            tableField = table.length()>10?table.substring(0,10):table;
		long              id         = -1;
				
		statement = connection.prepareStatement(GetSequenceIdSql);
		statement.setString(1, tableField);		
		resultSet = statement.executeQuery();
		if (resultSet.next()) {			
			id = resultSet.getLong("SEQUENCE");
System.err.println("Next/"+table+"["+id+"]");			
			statement = connection.prepareStatement(IncrSequenceIdSql);
			statement.setLong  (1, id+1);
			statement.setString(2, tableField);
			statement.executeUpdate();
		}
		
		return id;
	}

	
	public void reindex() throws SQLException {
		String[] tables = {AlbumTable,ArtistTable,LibraryTrackTable,GenreTable,PlayListTable,PlayListItemTable};		
        
		for (int i=0; i<tables.length; i++) {
        	Statement statement = connection.createStatement();
        	statement.executeUpdate(ReindexAll);
        }		
	}
	
	
	protected void toString(String label, ResultSet resultSet, boolean one) throws SQLException {
		ResultSetMetaData metaData  = resultSet.getMetaData();
		if (label!=null) System.out.println(label);
		while (resultSet.next()) {
			for (int x=1; x<=metaData.getColumnCount(); x++) {
				System.out.println(metaData.getColumnLabel(x)+": "+resultSet.getObject(x).toString());
			}
			if (one) break;
		}
	}
	
	
	protected void doSql(String sql) throws SQLException {
		Statement statement = null;
        ResultSet resultSet = null;
        
		try {
	        statement = connection.createStatement();
	        resultSet = statement.executeQuery(sql);
	        
	        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();	        
	        int numCols = resultSetMetaData.getColumnCount();
	        for (int i = 1; i <= numCols; i++) {
	            System.out.println(resultSetMetaData.getColumnLabel(i)
	                               + "  " +
	                               resultSetMetaData.getColumnTypeName(i));
	        }
	
	        Object colval;
	        while (resultSet.next()) {
	            for (int i = 1; i <= numCols; i++) {
	                colval = resultSet.getObject(i);
	                System.out.print(colval + "  ");
	            }
	            System.out.println();
	        }
		       
		} finally {
			if (resultSet!=null) resultSet.close();
			if (statement!=null) statement.close();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		DB db = new DB();
		try {
			db.fixUnknown();	
			
		} finally {
			db.close(true);
		}
	}
}