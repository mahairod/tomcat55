
; Tomcat 4 script for Nullsoft Installer
; $Id$

Name "apache-tomcat-4.1"
Caption "Apache Tomcat 4.1"
OutFile tomcat4.exe
CRCCheck on
SetCompress force
SetDatablockOptimize on

BGGradient 000000 800000 FFFFFF
InstallColors FF8080 000000

Icon main.ico
UninstallIcon uninst.ico 
EnabledBitmap tickyes.bmp 
DisabledBitmap tickno.bmp

LicenseText "You must read the following license before installing:"
LicenseData INSTALLLICENSE
ComponentText "This will install the Apache Tomcat 4.1 servlet container on your computer:"
InstType Normal
InstType Minimum
InstType "Full (w/ Source Code)"
AutoCloseWindow false
ShowInstDetails show
DirText "Please select a location to install Tomcat 4.1 (or use the default):"
SetOverwrite on
SetDateSave on

InstallDir "$PROGRAMFILES\Apache Tomcat 4.1"
InstallDirRegKey HKLM "SOFTWARE\Apache\Apache Tomcat 4.1" ""

Section "Tomcat 4.1 (required)"

  SectionIn 1 2 3

  SetOutPath $INSTDIR
  File tomcat.ico
  File LICENSE
  File /r bin
  File /r common
  File /r shared
  File /r logs
  File /r server
  File /r work
  SetOutPath $INSTDIR\webapps
  File webapps\*.xml
  File /r webapps\ROOT

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"

  CopyFiles "$2\lib\tools.jar" "$INSTDIR\common\lib" 4500

SectionEnd

Section "NT Service (NT/2k/XP only)"

  SectionIn 3

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "RuntimeLib"
  
  SetOutPath $INSTDIR\bin
  File /oname=tomcat.exe bin\tomcat.exe
  
  ExecWait '"$INSTDIR\bin\tomcat.exe" -install "Apache Tomcat" "$2" -Djava.class.path="$INSTDIR\bin\bootstrap.jar" -Dcatalina.home="$INSTDIR" -start org.apache.catalina.startup.BootstrapService -params start -stop org.apache.catalina.startup.BootstrapService -params stop -out "$INSTDIR\logs\stdout.log" -err "$INSTDIR\logs\stderr.log"'
  
  ClearErrors

SectionEnd

Section "JSP Development Shell Extensions"

  SectionIn 1 2 3
  ; back up old value of .jsp
  ReadRegStr $1 HKCR ".jsp" ""
  StrCmp $1 "" Label1
    StrCmp $1 "JSPFile" Label1
    WriteRegStr HKCR ".jsp" "backup_val" $1

Label1:

  WriteRegStr HKCR ".jsp" "" "JSPFile"
  WriteRegStr HKCR "JSPFile" "" "Java Server Pages source"
  WriteRegStr HKCR "JSPFile\shell" "" "open"
  WriteRegStr HKCR "JSPFile\DefaultIcon" "" "$INSTDIR\tomcat.ico"
  WriteRegStr HKCR "JSPFile\shell\open\command" "" 'notepad.exe "%1"'

SectionEnd

Section "Tomcat 4.1 Start Menu Group"

  SectionIn 1 2 3

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"

  SetOutPath "$SMPROGRAMS\Apache Tomcat 4.1"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat Home Page.lnk" \
                 "http://jakarta.apache.org/tomcat"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Uninstall Tomcat 4.1.lnk" \
                 "$INSTDIR\uninst-tomcat4.exe"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat 4.1 Program Directory.lnk" \
                 "$INSTDIR"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Start Tomcat.lnk" \
                 "$2\bin\java.exe" \
                 '-jar -Duser.dir="$INSTDIR" "$INSTDIR\bin\bootstrap.jar" start' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWNORMAL

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Stop Tomcat.lnk" \
                 "$2\bin\java.exe" \
                 '-jar -Duser.dir="$INSTDIR" "$INSTDIR\bin\bootstrap.jar" stop' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWMINIMIZED

  SetOutPath "$SMPROGRAMS\Apache Tomcat 4.1\Configuration"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Configuration\Edit Server Configuration.lnk" \
                 notepad "$INSTDIR\conf\server.xml"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Configuration\Edit Webapp Defaults.lnk" \
                 notepad "$INSTDIR\conf\web.xml"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Configuration\Edit Users.lnk" \
                 notepad "$INSTDIR\conf\tomcat-users.xml"

SectionEnd

SectionDivider

Section "Tomcat 4.1 Documentation"

  SectionIn 1 3
  SetOutPath $INSTDIR\webapps
  File /r webapps\tomcat-docs

  IfFileExists "$SMPROGRAMS\Apache Tomcat 4.1" 0 NoLinks

  SetOutPath "$SMPROGRAMS\Apache Tomcat 4.1"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat Documentation.lnk" \
                 "$INSTDIR\webapps\tomcat-docs\index.html"

 NoLinks:

SectionEnd

Section "Example Web Applications"

  SectionIn 1 3

  SetOverwrite off
  SetOutPath $INSTDIR\conf
  File conf\server.xml
  SetOverwrite on
  SetOutPath $INSTDIR\webapps
  File /r webapps\examples
  File /r webapps\webdav

SectionEnd

SectionDivider

Section "Tomcat 4.1 Source Code"

  SectionIn 3
  SetOutPath $INSTDIR
  File /r src

SectionEnd

Section -post

  SetOverwrite off
  SetOutPath $INSTDIR\conf
  File /oname=server.xml conf\server-noexamples.xml.config
  SetOutPath $INSTDIR
  File /r conf

  SetOverwrite on

  ; since the installer is now created last (in 1.2+), this makes sure 
  ; that any old installer that is readonly is overwritten.
  Delete $INSTDIR\uninst-tomcat4.exe 

  WriteRegStr HKLM "SOFTWARE\Apache\Apache Tomcat 4.1" "" $INSTDIR
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 4.1" \
                   "DisplayName" "Apache Tomcat 4.1 (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 4.1" \
                   "UninstallString" '"$INSTDIR\uninst-tomcat4.exe"'

  Sleep 500
  BringToFront

SectionEnd


Function .onInit

  ClearErrors

  Call doUpdate

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
  ReadRegStr $3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$3" "RuntimeLib"

  IfErrors 0 NoAbort
    MessageBox MB_OK "Couldn't find a Java Development Kit installed on this \
computer. Please download one from http://java.sun.com."
    Abort

  NoAbort:
    MessageBox MB_OK "Using Java Development Kit version $1 found in $2$\r$\nUsing Java Runtime Environment version $3 found in $4"

FunctionEnd


Function .onInstSuccess

  ExecShell open '$SMPROGRAMS\Apache Tomcat 4.1'

FunctionEnd


Function doUpdate

  ; This function will be called if a previous Tomcat 4.1 installation has been
  ; found

  ReadRegStr $1 HKLM "SOFTWARE\Apache\Apache Tomcat 4.1" ""
  IfErrors NoUpdate

  MessageBox MB_YESNO|MB_ICONQUESTION \
      "A previous installation of Jakarata Tomcat 4.1 has been found in $1.\
 Do you want to upgrade it to the latest version ?" IDNO NoUpdate

  SetOverwrite ifnewer
  SetOutPath $INSTDIR
  File tomcat.ico
  File LICENSE
  File /r bin
  File /r common
  File /r shared
  File /r logs
  File /r server
  File /r work
  SetOutPath $INSTDIR\webapps
  File /r webapps\ROOT

  MessageBox MB_OK "Update was successful."

  ; Installation over
  Abort

NoUpdate:

FunctionEnd


UninstallText "This will uninstall Apache Tomcat 4.1 from your system:"
UninstallExeName uninst-tomcat4.exe


Section Uninstall

  Delete "$INSTDIR\uninst-tomcat4.exe"

  ReadRegStr $1 HKCR ".jsp" ""
  StrCmp $1 "JSPFile" 0 NoOwn ; only do this if we own it
    ReadRegStr $1 HKCR ".jsp" "backup_val"
    StrCmp $1 "" 0 RestoreBackup ; if backup == "" then delete the whole key
      DeleteRegKey HKCR ".jsp"
    Goto NoOwn
    RestoreBackup:
      WriteRegStr HKCR ".jsp" "" $1
      DeleteRegValue HKCR ".jsp" "backup_val"
  NoOwn:

  ExecWait '"$INSTDIR\bin\tomcat.exe" -uninstall "Apache Tomcat"'
  ClearErrors

  DeleteRegKey HKCR "JSPFile"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 4.1"
  DeleteRegKey HKLM "SOFTWARE\Apache\Apache Tomcat 4.1"
  RMDir /r "$SMPROGRAMS\Apache Tomcat 4.1"
  Delete "$INSTDIR\tomcat.ico"
  Delete "$INSTDIR\LICENSE"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\common"
  Delete "$INSTDIR\conf\*.dtd"
  RMDir /r "$INSTDIR\shared"
  RMDir "$INSTDIR\logs"
  RMDir /r "$INSTDIR\server"
  RMDir "$INSTDIR\webapps\*.xml"
  RMDir /r "$INSTDIR\webapps\ROOT"
  RMDir /r "$INSTDIR\webapps\tomcat-docs"
  RMDir /r "$INSTDIR\webapps\examples"
  RMDir /r "$INSTDIR\webapps\webdav"
  RMDir "$INSTDIR\webapps"
  RMDir /r "$INSTDIR\work"
  RMDir /r "$INSTDIR\src"
  RMDir "$INSTDIR"

  ; if $INSTDIR was removed, skip these next ones
  IfFileExists "$INSTDIR" 0 Removed 
    MessageBox MB_YESNO|MB_ICONQUESTION \
      "Remove all files in your Tomcat 4.1 directory? (If you have anything\
 you created that you want to keep, click No)" IDNO Removed
    Delete "$INSTDIR\*.*" ; this would be skipped if the user hits no
    RMDir /r "$INSTDIR"
    Sleep 500
    IfFileExists "$INSTDIR" 0 Removed 
      MessageBox MB_OK|MB_ICONEXCLAMATION \
                 "Note: $INSTDIR could not be removed."
  Removed:

SectionEnd
