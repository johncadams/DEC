#!/usr/bin/perl -w
BEGIN {
  if ($ENV{'ODIN_HOME'}) {
    @INC = (@INC,
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl/i386-linux",
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl");
  }
}


use strict;
use DBI;


my $dbdir       = "$ENV{'ODIN_HOME'}/db";
my $dbh         = DBI->connect("DBI:XBase:$dbdir",,, {RaiseError => 1});
my $emptyGenre  = $dbh->prepare("SELECT genre from Genre  WHERE track_cnt=0");
my $emptyArtist = $dbh->prepare("SELECT name  from Artist WHERE track_cnt=0");
my $emptyAlbum  = $dbh->prepare("SELECT name,artist_id from Album WHERE count=0");
my $artistName  = $dbh->prepare("SELECT name from Artist WHERE id=?");
my $first;


$emptyGenre->execute();
$first = 1;
while (my @data = $emptyGenre->fetchrow_array()) {
   if ($first) {
      print("Empty genres\n");
      $first = 0;
   }
   printf("  %s\n", $data[0]);
}


$emptyArtist->execute();
$first = 1;
while (my @data = $emptyArtist->fetchrow_array()) {
   if ($first) {
      print("Empty artists\n");
      $first = 0;
   }
   printf("  %s\n", $data[0]);
}


$emptyAlbum->execute();
$first = 1;
while (my @data = $emptyAlbum->fetchrow_array()) {
   $artistName->execute($data[1]);
   my $artist = $artistName->fetchrow();
   $artistName->finish();
   if ($first) {
      print("Empty artists\n");
      $first = 0;
   }
   printf("  %s (%s)\n", $data[0], $artist);
}
