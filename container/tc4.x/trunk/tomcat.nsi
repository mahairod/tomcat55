
; Tomcat 4 script for Nullsoft Installer

Name "jakarta-tomcat-4.0"
Caption "Jakarta Tomcat 4.0"
OutFile tomcat4.exe
CRCCheck on
SetCompress force
SetDatablockOptimize on

BGGradient 000000 800000 FFFFFF
InstallColors FF8080 000000
Icon tomcat.ico

LicenseText "You must read the following license before installing:"
LicenseData LICENSE
ComponentText "This will install the Jakarta Tomcat 4.0 servlet container on your computer:"
InstType Normal
InstType Minimum
InstType "Full (w/ Source Code)"
AutoCloseWindow false
ShowInstDetails show
DirText "Please select a location to install Tomcat 4.0 (or use the default):"
SetOverwrite on
SetDateSave on

InstallDir "$PROGRAMFILES\Jakarta Tomcat 4.0"
InstallDirRegKey HKLM "SOFTWARE\Apache\Jakarta Tomcat 4.0" ""

Section "Tomcat 4.0 (required)"

  SectionIn 1 2 3
  SetOutPath $INSTDIR
  File tomcat.ico
  File LICENSE
  File README.txt
  File /r bin
  File /r common
  File /r jasper
  File /r lib
  File /r logs
  File /r server
  File /r work
  SetOutPath $INSTDIR\webapps
  File /r webapps\manager
  SetOutPath $INSTDIR\webapps\ROOT
  File /r webapps\ROOT\WEB-INF
  File webapps\ROOT\*.*

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

Section "Tomcat 4.0 Start Menu Group"

  SectionIn 1 2 3

  SetOutPath "$SMPROGRAMS\Jakarta Tomcat 4.0"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Tomcat Home Page.lnk" \
                 "http://jakarta.apache.org/tomcat"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Uninstall Tomcat 4.0.lnk" \
                 "$INSTDIR\uninst-tomcat4.exe"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Tomcat 4.0 Program Directory.lnk" \
                 "$INSTDIR"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Start Tomcat.lnk" \
                 "%JAVA_HOME%\bin\java.exe" \
                 '-cp "$INSTDIR\bin\bootstrap.jar;%JAVA_HOME%\lib\tools.jar" -Dcatalina.home="$INSTDIR" org.apache.catalina.startup.Bootstrap start' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWNORMAL

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Stop Tomcat.lnk" \
                 "%JAVA_HOME%\bin\java.exe" \
                 '-cp "$INSTDIR\bin\bootstrap.jar;%JAVA_HOME%\lib\tools.jar" -Dcatalina.home="$INSTDIR" org.apache.catalina.startup.Bootstrap stop' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWMINIMIZED

  SetOutPath "$SMPROGRAMS\Jakarta Tomcat 4.0\Configuration"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Configuration\Edit Server Configuration.lnk" \
                 notepad "$INSTDIR\conf\server.xml"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Configuration\Edit Webapp Defaults.lnk" \
                 notepad "$INSTDIR\conf\web.xml"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Configuration\Edit Users.lnk" \
                 notepad "$INSTDIR\conf\tomcat-users.xml"

SectionEnd

SectionDivider

Section "Tomcat 4.0 Documentation"

  SectionIn 1 3
  SetOutPath $INSTDIR\webapps
  File /r webapps\ROOT

  IfFileExists "$SMPROGRAMS\Jakarta Tomcat 4.0" 0 NoLinks

  SetOutPath "$SMPROGRAMS\Jakarta Tomcat 4.0\Documentation"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Documentation\Tomcat Documentation.lnk" \
                 "$INSTDIR\webapps\ROOT\docs\index.html"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Documentation\Catalina Javadoc.lnk" \
                 "$INSTDIR\webapps\ROOT\catalina-javadoc\index.html"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Documentation\Jasper Javadoc.lnk" \
                 "$INSTDIR\webapps\ROOT\jasper-javadoc\index.html"

  CreateShortCut "$SMPROGRAMS\Jakarta Tomcat 4.0\Documentation\Servlet API Javadoc.lnk" \
                 "$INSTDIR\webapps\ROOT\servletapi-javadoc\index.html"

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

Section "Tomcat 4.0 Source Code"

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

  WriteRegStr HKLM "SOFTWARE\Apache\Jakarta Tomcat 4.0" "" $INSTDIR
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Jakarta Tomcat 4.0" \
                   "DisplayName" "Jakarta Tomcat 4.0 (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Jakarta Tomcat 4.0" \
                   "UninstallString" '"$INSTDIR\uninst-tomcat4.exe"'

  Sleep 500
  BringToFront

SectionEnd

Function .onInstSuccess

    MessageBox MB_OK|MB_ICONEXCLAMATION \
      "If not done already, you need to set the JAVA_HOME environment\
 variable and have it point to your JDK installation directory."
  MessageBox MB_YESNO|MB_ICONQUESTION \
             "Setup has completed. View readme file now?" \
             IDNO NoReadme
    ExecShell open '$INSTDIR\README.txt'
  NoReadme:

FunctionEnd

UninstallText "This will uninstall Jakarta Tomcat 4.0 from your system:"
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

  DeleteRegKey HKCR "JSPFile"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Jakarta Tomcat 4.0"
  DeleteRegKey HKLM "SOFTWARE\Apache\Jakarta Tomcat 4.0"
  RMDir /r "$SMPROGRAMS\Jakarta Tomcat 4.0"
  Delete "$INSTDIR\tomcat.ico"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\README.txt"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\common"
  Delete "$INSTDIR\conf\*.dtd"
  RMDir /r "$INSTDIR\jasper"
  RMDir /r "$INSTDIR\lib"
  RMDir "$INSTDIR\logs"
  RMDir /r "$INSTDIR\server"
  RMDir /r "$INSTDIR\webapps\manager"
  RMDir /r "$INSTDIR\webapps\ROOT"
  RMDir /r "$INSTDIR\webapps\examples"
  RMDir /r "$INSTDIR\webapps\webdav"
  RMDir "$INSTDIR\webapps"
  RMDir /r "$INSTDIR\work"
  RMDir /r "$INSTDIR\src"
  RMDir "$INSTDIR"

  ; if $INSTDIR was removed, skip these next ones
  IfFileExists "$INSTDIR" 0 Removed 
    MessageBox MB_YESNO|MB_ICONQUESTION \
      "Remove all files in your Tomcat 4.0 directory? (If you have anything\
 you created that you want to keep, click No)" IDNO Removed
    Delete "$INSTDIR\*.*" ; this would be skipped if the user hits no
    RMDir /r "$INSTDIR"
    Sleep 500
    IfFileExists "$INSTDIR" 0 Removed 
      MessageBox MB_OK|MB_ICONEXCLAMATION \
                 "Note: $INSTDIR could not be removed."
  Removed:

SectionEnd
