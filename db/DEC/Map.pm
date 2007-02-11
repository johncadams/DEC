package DEC::Map;

use strict;
use DEC::DB;
use MP3::Info;


use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);

$VERSION   = 1.00;
@ISA       = qw(Exporter);
@EXPORT    = qw(mapGenre mapVideo);



sub mapGenre {
   my $inGenre  = $_[0];
   my $outGenre = $_[1];
   my $filter   = $_[2];

   my %inGenre = getGenreForName($inGenre);
   my $inId    = $inGenre{"id"};
   my %tracks  = getTracksForGenre($inId);
   my @tids    = keys(%tracks);


   # Load all of the WINAMP known genres
   use_winamp_genres();

   printf("Mapping %s[%s] -> [%s]\n", $filter, $inGenre, $outGenre);
   while (my $tid=shift(@tids)) {
      my $file = $tracks{$tid};

      if ($file=~/$filter/) {

         my $genreName = $outGenre;
         if ($genreName eq "-") {
            my $tag = get_mp3tag($file);
            $genreName = $tag->{GENRE};
         }
     
         if ($genreName) {
            # Only switch to defined genres
            my %genre = addGenre($genreName);
            setTrackGenre($tid, $genre{"id"});
            DEC::DB::setGenreDirty($inId);
         }
         printf("  %d) %s\n", $#tids+2, $file);
      }
   }
}


sub mapVideo {
   my %genre    = addGenre($_[0]);
   my $filter   = $_[1];

   my $vidTrack = 99;
   my $vidGenre = $genre{"id"};
   my $vidTitle = " (Video)";

   my %tracks   = getTracks();
   my @tids     = keys(%tracks);


   printf("Mapping .mp3[*] -> %s[%s]\n", $filter, $genre{name});
   while (my $tid=shift(@tids)) {
      my $rm  = $tracks{$tid};
      my $mp3 = $rm;

      $mp3 =~ s|$filter|.mp3|;
      if ($rm =~ /$filter/) {
         if (-r $mp3) {
            my %rmTrack  = getTrackForFile($rm);
            my %mp3Track = getTrackForFile($mp3);

            if (!%mp3Track) {
               $mp3 =~ s|slurped//|slurped/|;
               %mp3Track = getTrackForFile($mp3);
            }

            if (!%mp3Track) {
               $mp3 =~ s|slurped/|slurped//|;
               %mp3Track = getTrackForFile($mp3);
            }

            if (%mp3Track) {
               if ($rmTrack{"cd_trk_num"} != $vidTrack) {
   
                  print("$rmTrack{'file'}\n");

                  DEC::DB::setGenreDirty ($rmTrack{"genre_id" });
                  DEC::DB::setAlbumDirty ($rmTrack{"album_id" });
                  DEC::DB::setArtistDirty($rmTrack{"artist_id"});

                  $rmTrack{"title"     } = $mp3Track{"title"     }.$vidTitle;
                  $rmTrack{"artist_id" } = $mp3Track{"artist_id" };
                  $rmTrack{"album_id"  } = $mp3Track{"album_id"  };
                  $rmTrack{"cd_trk_num"} = $vidTrack;
                  $rmTrack{"genre_id"  } = $vidGenre;

                  if ($rmTrack{"artist_id"} && $rmTrack{"album_id"}) {
                     setTrack(\%rmTrack);
                  } else {
                     printf("Problems with mp3: %s\n%s\n", $mp3, join(",", %mp3Track));
                     die;
                  }
               }
            } else {
               print("$rm...BAD FILE PATH\n");
            }
         } else {
            print("$rm...NO MATCH FOUND\n");
         }
      }
   }
}
