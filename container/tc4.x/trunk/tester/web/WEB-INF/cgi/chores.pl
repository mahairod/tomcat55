#!/usr/bin/perl

%chores = ("Monday", "vacuum", "Wednesday", "mop", "Friday", "wash windows");

print "Content-type: text/html\n\n";
foreach $day (keys (%chores)) {
print "<P>On $day, we have to $chores{$day}";
} 

