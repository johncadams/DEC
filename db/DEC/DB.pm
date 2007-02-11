package DEC::DB;

use DBI;
use XBase;
use strict;


use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);


$VERSION   = 1.00;
@ISA       = qw(Exporter);
@EXPORT    = qw(
                getGenre
                getGenreForName
                getGenres
                addGenre

                getTrack
                getTrackForFile
                getTracks
                getTracksForGenre
                getTracksForArtist
                getTracksForAlbum
                setTrack
                setTrackGenre

                getPlaylist
                getPlaylistForName
                getPlaylists
                addPlaylist

                getPlaylistItem
                getPlaylistItems
                getPlaylistItemsForTrack
                getPlaylistItemsForPlaylist
                addPlaylistItem

                getArtist
                getArtistForName
                getArtists
                setArtist

                getAlbum
                getAlbums
                getAlbumsForArtist
                setAlbum
               );

@EXPORT_OK = qw(
               );

my (
    $getGenres,
    $getGenreForId,
    $getGenreForName,
    $updGenreCount,
    $insGenre,

    $getTracks,
    $getTrackForId,
    $getTrackForFile,
    $getTracksForGenreId,
    $getTracksForAlbumId,
    $getTracksForArtistId,
    $updTrack,
    $updTrackGenre,

    $getLists,
    $getListForId,
    $getListForName,
    $updListName,
    $updListCount,
    $getAvailableLists,
    $rstList,

    $getListItems,
    $getListItemForId,
    $getListItemsForTrackId,
    $getListItemsForListId,
    $insListItem,
    $updListItem,
    $mrkListItemsForListId,
    $fndMarkedListItems,

    $getArtists,
    $getArtistForId,
    $getArtistForName,
    $updArtist,
    $updArtistCount,

    $getAlbums,
    $getAlbumForAlbumId,
    $getAlbumsForArtistId,
    $updAlbum,
    $updAlbumCount,

    $getSequenceForTable,
    $updSequenceForTable,

    $dbh,
    $dbdir,
    %xbase,
    %dirtyTables,
    %dirtyGids,
    %dirtyPlids,
    %dirtyArtids,
    %dirtyAlbids,
    %dirtyTids,
    %plistOrder,
   );


sub BEGIN {
   $dbdir                  = "$ENV{'ODIN_HOME'}/db";
   $dbh                    = DBI->connect("DBI:XBase:$dbdir",,,
                                         {RaiseError => 1});

   $getGenres              = $dbh->prepare("SELECT * FROM Genre");
   $getGenreForName        = $dbh->prepare("SELECT * FROM Genre WHERE genre=?");
   $getGenreForId          = $dbh->prepare("SELECT * FROM Genre WHERE id=?");
   $insGenre               = $dbh->prepare("INSERT INTO Genre VALUES(?,?,?,?)");
   $updGenreCount          = $dbh->prepare("UPDATE Genre SET track_cnt=?,duration=? WHERE id=?");

   $getTracks              = $dbh->prepare("SELECT * FROM LibraryTrack");
   $getTracksForGenreId    = $dbh->prepare("SELECT * FROM LibraryTrack WHERE genre_id=?");
   $getTracksForAlbumId    = $dbh->prepare("SELECT * FROM LibraryTrack WHERE album_id=?");
   $getTracksForArtistId   = $dbh->prepare("SELECT * FROM LibraryTrack WHERE artist_id=?");
   $getTrackForId          = $dbh->prepare("SELECT * FROM LibraryTrack WHERE id=?");
   $getTrackForFile        = $dbh->prepare("SELECT * FROM LibraryTrack WHERE file=?");
   $updTrack               = $dbh->prepare("UPDATE LibraryTrack SET title=?,artist_id=?,album_id=?,genre_id=?,file=?,duration=?,cd_trk_id=?,cd_trk_num=?,year=? WHERE id=?");
   $updTrackGenre          = $dbh->prepare("UPDATE LibraryTrack SET genre_id=? WHERE id=?");

   $getListForId           = $dbh->prepare("SELECT * FROM PlayList WHERE id=?");
   $getListForName         = $dbh->prepare("SELECT * FROM PlayList WHERE name=?");
   $getLists               = $dbh->prepare("SELECT * FROM PlayList");
   $updListName            = $dbh->prepare("UPDATE PlayList SET name=?,count=-1,time=0 WHERE id=?");
   $updListCount           = $dbh->prepare("UPDATE PlayList SET count=?,time=? WHERE id=?");
   $rstList                = $dbh->prepare("UPDATE PlayList SET name=?,count=0,time=0 WHERE id=?");

   $getListItemForId       = $dbh->prepare("SELECT * FROM PlayListItem WHERE id=?");
   $getListItems           = $dbh->prepare("SELECT * FROM PlayListItem");
   $getListItemsForListId  = $dbh->prepare("SELECT * FROM PlayListItem WHERE list_id=?");
   $getListItemsForTrackId = $dbh->prepare("SELECT * FROM PlayListItem WHERE track_id=?");
   $insListItem            = $dbh->prepare("INSERT INTO PlayListItem VALUES(?,?,?,?,?,?)");
   $updListItem            = $dbh->prepare("UPDATE PlayListItem SET list_id=?,track_id=?,type=?,url=?,order=? WHERE id=?");
   $mrkListItemsForListId  = $dbh->prepare("UPDATE PlayListItem SET list_id=99,order=-1 WHERE list_id=?");
   $fndMarkedListItems     = $dbh->prepare("SELECT id FROM PlayListItem WHERE track_id=? AND list_id=99 AND order=-1");

   $getArtists             = $dbh->prepare("SELECT * FROM Artist");
   $getArtistForId         = $dbh->prepare("SELECT * FROM Artist WHERE id=?");
   $getArtistForName       = $dbh->prepare("SELECT * FROM Artist WHERE name=?");
   $updArtist              = $dbh->prepare("UPDATE Artist SET name=?,track_cnt=?,duration=? WHERE id=?");
   $updArtistCount         = $dbh->prepare("UPDATE Artist SET track_cnt=?,duration=? WHERE id=?");

   $getAlbums              = $dbh->prepare("SELECT * FROM Album");
   $getAlbumForAlbumId     = $dbh->prepare("SELECT * FROM Album WHERE id=?");
   $getAlbumsForArtistId   = $dbh->prepare("SELECT * FROM Album WHERE artist_id=?");
   $updAlbum               = $dbh->prepare("UPDATE Album SET name=?,artist_id=?,duration=?,count=? WHERE id=?");
   $updAlbumCount          = $dbh->prepare("UPDATE Album SET count=?,duration=? WHERE id=?");

   $getAvailableLists      = $dbh->prepare("SELECT * FROM PlayList WHERE count=0");
   $getSequenceForTable    = $dbh->prepare("SELECT * FROM SequenceNumber WHERE table=?");
   $updSequenceForTable    = $dbh->prepare("UPDATE SequenceNumber SET sequence=? WHERE table=?");
}


sub END {
   fixTables();
   printDeletedRecords(keys(%dirtyTables));
   $dbh->disconnect();
}



sub getGenre {
  my $id = $_[0];
  my @data;
  my %genre;

  $getGenreForId->execute($id);
  @data = $getGenreForId->fetchrow_array();
  if (@data) {
     $genre{"id"       } = $data[0];
     $genre{"name"     } = $data[1];
     $genre{"track_cnt"} = $data[2];
     $genre{"duration" } = $data[3];
  }
  return %genre;
}


sub getGenreForName {
  my $name = $_[0];
  my @data;
  my %genre;

  $getGenreForName->execute($name);
  @data = $getGenreForName->fetchrow_array();
  if (@data) {
     $genre{"id"       } = $data[0];
     $genre{"name"     } = $data[1];
     $genre{"track_cnt"} = $data[2];
     $genre{"duration" } = $data[3];
  }
  return %genre;
}


sub getGenres {
  my %gids;

  $getGenres->execute();
  while (my @data=$getGenres->fetchrow_array()) {
     my %genre;
     $genre{"id"       } = $data[0];
     $genre{"name"     } = $data[1];
#    $genre{"track_cnt"} = $data[2];
#    $genre{"duration" } = $data[3];
     $gids{$genre{"id"}} = $genre{"name"}
  }
  return %gids;
}


sub addGenre {
   my $name = $_[0];
   my %genre;

   if (%genre=getGenreForName($name)) {
      # Genre already exists
      return %genre;
   }

   print("Creating Genre: $name\n");
   my $gid = getNextId("Genre");

   # Create an empty Genre
   $insGenre->execute($gid, $name, 0, 0);

   $genre{"id"       } = $gid;
   $genre{"name"     } = $name;
   $genre{"track_cnt"} = 0;
   $genre{"duration" } = 0;

   setGenreDirty($gid);

   return %genre;
}


sub getTrack {
   my $tid = $_[0];
   my %track;

   $getTrackForId->execute($tid);
   if (my @data=$getTrackForId->fetchrow_array()) {
      $track{"id"        } = $data[0];
      $track{"title"     } = $data[1];
      $track{"artist_id" } = $data[2];
      $track{"album_id"  } = $data[3];
      $track{"genre_id"  } = $data[4];
      $track{"file"      } = $data[5];
      $track{"duration"  } = $data[6];
      $track{"cd_trk_id" } = $data[7];
      $track{"cd_trk_num"} = $data[8];
      $track{"year"      } = $data[9];
   }
   return %track;
}


sub getTrackForFile {
   my $file = $_[0];
   my %track;

   $getTrackForFile->execute($file);
   if (my @data=$getTrackForFile->fetchrow_array()) {
      $track{"id"        } = @data[0];
      $track{"title"     } = @data[1];
      $track{"artist_id" } = @data[2];
      $track{"album_id"  } = @data[3];
      $track{"genre_id"  } = @data[4];
      $track{"file"      } = @data[5];
      $track{"duration"  } = @data[6];
      $track{"cd_trk_id" } = @data[7];
      $track{"cd_trk_num"} = @data[8];
      $track{"year"      } = @data[9];
   } 
   return %track;
}


sub getTracks {
   my %tids;

   $getTracks->execute();
   while (my @data=$getTracks->fetchrow_array()) {
      if (@data) {
         my %track;
         $track{"id"        } = @data[0];
#        $track{"title"     } = @data[1];
#        $track{"artist_id" } = @data[2];
#        $track{"album_id"  } = @data[3];
#        $track{"genre_id"  } = @data[4];
         $track{"file"      } = @data[5];
#        $track{"duration"  } = @data[6];
#        $track{"cd_trk_id" } = @data[7];
#        $track{"cd_trk_num"} = @data[8];
#        $track{"year"      } = @data[9];
         $tids{$track{"id"}} = $track{"file"};
      }
   }
   return %tids;
}


sub getTracksForGenre {
  my $gid = $_[0];
  my %tids;

  $getTracksForGenreId->execute($gid);
  while (my @data=$getTracksForGenreId->fetchrow_array()) {
      my %track;
      $track{"id"        } = @data[0];
#     $track{"title"     } = @data[1];
#     $track{"artist_id" } = @data[2];
#     $track{"album_id"  } = @data[3];
#     $track{"genre_id"  } = @data[4];
      $track{"file"      } = @data[5];
#     $track{"duration"  } = @data[6];
#     $track{"cd_trk_id" } = @data[7];
#     $track{"cd_trk_num"} = @data[8];
#     $track{"year"      } = @data[9];
      $tids{$track{"id"}} = $track{"file"};
  }
  return %tids;
}


sub getTracksForArtist {
  my $aid = $_[0];
  my %tids;

  $getTracksForArtistId->execute($aid);
  while (my @data=$getTracksForArtistId->fetchrow_array()) {
      my %track;
      $track{"id"        } = @data[0];
#     $track{"title"     } = @data[1];
#     $track{"artist_id" } = @data[2];
#     $track{"album_id"  } = @data[3];
#     $track{"genre_id"  } = @data[4];
      $track{"file"      } = @data[5];
#     $track{"duration"  } = @data[6];
#     $track{"cd_trk_id" } = @data[7];
#     $track{"cd_trk_num"} = @data[8];
#     $track{"year"      } = @data[9];
      $tids{$track{"id"}} = $track{"file"};
  }
  return %tids;
}


sub getTracksForAlbum {
  my $alb = $_[0];
  my %tids;

  $getTracksForAlbumId->execute($alb);
  while (my @data=$getTracksForAlbumId->fetchrow_array()) {
      my %track;
      $track{"id"        } = @data[0];
#     $track{"title"     } = @data[1];
#     $track{"artist_id" } = @data[2];
#     $track{"album_id"  } = @data[3];
#     $track{"genre_id"  } = @data[4];
      $track{"file"      } = @data[5];
#     $track{"duration"  } = @data[6];
#     $track{"cd_trk_id" } = @data[7];
#     $track{"cd_trk_num"} = @data[8];
#     $track{"year"      } = @data[9];
      $tids{$track{"id"}} = $track{"file"};
  }
  return %tids;
}


sub getTrackArrayForPlaylist {
   my $plid = $_[0];
   my @tids;

   $getListItemsForListId->execute($plid);
   while (my @data=$getListItemsForListId->fetchrow_array()) {
      my $tid   = @data[2];
      my $order = @data[5];
      $tids[$order] = $tid;
   }
   return @tids;
}


sub setTrack {
   my %track = %{$_[0]};
   $updTrack->execute($track{"title"     },
                      $track{"artist_id" },
                      $track{"album_id"  },
                      $track{"genre_id"  },
                      $track{"file"      },
                      $track{"duration"  },
                      $track{"cd_trk_id" },
                      $track{"cd_trk_num"},
                      $track{"year"      },
                      $track{"id"        });

   setTrackDirty ($track{"id"       });
   setGenreDirty ($track{"genre_id" });
   setArtistDirty($track{"artist_id"});
   setAlbumDirty ($track{"album_id" });
}


sub setTrackGenre {
   my $id  = $_[0];
   my $gid = $_[1];
   $updTrackGenre->execute($gid, $id);
   setGenreDirty($gid);
}


sub getPlaylist {
   my $id = $_[0];
   my %pl;

   $getListForId->execute($id);
   if (my @data = $getListForId->fetchrow_array()) {
      $pl{"id"   } = @data[0];
      $pl{"name" } = @data[1];
      $pl{"count"} = @data[2];
      $pl{"slot" } = @data[3];
      $pl{"time" } = @data[4];
   }
   return %pl;
}


sub getPlaylistForName {
   my $name = $_[0];
   my %pl;

   $getListForName->execute($name);
   if (my @data = $getListForName->fetchrow_array()) {
      $pl{"id"   } = @data[0];
      $pl{"name" } = @data[1];
      $pl{"count"} = @data[2];
      $pl{"slot" } = @data[3];
      $pl{"time" } = @data[4];
   }
   return %pl;
}


sub getPlaylists {
   my %plids;

   $getLists->execute();
   while (my @data=$getLists->fetchrow_array()) {
      my %pl;
      $pl{"id"   } = @data[0];
      $pl{"name" } = @data[1];
      $pl{"count"} = @data[2];
      $pl{"slot" } = @data[3];
      $pl{"time" } = @data[4];
      $plids{$pl{"id"}} = $pl{"name"};
   }
   return %plids;
}


sub addPlaylist {
   my $name = $_[0];
   my %pl;

   $dbh->do("UPDATE PlayList SET name='Trash' WHERE id=99");
   setPlaylistDirty(99);

   if (%pl=getPlaylistForName($name)) {
      # Playlist already exists
      print("Setting Playlist: $name\n");
      $updListName->execute($name, $pl{"id"});
      $mrkListItemsForListId->execute($pl{"id"});

   } else {
      $getAvailableLists->execute();
      my @data=$getAvailableLists->fetchrow_array();
      if (@data) {
         $pl{"id"   } = @data[0];
         $pl{"name" } = @data[1];
         $pl{"count"} = @data[2];
         $pl{"slot" } = @data[3];
         $pl{"time" } = @data[4];
      }
      $getAvailableLists->finish();

      if (%pl) {
         print("Creating Playlist: $name\n");
         $updListName->execute($name, $pl{"id"});

      } else {
         die("No available playlists\n");
      }
   }

   setPlaylistDirty($pl{"id"});
   $plistOrder{$pl{"id"}} = 0;
   return %pl;
}


sub getPlaylistItem {
   my $id = $_[0];
   my %item;

   $getListItemsForListId->execute($id);
   my @data=$getListItemsForListId->fetchrow_array();
   if (@data) {
      $item{"id"      } = @data[0];
      $item{"list_id" } = @data[1];
      $item{"track_id"} = @data[2];
      $item{"type"    } = @data[3];
      $item{"url"     } = @data[4];
      $item{"order"   } = @data[5];
   }
   return %item;
}


sub getPlaylistItems {
   my %items;

   $getListItems->execute();
   while (my @data=$getListItems->fetchrow_array()) {
      my %item;
      $item{"id"      } = @data[0];
#     $item{"list_id" } = @data[1];
      $item{"track_id"} = @data[2];
#     $item{"type"    } = @data[3];
#     $item{"url"     } = @data[4];
#     $item{"order"   } = @data[5];
      $items{$item{"id"}} = $item{"track_id"};
   }
   return %items;
}


sub getPlaylistItemsForTrack {
   my $tid = $_[0];
   my %items;

   $getListItemsForTrackId->execute($tid);
   while (my @data=$getListItemsForTrackId->fetchrow_array()) {
      my %item;
      $item{"id"      } = @data[0];
#     $item{"list_id" } = @data[1];
      $item{"track_id"} = @data[2];
#     $item{"type"    } = @data[3];
#     $item{"url"     } = @data[4];
#     $item{"order"   } = @data[5];
      $items{$item{"id"}} = $item{"track_id"};
   }
   return %items;
}


sub getPlaylistItemsForPlaylist {
   my $plid = $_[0];
   my %items;

   $getListItemsForListId->execute($plid);
   while (my @data=$getListItemsForListId->fetchrow_array()) {
      my %item;
      $item{"id"      } = @data[0];
#     $item{"list_id" } = @data[1];
      $item{"track_id"} = @data[2];
#     $item{"type"    } = @data[3];
#     $item{"url"     } = @data[4];
#     $item{"order"   } = @data[5];
      $items{$item{"id"}} = $item{"track_id"};
   }
   return %items;
}


sub addPlaylistItem {
   my $plid   = $_[0];
   my $tid    = $_[1];
   my $file   = $_[2];
   my $type   = 1;  # Don't know what these means
   my $url    = ""; # Nor this
   my $order  = $plistOrder{$plid}++;

   $fndMarkedListItems->execute($tid);
   if (my @data=$fndMarkedListItems->fetchrow_array()) {
      my $itemId = $data[0];
      $updListItem->execute($plid, $tid, $type, $url, $order, $itemId);
#     printf("  Setting: %s\n", $file?$file:$tid);
   
   } else {
      my $itemId = getNextId("PlayListIt");
      $insListItem->execute($itemId, $plid, $tid, $type, $url, $order);
      printf("  Adding: %s\n", $file?$file:$tid);
   }

}


sub getArtist {
   my $id = $_[0];
   my %artist;

   $getArtistForId->execute($id);
   if (my @data = $getArtistForId->fetchrow_array()) {
      $artist{"id"       } = @data[0];
      $artist{"name"     } = @data[1];
      $artist{"track_cnt"} = @data[2];
      $artist{"duration" } = @data[3];
   }
   return %artist;
}


sub getArtistForName {
   my $name = $_[0];
   my %artist;

   $getArtistForName->execute($name);
   if (my @data = $getArtistForName->fetchrow_array()) {
      $artist{"id"       } = @data[0];
      $artist{"name"     } = @data[1];
      $artist{"track_cnt"} = @data[2];
      $artist{"duration" } = @data[3];
   }
   return %artist;
}


sub getArtists {
  my %ids;

  $getArtists->execute();
  while (my @data=$getArtists->fetchrow_array()) {
     $ids{$data[0]} = $data[1];
  }
  return %ids;
}


sub setArtist {
   my %artist = %{$_[0]};
   $updArtist->execute($artist{"name"     },
                       $artist{"track_cnt"},
                       $artist{"duration" },
                       $artist{"id"       });
}


sub getAlbum {
   my $id = $_[0];
   my %album;

   $getAlbumForAlbumId->execute($id);
   if (my @data = $getAlbumForAlbumId->fetchrow_array()) {
      $album{"id"       } = $data[0];
      $album{"name"     } = $data[1];
      $album{"artist_id"} = $data[2];
      $album{"duration" } = $data[3];
      $album{"count"    } = $data[4];
   }
   return %album;
}


sub getAlbums {
  my %ids;

  $getAlbums->execute();
  while (my @data=$getAlbums->fetchrow_array()) {
     my %album;
     $album{"id"       } = $data[0];
     $album{"name"     } = $data[1];
#    $album{"artist_id"} = $data[2];
#    $album{"duration" } = $data[3];
#    $album{"count"    } = $data[4];
     $ids{$album{"id"}} = $album{"name"};
  }
  return %ids;
}


sub getAlbumsForArtist {
  my $artId = $_[0];
  my %ids;

  $getAlbumsForArtistId->execute($artId);
  while (my @data=$getAlbumsForArtistId->fetchrow_array()) {
     my %album;
     $album{"id"       } = $data[0];
     $album{"name"     } = $data[1];
#    $album{"artist_id"} = $data[2];
#    $album{"duration" } = $data[3];
#    $album{"count"    } = $data[4];
     $ids{$album{"id"}} = $album{"name"};
  }
  return %ids;
}


sub setAlbum {
   my %album = %{$_[0]};

   $updAlbum->execute($album{"name"     },
                      $album{"artist_id"},
                      $album{"duration" },
                      $album{"count"    },
                      $album{"id"       });
}


sub setTrackDirty {
   my $id = $_[0];
   $dirtyTids{$id} = $id;
   $dirtyTables{"LibraryTrack"} = 1;
}


sub setGenreDirty {
   my $id = $_[0];
   $dirtyGids{$id} = $id;
   $dirtyTables{"Genre"} = 1;
}


sub setPlaylistDirty {
   my $id = $_[0];
   $dirtyPlids{$id} = $id;
   $dirtyTables{"PlayList"}     = 1;
   $dirtyTables{"PlayListItem"} = 1;
}


sub setAlbumDirty {
   my $id = $_[0];
   $dirtyAlbids{$id} = $id;
   $dirtyTables{"Album"} = 1;
}


sub setArtistDirty {
   my $id = $_[0];
   $dirtyArtids{$id} = $id;
   $dirtyTables{"Artist"} = 1;
}


sub fixGenreTable {
   my %gids = getGenres();
   my @gids = keys(%gids);

   print("Fixing Genres\n");
   while (my $gid=pop(@gids)) {
      fixGenreTableForId($gid);
   }
}


sub fixPlaylistTable {
   my @plids = getPlayLists();

   while (my $plid=pop(@plids)) {
      fixPlaylistTableForId($plid);
   }
}


sub fixGenreTableForId {
   my $id  = $_[0];
   my $cnt = 0;
   my $dur = 0;

   $getTracksForGenreId->execute($id);
   while (my @data=$getTracksForGenreId->fetchrow_array()) {
	  $cnt++;
	  $dur += $data[6];
   }

   printf("  Updated: %2d %4d %5d\n", $id, $cnt, $dur);
   $updGenreCount->execute($cnt, $dur, $id);
}


sub fixPlaylistTableForId {
   # Now count the track/times for the remaining tracks
   my $id    = $_[0];
   my %items = getPlaylistItemsForPlaylist($id);
   my @tids  = values(%items);
   my $cnt   = 0;
   my $dur   = 0;
   while (my $tid=pop(@tids)) {
      my %track = getTrack($tid);
      $cnt++;
      $dur += $track{"duration"};
   }

   printf("  Updated: %2d %4d %5d\n", $id, $cnt, $dur);
   $updListCount->execute($cnt, $dur, $id);
   if ($cnt == 0) {
     # Reset the name
     $rstList->execute("Playlist #".$id, $id);
   }
}


sub fixArtistTableForId {
   my $id  = $_[0];
   my $cnt = 0;
   my $dur = 0;

   $getTracksForArtistId->execute($id);
   while (my @data=$getTracksForArtistId->fetchrow_array()) {
	  $cnt++;
	  $dur += $data[6];
   }

   printf("  Updated: %2d %4d %5d\n", $id, $cnt, $dur);
   $updArtistCount->execute($cnt, $dur, $id);
}


sub fixAlbumTableForId {
   my $id  = $_[0];
   my $cnt = 0;
   my $dur = 0;

   $getTracksForAlbumId->execute($id);
   while (my @data=$getTracksForAlbumId->fetchrow_array()) {
	  $cnt++;
	  $dur += $data[6];
   }

   printf("  Updated: %2d %4d %5d\n", $id, $cnt, $dur);
   $updAlbumCount->execute($cnt, $dur, $id);
}


sub fixTables {
   my @ids = keys(%dirtyGids);
   if (@ids) {
      printf("Updating Genre table (%d)\n", $#ids+1);
      while (my $id=pop(@ids)) {
         fixGenreTableForId($id);
      }
   }

   @ids = keys(%dirtyPlids);
   if (@ids) {
      printf("Updating Playlist table (%d)\n", $#ids+1);
      while (my $id=pop(@ids)) {
         fixPlaylistTableForId($id);
      }
   }

   @ids = keys(%dirtyAlbids);
   if (@ids) {
      printf("Updating Album table (%d)\n", $#ids+1);
      while (my $id=pop(@ids)) {
         fixAlbumTableForId($id);
      }
   }

   @ids = keys(%dirtyArtids);
   if (@ids) {
      printf("Updating Artist table (%d)\n", $#ids+1);
      while (my $id=pop(@ids)) {
         fixArtistTableForId($id);
      }
   }
}


sub getNextId {
   my $table = $_[0];
   my $id;

   $getSequenceForTable->execute($table);
   my ($nop,$id) = $getSequenceForTable->fetchrow();

   if (! $id) {
      $id = 1;
      $dbh->do("INSERT INTO SequenceNumber VALUES(\"$table\",$id)");
   }
   $updSequenceForTable->execute($id+1, $table);

   return $id;
}


##
#  These are much faster since these use the index files
#  but we can only use them when tables don't changed (indices not broken)
##
sub getXBase {
   my $table = $_[0];
   if (!$xbase{$table}) {
      $xbase{$table} = new XBase("$dbdir/$table");
   }
   return $xbase{$table};
}


sub printDeletedRecords {
   my $veryfirst = 1;
   foreach my $table (@_) {
      my $first = 1;
      my $xbase  = getXBase($table);
      for (0 .. $xbase->last_record) {
         my $hashref = $xbase->get_record_as_hash($_);
         my %hash    = %{$hashref};

         if ($hash{"_DELETED"}) {
            if ($veryfirst) {
               print("Found deleted records\n");
               $veryfirst = 0;
            }
            if ($first) {
               print("  $table\n");
               $first = 0;
            }
            delete($hash{"_DELETED"});
            printf("   %s\n", join(",", %hash));
         }
      }
   }
}


sub getDataForIndex {
   my $value  = $_[0];
   my $table  = $_[1];
   my $tag    = $_[2];
   my $type   = $_[3];
   my $cdx    = "$dbdir/$table.cdx";
   my $xbase  = getXBase($table);
   my $cursor = $xbase->prepare_select_with_index(["$cdx", "$tag", "$type"]);

   if (!$cursor) {
      die(XBase->errstr()."\n");
   }
   $cursor->find_eq($value);
   return $cursor->fetch();
}

sub getTrack {
   my $tid  = $_[0];
   my @data = getDataForIndex($tid, "LibraryTrack", "ID_TAG", "N");
   my %track;
   if ($data[0] == $tid) {
      $track{"id"        } = $data[0];
      $track{"title"     } = $data[1];
      $track{"artist_id" } = $data[2];
      $track{"album_id"  } = $data[3];
      $track{"genre_id"  } = $data[4];
      $track{"file"      } = $data[5];
      $track{"duration"  } = $data[6];
      $track{"cd_trk_id" } = $data[7];
      $track{"cd_trk_num"} = $data[8];
      $track{"year"      } = $data[9];
   }
   return %track;
}


sub getTrackForFile {
   my $file = $_[0];
   my @data = getDataForIndex($file, "LibraryTrack", "FILE_TAG", "C");
   my %track;

   if ($data[5] eq $file) {
      $track{"id"        } = $data[0];
      $track{"title"     } = $data[1];
      $track{"artist_id" } = $data[2];
      $track{"album_id"  } = $data[3];
      $track{"genre_id"  } = $data[4];
      $track{"file"      } = $data[5];
      $track{"duration"  } = $data[6];
      $track{"cd_trk_id" } = $data[7];
      $track{"cd_trk_num"} = $data[8];
      $track{"year"      } = $data[9];
   }
   return %track;
}
