
; Tomcat 4 script for Nullsoft Installer

!ifdef NO_COMPRESSION
SetCompress off
SetDatablockOptimize off
!endif

!ifdef NO_CRC
CRCCheck off
!endif

Name "NSIS"
Caption "Jakarta Tomcat 4.0"
OutFile tomcat4.exe

#BGGradient 000000 800000 FFFFFF
#InstallColors FF8080 000000

LicenseText "You must read the following license before installing:"
LicenseData LICENSE
ComponentText "This will install the Jakarta Tomcat 4.0 servlet container on your computer:"
InstType Normal
InstType "Full (w/ Source Code)"
AutoCloseWindow false
ShowInstDetails show
DirText "Please select a location to install Tomcat 4.0 (or use the default):"
SetOverwrite on
SetDateSave on
!ifdef HAVE_UPX
  !packhdr tmp.dat "upx\upx --best --compress-icons=1 tmp.dat"
!endif

InstallDir "$PROGRAMFILES\Jakarta Tomcat 4.0"
InstallDirRegKey HKLM "SOFTWARE\Apache\Jakarta\Tomcat 4.0" ""

Section "Tomcat 4.0 (required)"
  SectionIn 1 2
  SetOutPath $INSTDIR
  File tomcat.ico
  File LICENSE
  File README.txt
  File /r bin
  File /r common
  File /r conf
  File /r jasper
  File /r lib
  File /r logs
  File /r server
  File /r webapps
  File /r work
SectionEnd

Section "JSP Development Shell Extensions"
  SectionIn 1 2

  ; back up old value of .nsi
  ReadRegStr $1 HKCR ".jsp" ""
  StrCmp $1 "" Label1
    StrCmp $1 "JSPFile" Label1
    WriteRegStr HKCR ".jsp" "backup_val" $1
Label1:

  WriteRegStr HKCR ".jsp" "" "JSPFile"
  WriteRegStr HKCR "JSPFile" "" "Java Server Pages source"
  WriteRegStr HKCR "JSPFile\shell" "" "open"
  WriteRegStr HKCR "JSPFile\DefaultIcon" "" $INSTDIR\tomcat.ico
  WriteRegStr HKCR "JSPFile\shell\open\command" "" 'notepad.exe "%1"'
  WriteRegStr HKCR "JSPFile\shell\compile" "" "Compile JSP"
  WriteRegStr HKCR "JSPFile\shell\compile\command" "" '"$INSTDIR\bin\jspc.bat" "%1"'
SectionEnd

Section "Tomcat 4.0 Start Menu Group"
  SectionIn 1 2
  SetOutPath "$SMPROGRAMS\Tomcat 4.0"
  CreateShortCut "$SMPROGRAMS\Tomcat 4.0\Tomcat Home Page.lnk" \
                 "http://jakarta.apache.org/tomcat"
  CreateShortCut "$SMPROGRAMS\Tomcat 4.0\Uninstall Tomcat 4.0.lnk" \
                 "$INSTDIR\uninst-tomcat4.exe"
  CreateShortCut "$SMPROGRAMS\Tomcat 4.0\Tomcat 4.0 Documentation.lnk" \
                 "http://jakarta.apache.org/tomcat/tomcat-4.0-doc/index.html"
  CreateShortCut "$SMPROGRAMS\Tomcat 4.0\Tomcat 4.0 Program Directory.lnk" \
                 "$INSTDIR"
  CreateShortCut "$SMPROGRAMS\Tomcat 4.0\Start Tomcat.lnk" \
                 "$INSTDIR\bin\startup.bat"
  CreateShortCut "$SMPROGRAMS\Tomcat 4.0\Stop Tomcat.lnk" \
                 "$INSTDIR\bin\shutdown.bat"
SectionEnd

!ifndef NO_SOURCE
SectionDivider

Section "Tomcat 4.0 Source Code"
  SectionIn 2
  SetOutPath $INSTDIR\Source
  File /r src
SectionEnd

!endif

Section -post
  SetOutPath $INSTDIR

  ; since the installer is now created last (in 1.2+), this makes sure 
  ; that any old installer that is readonly is overwritten.
  Delete $INSTDIR\uninst-tomcat4.exe 

  WriteRegStr HKLM "SOFTWARE\Tomcat 4.0" "" $INSTDIR
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tomcat 4.0" \
                   "DisplayName" "Jakarta Tomcat 4.0 (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tomcat 4.0" \
                   "UninstallString" '"$INSTDIR\uninst-tomcat4.exe"'
  ExecShell open '$INSTDIR'

  WriteRegStr HKCU "Environment" "CATALINA_HOME" $INSTDIR

  Sleep 500
  BringToFront
SectionEnd

Function .onInstSuccess
  MessageBox MB_YESNO|MB_ICONQUESTION \
             "Setup has completed. View readme file now?" \
             IDNO NoReadme
    ExecShell open '$INSTDIR\README.txt'
  NoReadme:
FunctionEnd

!ifndef NO_UNINST
UninstallText "This will uninstall Jakarta Tomcat 4.0 from your system:"
UninstallExeName uninst-tomcat4.exe

Section Uninstall
  ReadRegStr $1 HKCR ".nsi" ""
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
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Tomcat 4.0"
  DeleteRegKey HKLM $InstallDirRegKey
  Delete "$SMPROGRAMS\Tomcat 4.0\*.lnk"
  RMDir "$SMPROGRAMS\Tomcat 4.0"
  Delete $INSTDIR\tomcat.ico
  Delete $INSTDIR\LICENSE
  Delete $INSTDIR\README.txt
  RMDir /r bin
  RMDir /r common
  RMDir /r conf
  RMDir /r jasper
  RMDir /r lib
  RMDir /r logs
  RMDir /r server
  RMDir /r webapps
  RMDir /r work
  RMDir /r $INSTDIR\Source
  RMDir $INSTDIR

  ; if $INSTDIR was removed, skip these next ones
  IfFileExists $INSTDIR 0 Removed 
    MessageBox MB_YESNO|MB_ICONQUESTION \
      "Remove all files in your Tomcat 4.0 directory? (If you have anything\
 you created that you want to keep, click No)" IDNO Removed
    Delete $INSTDIR\*.* ; this would be skipped if the user hits no
    RMDir /r $INSTDIR
    IfFileExists $INSTDIR 0 Removed 
      MessageBox MB_OK|MB_ICONEXCLAMATION \
                 "Note: $INSTDIR could not be removed."
  Removed:
SectionEnd

!endif
