@ECHO OFF
echo Bootstrapping
deltree /Y build
mkdir foo
set B=src\main\org\apache\tools\ant
javac -classpath ..\projectx-tr2.jar;..\javac.jar -d foo %B%\BuildException.java %B%\Main.java %B%\Project.java %B%\ProjectHelper.java %B%\Target.java %B%\Task.java %B%\taskdefs\Jar.java %B%\taskdefs\Javac.java %B%\taskdefs\Mkdir.java %B%\taskdefs\Deltree.java
copy src\main\org\apache\tools\ant\taskdefs\defaults.properties foo\org\apache\tools\ant\taskdefs\defaults.properties
copy src\main\org\apache\tools\ant\defaultManifest.mf foo\org\apache\tools\ant\defaultManifest.mf
java -classpath ..\javac.jar;..\projectx-tr2.jar;foo org.apache.tools.ant.Main dist
deltree /Y foo


