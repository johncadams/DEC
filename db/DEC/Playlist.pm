package DEC::Playlist;

use strict;
use DEC::DB;


use vars qw(@ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $VERSION);

$VERSION   = 1.00;
@ISA       = qw(Exporter);
@EXPORT    = qw(importPlaylist exportPlaylist getFilesFromM3u);
@EXPORT_OK = qw();


my $destDir = "/content/music/slurped";


sub getFilesFromM3u {
   my $file   = $_[0];
   my $srcDir = $_[1];
   my $die;
   my $line;
   my @rtn;

   open(M3U, $file) || die("Cannot open: $file\n");
   $line = <M3U>;                      # Ignore the #EXTM3U line
   while ($line=<M3U>) {
      $line =~ s|\s*$||;               # DOS/Unix chomp
      $line =~ s/#EXTINF://;           # Strip off the leading marker
      $line =~ s/,.*$//;               # Strip off the title;
      my $dur = $line;

      $line = <M3U>;
      $line =~ s|\s*$||;               # DOS/Unix chomp
      $line =~ tr!\\!/!;               # Convert to Unix paths
      $line =~ s!$srcDir!$destDir!;    # Replace the 'from' with the 'to'
      if (-r $line) {
         push(@rtn, $line);
      } else {
         warn("Cannot locate: '$line'\n");
      }
   }
   close(M3U);

   return @rtn;
}


sub createM3uFromTrackIds {
   my $file   = $_[0];
   my $srcDir = $_[1];
   my @tids   = @{$_[2]};

   open(M3U, ">$file");
   print(M3U "#EXTM3U\n");

   while (my $tid=shift(@tids)) {
      my %track  = getTrack($tid);
      my %artist = getArtist($track{"artist_id"});
      my $mp3    = $track{"file"};
      my $title  = $track{"title"};
      my $dur    = $track{"duration"};
      my $artist = $artist{"name"};
      
      $mp3 =~ s!//!/!;
      $mp3 =~ s!$destDir!$srcDir!;
      $mp3 =~ s!/!\\!;
      print(M3U "#EXTINF:$dur,$artist - $title\n");
      print(M3U "$mp3\n");
   }
   close(M3U);
}


sub importPlaylist {
   my $m3u    = $_[0];
   my $name   = $_[1];
   my $srcDir = $_[2];
   my %pl     = addPlaylist($name);
   my @files  = getFilesFromM3u($m3u, $srcDir);

   while (my $file=shift(@files)) {
      my %track = getTrackForFile($file);
      if ($track{"id"}) {
         addPlaylistItem($pl{"id"}, $track{"id"}, $track{"file"});
      } else {
         # Maybe we are seeing the need to double the /
         $file =~ s|slurped/|slurped//|;
         %track = getTrackForFile($file);
         if ($track{"id"}) {
            addPlaylistItem($pl{"id"}, $track{"id"}, $track{"file"});
         } else {
            warn("Cannot locate track: $file\n");
         }
      }
   }
}


sub exportPlaylist {
   my $m3u    = $_[0];
   my $name   = $_[1];
   my $srcDir = $_[2];
   my %pl     = getPlaylistForName($name);
   my @tids   = DEC::DB::getTrackArrayForPlaylist($pl{"id"});

   createM3uFromTrackIds($m3u, $srcDir, \@tids);
}
