#!/usr/bin/perl

# Replace the "package" declaration

my $file = $ARGV[0];
my $temp = "temp";
open (FILE, $file) || die ("Can't open file $file :$! \n");

open (TEMP, ">$temp") || die( "Can't open file $file: $!\n");

while (<FILE>) {

  s/import\s+($ARGV[1])\;/import\ $ARGV[2]\;/g;
  print TEMP;

}


close(FILE);
close(TEMP);
rename($temp, $file);
