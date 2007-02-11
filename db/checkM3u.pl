#!/usr/bin/perl -w
BEGIN {
  if ($ENV{'ODIN_HOME'}) {
    @INC = (@INC,
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl/i386-linux",
            "$ENV{'ODIN_HOME'}/perl5/lib/site_perl");
  }
}

use DEC::DB;
use DEC::Playlist;


my $m3uFile = "$ARGV[0]";
my $srcDir  = ".*My Music";
my @m3u;
my %decMissing;
my %decExtra;
my %dbTracks;
my $file;


print("Reading M3U...\n");
$SIG{__WARN__} = sigWarn;
   @m3u = getFilesFromM3u($m3uFile,$srcDir);
$SIG{__WARN__};


print("Reading DataBase...\n");
my %switch = getTracks();
my @files  = values(%switch);
foreach $file (@files) {
   $decExtra{$file} = $file;
}


printf("Processing (%d)...\n", $#m3u+1);
foreach $file (@m3u) {
   delete($decExtra{$file});

   my $fix = $file;
   $fix =~ s|slurped/|slurped//|;
   delete($decExtra{$fix});
}

printHash("Missing (Dec)",  \%decMissing);
printHash("Missing (M3U)",  \%decExtra);



sub printHash {
   my $title =   $_[0];
   my %hash  = %{$_[1]};
   my @files = keys(%hash);

   print("$title\n");
   @files = sort(@files);
   while (my $file=shift(@files)) {
      print("  $file\n");
   }
}


sub sigWarn {
   my $msg = $_[0];
   $msg =~ s!.*: '!!;
   $msg =~ s!'\s*$!!;
   $decMissing{$msg} = $msg;
}
