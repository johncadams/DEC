package com.smadanhoj.dec.database;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.smadanhoj.dec.mp3.M3UFile;
import com.smadanhoj.dec.mp3.MyMP3File;


public class Playlist extends DB {
	private static final String FindLibraryTrackByFileSql= "SELECT *  FROM LibraryTrack WHERE file LIKE ?";
	private static final String FindPlaylistByNameSql    = "SELECT *  FROM PlayList WHERE name=?";
	private static final String FindPlaylistByIdSql      = "SELECT *  FROM PlayList WHERE id=?";
	private static final String FindEmptyPlaylistIdsSql  = "SELECT *  FROM PlayList WHERE count=0 ORDER BY SLOT";
	private static final String UpdatePlaylistSql        = "UPDATE PlayList SET name=?,count=?,time=? WHERE id=?";	
	private static final String MovePlaylistToTrashSql   = "UPDATE PlayListItem SET list_id=99 WHERE                list_id=?";
	private static final String MovePlaylistItemFromTrash= "UPDATE PlayListItem SET list_id=?  WHERE track_id=? AND list_id=99";
	private static final String FindPlaylistItemsByPlSql = "SELECT * FROM PlayListItem WHERE list_id=?";
	private static final String InsertPlaylistItemSql    = "INSERT INTO PlayListItem VALUES(?,?,?,?,?,?)";
	private static final String FindLibraryTrackByIdSql  = "SELECT * FROM LibraryTrack WHERE ID=?";
	
	private final PreparedStatement findLibraryTrackByFile= connection.prepareStatement(FindLibraryTrackByFileSql);
	private final PreparedStatement findPlaylistByName    = connection.prepareStatement(FindPlaylistByNameSql);
	private final PreparedStatement findPlaylistById      = connection.prepareStatement(FindPlaylistByIdSql);
	private final PreparedStatement findEmptyPlaylistIds  = connection.prepareStatement(FindEmptyPlaylistIdsSql);
	private final PreparedStatement updatePlaylist        = connection.prepareStatement(UpdatePlaylistSql);
	private final PreparedStatement movePlaylistToTrash   = connection.prepareStatement(MovePlaylistToTrashSql);
	private final PreparedStatement movePlaylistItemFromTrash=connection.prepareStatement(MovePlaylistItemFromTrash);
	private final PreparedStatement findPlaylistItemsByPl = connection.prepareStatement(FindPlaylistItemsByPlSql);
	private final PreparedStatement insertPlaylistItem    = connection.prepareStatement(InsertPlaylistItemSql);
	private final PreparedStatement findLibraryTrackById  = connection.prepareStatement(FindLibraryTrackByIdSql);
	
	private String name;
	private List   ids;
	
	
	Playlist(String name) throws ClassNotFoundException,SQLException {
		this.name = name;
		this.ids  = new ArrayList();
	}
	
	
	public void importPlaylist(String m3uPath) throws IOException,SQLException {
		File      m3uFile   = new File(m3uPath);
		M3UFile   m3u       = new M3UFile(m3uFile.toURL(), ".*web/", "//dec/export/imported/");		
		ResultSet resultSet = findPlaylist();
		boolean   trash     = false;
		long      plId;		
		
		// First check to see if the named playlist exists in the DB		
		if (resultSet.next()) {
			plId = resultSet.getLong("ID"); 
			updatePlaylist(99, "Trash", -1, 0);
			ids.add( new Long(99) );
			ids.add( new Long(plId) );
			
			// Move all of the current playlist items to the trash can (99)
System.err.println("Trashing: "+plId);
			movePlaylistToTrash.setLong(1, plId);
			trash = movePlaylistToTrash.executeUpdate() > 0;
			
		} else {
			plId = createPlaylist();
			ids.add( new Long(plId) );
		}
				
		// Now walk the list of m3u items populating the list using the trash can first
		Iterator it = m3u.iterator();
		while (it.hasNext()) {
			MyMP3File mp3     = (MyMP3File)it.next();			
			String    mp3Path = mp3.getFilename();
			long      order   = 0;
						
			findLibraryTrackByFile.setString(1, "%"+mp3Path);
			resultSet = findLibraryTrackByFile.executeQuery();
			long trkId;
			
			if (resultSet.next()) {
				trkId = resultSet.getLong("ID");
		
				boolean create = false;
				if (trash) {
System.err.println("Recovering: "+plId+"/"+trkId);
					movePlaylistItemFromTrash.setLong(1, plId);
					movePlaylistItemFromTrash.setLong(2, trkId);					
					create = movePlaylistItemFromTrash.executeUpdate() == 0;
				}
				
				if (create) {			
					createPlaylistItem(plId, trkId, order);
				}
				order++;
			}			
		}		
	}	
	
	
	private void calculateDuration() throws SQLException {
		Iterator it = ids.iterator();
		while (it.hasNext()) {
			long plId = ((Long)it.next()).longValue();
			Object[] info     = findPlaylistInfo(plId);
			String   name     = (String)info[0];
			Integer  count    = (Integer)info[1];
			Long     duration = (Long)info[2];
			updatePlaylist(plId, name, count.intValue(), duration.longValue());
		}
	}
	
	
	private ResultSet findPlaylist() throws SQLException {				
		findPlaylistByName.setString(1, name);
		return findPlaylistByName.executeQuery();		
	}	
	
	
	private ResultSet findPlaylistItems(long id) throws SQLException {
		findPlaylistItemsByPl.setLong(1, id);
		return findPlaylistItemsByPl.executeQuery();		
	}
	
	
	private Object[] findPlaylistInfo(long plId) throws SQLException {
		long      duration  = 0;
		int       count     = 0;
		ResultSet resultSet;
		
		findPlaylistById.setLong(1, plId);
		resultSet = findPlaylistById.executeQuery();
		resultSet.next();
		name = resultSet.getString("NAME");
		
		resultSet = findPlaylistItems(plId);
		while (resultSet.next()) {
			long trkId = resultSet.getLong("TRACK_ID");			
			duration += findTrackDuration(trkId);
			count++;
		}
		
		return new Object[]{name, new Integer(count),new Long(duration)};
	}
	
	
	private long findTrackDuration(long trkId) throws SQLException {
		long      duration = 0;		
		ResultSet resultSet;
		
		findLibraryTrackById.setLong(1, trkId);
		resultSet = findLibraryTrackById.executeQuery();
		while (resultSet.next()) {
			duration += resultSet.getLong("DURATION");
		}
		
		return duration;
	}
	
	
	private void resetEmptyPlaylists() throws SQLException {
		ResultSet resultSet = findEmptyPlaylistIds.executeQuery(); resultSet.next();
		
		while (resultSet.next()) {
			long   id   = resultSet.getLong("ID");
			long   slot = resultSet.getLong("SLOT");
			String name = "Playlist #"+slot; 
			updatePlaylist(id, name, 0, 0);
		}
	}
	
	
	private long createPlaylist() throws SQLException {
		ResultSet resultSet = findEmptyPlaylistIds.executeQuery(); resultSet.next();
		int       count     = -1;
		int       time      = 0;
		long      id        = resultSet.getLong("ID");

System.err.println("Creating: "+id);
		updatePlaylist(id, this.name, count, time);
		return id;
	}
	
	
	private long createPlaylistItem(long listId, long trackId, long order) throws SQLException {		
		long   pliId = getNextId(PlayListItemTable);
		long   type  = 1;
		String url   = "";		
				
System.err.println("Creating Item["+pliId+"]: "+listId+","+trackId+","+type+","+url+","+order);
		insertPlaylistItem.setLong  (1, pliId);
		insertPlaylistItem.setLong  (2, listId);
		insertPlaylistItem.setLong  (3, trackId);
		insertPlaylistItem.setLong  (4, type);
		insertPlaylistItem.setString(5, url);
		insertPlaylistItem.setLong  (6, order);
		insertPlaylistItem.executeUpdate();
							
		return pliId;
	}
	
	
	private long updatePlaylist(long id, String name, int count, long time) throws SQLException {
System.err.println("Updating["+id+"]: "+name+","+count+","+time);
		updatePlaylist.setString(1, name);
		updatePlaylist.setInt   (2, count);
		updatePlaylist.setLong  (3, time);
		updatePlaylist.setLong  (4, id);		
		updatePlaylist.executeUpdate();
		
		return id;
	}
	
	
	protected void dumpPlaylist(String name) throws SQLException {
		ResultSet resultSet;
		
		findPlaylistByName.setString(1, name);
		resultSet = findPlaylistByName.executeQuery();
        this.toString("PlayList: "+name, resultSet, false);
	}
	
	
	public void close(boolean commit) {
		try {
			if (commit) {
				calculateDuration();
				resetEmptyPlaylists();
			}
			super.close(commit);
			
		} catch (SQLException ex) {
		}					
	}


	public static void main(String[] args) throws Exception {
		String    name     = "Christmas2";
//		String    m3u      = "//dec/mcaster/htdocs/xmas.m3u";
		Playlist  playlist = new Playlist(name);
		
		// playlist.importPlaylist(m3u);		
		playlist.dumpPlaylist(name);
		playlist.dumpPlaylist("Trash");
		playlist.close(true);
	}
}