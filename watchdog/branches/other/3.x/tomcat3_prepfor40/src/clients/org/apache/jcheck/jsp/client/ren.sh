#!/bin/sh

for name in `ls -R */*/*.java` 
do
  echo $name
  sed -e '1,$s/com.sun.jsp.tck.util/org.apache.jcheck.jsp.util/g' $name > $name.tmp
  sed -e '1,$s/com.sun.moo/org.apache.tools.moo/g' $name > $name.tmp
  mv $name.tmp $name
done

for name in `ls -R */*/*/*.java` 
do
  echo $name
  sed -e '1,$s/com.sun.jsp.tck.util/org.apache.jcheck.jsp.util/g' $name > $name.tmp
  sed -e '1,$s/com.sun.moo/org.apache.tools.moo/g' $name > $name.tmp
  mv $name.tmp $name
done

for name in `ls -R */*/*/*/*.java` 
do
  echo $name
  sed -e '1,$s/com.sun.jsp.tck.util/org.apache.jcheck.jsp.util/g' $name > $name.tmp
  sed -e '1,$s/com.sun.moo/org.apache.tools.moo/g' $name > $name.tmp
  mv $name.tmp $name
done
