#!/usr/bin/perl -w
BEGIN {
  if ($ENV{'ODIN_HOME'}) {
    @INC = (@INC,
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl/i386-linux",
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl");
  }
}

use strict;
use DEC::Map;


if ($#ARGV<0 || $#ARGV>2) {
   die("usage: $0 <genre> [ '-'|<genre> [ <suffix> ]]\n");
   exit;
}

my $inGenre  = $ARGV[0]?$ARGV[0]:"Unknown Genre";
my $outGenre = $ARGV[1]?$ARGV[1]:"-";
my $filter   = $ARGV[2]?$ARGV[2]:".mp3\$";

mapGenre($inGenre, $outGenre, $filter);
