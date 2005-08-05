#!/bin/sh

for name in `ls *.java` 
do
  echo $name
  sed -e '1,$s/.useBean/.setProperty/g' $name > $name.tmp
  mv $name.tmp $name
done
