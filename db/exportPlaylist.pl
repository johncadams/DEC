#!/usr/bin/perl -w
BEGIN {
  if ($ENV{'ODIN_HOME'}) {
    @INC = (@INC,
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl/i386-linux",
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl");
  }
}

use strict;
use DEC::Playlist;


my $m3u    = $ARGV[0];
my $name   = $ARGV[1];
my $srcDir = $ARGV[2]?$ARGV[2]:"/Documents and Settings/Owner/My Documents/My Music";

exportPlaylist($m3u, $name, $srcDir);
