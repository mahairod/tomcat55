
; Tomcat script for Nullsoft Installer
; $Id$

!define NAME "Apache Tomcat"
!define VERSION "@VERSION@"

!include "${NSISDIR}\Contrib\Modern UI\System.nsh"

;--------------------------------
;Configuration

  !define MUI_INSTALLOPTIONS

  !define MUI_LICENSEPAGE
  !define MUI_COMPONENTPAGE
  !define MUI_DIRSELECTPAGE
  !define MUI_INSTALLBUTTONTEXT_NEXT
  !define MUI_ABORTWARNING
  !define MUI_UNINSTALLER

  !define MUI_SETPAGE_FUNCTIONNAME "SetPage"
  !define MUI_UNSETPAGE_FUNCTIONNAME "un.SetPage"

  ;Language
    ;English
    LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
    !include "${NSISDIR}\Contrib\Modern UI\Language files\English.nsh"

  ;General
  Name "${NAME} ${VERSION}"
  OutFile tomcat-installer.exe
  BrandingText "${NAME} ${VERSION} Installer"

  ;Compression options
  CRCCheck on
  SetCompress force
  SetCompressor bzip2
  SetDatablockOptimize on

  !insertmacro MUI_INTERFACE
  !insertmacro MUI_INSTALLOPTIONS "$7" "$8"

  ;License dialog
  LicenseData INSTALLLICENSE

  ;Folder-select dialog
  InstallDir "$PROGRAMFILES\Apache Group\Tomcat 5.0"

  ;Install types
  InstType Normal
  InstType Minimum
  InstType Full

  ; Main registry key
  InstallDirRegKey HKLM "SOFTWARE\Apache Group\Tomcat\5.0" ""

  ReserveFile "${NSISDIR}\Plugins\InstallOptions.dll"
  ReserveFile "config.ini"

SubSection "Tomcat" SecTomcat

Section "Core" SecTomcatCore

  SectionIn 1 2 3

  SetOutPath $INSTDIR
  File tomcat.ico
  File LICENSE
  File /r bin
  Delete "$INSTDIR\bin\tomcat.exe"
  File /r common
  File /r conf
  File /r shared
  File /r logs
  File /r server
  File /r work
  File /r temp
  SetOutPath $INSTDIR\webapps
  File webapps\*.xml
  File /r webapps\ROOT

  Call findJavaPath
  Pop $2

  CopyFiles "$2\lib\tools.jar" "$INSTDIR\common\lib" 4500
  BringToFront

  Call configure

SectionEnd

Section "Service" SecTomcatService

  SectionIn 3

  Call findJVMPath
  Pop $2

  SetOutPath $INSTDIR\bin
  File /oname=tomcat.exe bin\tomcat.exe
  
  ExecWait '"$INSTDIR\bin\tomcat.exe" -install "Apache Tomcat 5.0" "$2" -Djava.class.path="$INSTDIR\bin\bootstrap.jar" -Dcatalina.home="$INSTDIR" -Djava.endorsed.dirs="$INSTDIR\common\endorsed" -start org.apache.catalina.startup.Bootstrap -params startd -stop org.apache.catalina.startup.Bootstrap -params stopd -out "$INSTDIR\logs\stdout.log" -err "$INSTDIR\logs\stderr.log"'
  
  BringToFront
  ClearErrors

SectionEnd

Section "Source Code" SecTomcatSource

  SectionIn 3
  SetOutPath $INSTDIR
  File /r src

SectionEnd

Section "Documentation" SecTomcatDocs

  SectionIn 1 3
  SetOutPath $INSTDIR\webapps
  File /r webapps\tomcat-docs

SectionEnd

SubSectionEnd

Section "Start Menu Items" SecMenu

  SectionIn 1 2 3

  Call findJavaPath
  Pop $2

  SetOutPath "$SMPROGRAMS\Apache Tomcat 5.0"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Tomcat Home Page.lnk" \
                 "http://jakarta.apache.org/tomcat"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Welcome.lnk" \
                 "http://127.0.0.1:$R0/"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Tomcat Administration.lnk" \
                 "http://127.0.0.1:$R0/admin/"

  IfFileExists "$INSTDIR\webapps\webapps\tomcat-docs" 0 NoDocumentaion

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Tomcat Documentation.lnk" \
                 "$INSTDIR\webapps\tomcat-docs\index.html"

NoDocumentaion:

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Uninstall Tomcat 5.0.lnk" \
                 "$INSTDIR\Uninstall.exe"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Tomcat 5.0 Program Directory.lnk" \
                 "$INSTDIR"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Start Tomcat.lnk" \
                 "$2\bin\java.exe" \
                 '-Duser.dir="$INSTDIR\bin" LauncherBootstrap -launchfile catalina.xml catalina start' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWNORMAL

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 5.0\Stop Tomcat.lnk" \
                 "$2\bin\java.exe" \
                 '-Duser.dir="$INSTDIR\bin" LauncherBootstrap -launchfile catalina.xml catalina stop' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWMINIMIZED

SectionEnd

Section "Examples" SecExamples

  SectionIn 1 3

  SetOverwrite on
  SetOutPath $INSTDIR\webapps
  File webapps\jsp-examples.war
  File webapps\servlets-examples.war

SectionEnd

Section ""

  ;Invisible section to display the Finish header
  !insertmacro MUI_FINISHHEADER

SectionEnd

Section -post

  WriteUninstaller "$INSTDIR\Uninstall.exe"

  Call startService

  WriteRegStr HKLM "SOFTWARE\Apache Group\Tomcat\5.0" "" $INSTDIR
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 5.0" \
                   "DisplayName" "Apache Tomcat 5.0 (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 5.0" \
                   "UninstallString" '"$INSTDIR\Uninstall.exe"'

  Sleep 500
  BringToFront

SectionEnd


Function .onInit

  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "config.ini"

  ClearErrors

  Call findJavaPath
  Pop $1
  MessageBox MB_OK "Using Java Development Kit found in $1"

FunctionEnd


Function .onInstSuccess

  ExecShell open '$SMPROGRAMS\Apache Tomcat 5.0'

FunctionEnd


Function .onInitDialog

  !insertmacro MUI_INNERDIALOG_INIT

    !insertmacro MUI_INNERDIALOG_START 1
      !insertmacro MUI_INNERDIALOG_TEXT 1040 $(MUI_INNERTEXT_LICENSE)
    !insertmacro MUI_INNERDIALOG_STOP 1

    !insertmacro MUI_INNERDIALOG_START 2
      !insertmacro MUI_INNERDIALOG_TEXT 1042 $(MUI_INNERTEXT_DESCRIPTION_TITLE)
      !insertmacro MUI_INNERDIALOG_TEXT 1043 $(MUI_INNERTEXT_DESCRIPTION_INFO)
    !insertmacro MUI_INNERDIALOG_STOP 2

    !insertmacro MUI_INNERDIALOG_START 3
      !insertmacro MUI_INNERDIALOG_TEXT 1041 $(MUI_INNERTEXT_DESTINATIONFOLDER)
    !insertmacro MUI_INNERDIALOG_STOP 3

  !insertmacro MUI_INNERDIALOG_END

FunctionEnd

Function .onNextPage

  !insertmacro MUI_INSTALLOPTIONS_NEXTPAGE
  !insertmacro MUI_NEXTPAGE

FunctionEnd

Function .onPrevPage

  !insertmacro MUI_INSTALLOPTIONS_PREVPAGE
  !insertmacro MUI_PREVPAGE

FunctionEnd

Function SetPage

  !insertmacro MUI_PAGE_INIT

    !insertmacro MUI_PAGE_START 1
       !insertmacro MUI_HEADER_TEXT $(MUI_TEXT_LICENSE_TITLE) $(MUI_TEXT_LICENSE_SUBTITLE)
    !insertmacro MUI_PAGE_STOP 1

    !insertmacro MUI_PAGE_START 2
      !insertmacro MUI_HEADER_TEXT $(MUI_TEXT_COMPONENTS_TITLE) $(MUI_TEXT_COMPONENTS_SUBTITLE)
    !insertmacro MUI_PAGE_STOP 2

    !insertmacro MUI_PAGE_START 3
      !insertmacro MUI_HEADER_TEXT $(MUI_TEXT_DIRSELECT_TITLE) $(MUI_TEXT_DIRSELECT_SUBTITLE)
   !insertmacro MUI_PAGE_STOP 3

    !insertmacro MUI_PAGE_START 4
      !insertmacro MUI_HEADER_TEXT "Configuration" "Tomcat basic configuration."
      !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Settings" "Title" "${NAME} ${VERSION} Configuration Options"
      !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Settings" "CancelConfirm" "Are you sure you want to quit ${NAME} Setup?"
      !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Settings" "CancelConfirmCaption" "${NAME} ${VERSION} Setup"
      !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Settings" "CancelConfirmFlags" "MB_ICONEXCLAMATION"
      !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Settings" "BackButtonText" $(MUI_BUTTONTEXT_BACK)
      !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Settings" "NextButtonText" $(MUI_BUTTONTEXT_NEXT)
      !insertmacro MUI_INSTALLOPTIONS_SHOW 4 "config.ini" "" ""
    !insertmacro MUI_PAGE_STOP 4

    !insertmacro MUI_PAGE_START 5
      !insertmacro MUI_HEADER_TEXT $(MUI_TEXT_INSTALLING_TITLE) $(MUI_TEXT_INSTALLING_SUBTITLE)
    !insertmacro MUI_PAGE_STOP 5

    !insertmacro MUI_PAGE_START 6
      !insertmacro MUI_HEADER_TEXT $(MUI_TEXT_FINISHED_TITLE) $(MUI_TEXT_FINISHED_SUBTITLE)
    !insertmacro MUI_PAGE_STOP 6

  !insertmacro MUI_PAGE_END

FunctionEnd

!insertmacro MUI_FUNCTION_DESCRIPTION_START

    !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcat} "Install the Tomcat Servlet container."
    !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatCore} "Install the Tomcat Servlet container core."
    !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatService} "Install the Tomcat service, used to automatically start Tomcat in the background when the computer is started. This requires Windows NT 4.0, Windows 2000 or Windows XP."
    !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatSource} "Install the Tomcat source code."
    !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatDocs} "Install the Tomcat documentation bundle. This include documentation on the servlet container and its configuration options, on the Jasper JSP page compiler, as well as on the native webserver connectors."
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMenu} "Create a Start Menu program group for Tomcat."
    !insertmacro MUI_DESCRIPTION_TEXT ${SecExamples} "Installs some examples web applications."

!insertmacro MUI_FUNCTION_DESCRIPTION_END

!insertmacro MUI_FUNCTION_ABORTWARNING



; =====================
; FindJavaPath Function
; =====================
;
; Find the JAVA_HOME used on the system, and put the result on the top of the
; stack
; Will exit if the path cannot be determined
;
Function findJavaPath

  ClearErrors

  ReadEnvStr $1 JAVA_HOME

  IfErrors 0 FoundJDK

  ClearErrors

  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$2" "JavaHome"
  ReadRegStr $3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$3" "RuntimeLib"

  FoundJDK:

  IfErrors 0 NoAbort
    MessageBox MB_OK "Couldn't find a Java Development Kit installed on this \
computer. Please download one from http://java.sun.com. If there is already \ a JDK installed on this computer, set an environment variable JAVA_HOME to the \ pathname of the directory where it is installed."
    Abort

  NoAbort:

  ; Put the result in the stack
  Push $1

FunctionEnd


; ====================
; FindJVMPath Function
; ====================
;
; Find the full JVM path, and put the result on top of the stack
; Will exit if the path cannot be determined
;
Function findJVMPath

  ReadEnvStr $1 JAVA_HOME
  IfFileExists "$1\jre\bin\hotspot\jvm.dll" 0 TryJDK14
    StrCpy $2 "$1\jre\bin\hotspot\jvm.dll"
    Goto EndIfFileExists
  TryJDK14:
  IfFileExists "$1\jre\bin\server\jvm.dll" 0 TryClassic
    StrCpy $2 "$1\jre\bin\server\jvm.dll"
    Goto EndIfFileExists
  TryClassic:
  IfFileExists "$1\jre\bin\classic\jvm.dll" 0 JDKNotFound
    StrCpy $2 "$1\jre\bin\classic\jvm.dll"
    Goto EndIfFileExists
  JDKNotFound:
    SetErrors
  EndIfFileExists:

  IfErrors 0 FoundJVMPath

  ClearErrors

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "RuntimeLib"
  
  FoundJVMPath:
  
  IfErrors 0 NoAbort
    MessageBox MB_OK "Couldn't find a Java Development Kit installed on this \
computer. Please download one from http://java.sun.com."
    Abort

  NoAbort:

  ; Put the result in the stack
  Push $2

FunctionEnd


; ==================
; Configure Function
; ==================
;
; Display the configuration dialog boxes, read the values entered by the user,
; and build the configuration files
;
Function configure

  !insertmacro MUI_INSTALLOPTIONS_READ $R0 "config.ini" "Field 2" "State"
  !insertmacro MUI_INSTALLOPTIONS_READ $R1 "config.ini" "Field 5" "State"
  !insertmacro MUI_INSTALLOPTIONS_READ $R2 "config.ini" "Field 7" "State"

  StrCpy $R4 'port="$R0"'
  StrCpy $R5 '<user name="$R1" password="$R2" roles="admin,manager" />'

  DetailPrint 'HTTP/1.1 Connector configured on port "$R0"'
  DetailPrint 'Admin user added: "$R1"'

  SetOutPath $TEMP
  File /r confinstall

  ; Build final server.xml
  Delete "$INSTDIR\conf\server.xml"
  FileOpen $R9 "$INSTDIR\conf\server.xml" w

  Push "$TEMP\confinstall\server_1.xml"
  Call copyFile
  FileWrite $R9 $R4
  Push "$TEMP\confinstall\server_2.xml"
  Call copyFile

  FileClose $R9

  DetailPrint "server.xml written"

  ; Build final tomcat-users.xml
  Delete "$INSTDIR\conf\tomcat-users.xml"
  FileOpen $R9 "$INSTDIR\conf\tomcat-users.xml" w

  Push "$TEMP\confinstall\tomcat-users_1.xml"
  Call copyFile
  FileWrite $R9 $R5
  Push "$TEMP\confinstall\tomcat-users_2.xml"
  Call copyFile

  FileClose $R9

  DetailPrint "tomcat-users.xml written"

 NoLinks:

  RMDir /r "$TEMP\confinstall"

FunctionEnd


; =================
; CopyFile Function
; =================
;
; Copy specified file contents to $R9
;
Function copyFile

  ClearErrors

  Pop $0

  FileOpen $1 $0 r

 NoError:

  FileRead $1 $2
  IfErrors EOF 0
  FileWrite $R9 $2

  IfErrors 0 NoError

 EOF:

  FileClose $1

  ClearErrors

FunctionEnd


; =====================
; StartService Function
; =====================
;
; Start Tomcat NT Service
;
Function startService

  IfFileExists "$INSTDIR\bin\tomcat.exe" 0 NoService
  ExecWait 'net start "Apache Tomcat 5.0"'
  BringToFront

 NoService:

FunctionEnd


; =====================
; StopService Function
; =====================
;
; Stop Tomcat NT Service
;
Function un.stopService

  IfFileExists "$INSTDIR\bin\tomcat.exe" 0 NoService
  ExecWait 'net stop "Apache Tomcat 5.0"'
  Sleep 1000
  BringToFront

 NoService:

FunctionEnd


;--------------------------------
;Uninstaller Section

Section Uninstall

  Delete "$INSTDIR\modern.exe"
  Delete "$INSTDIR\Uninstall.exe"

  ; Stopping NT service (if in use)
  Call un.stopService

  ExecWait '"$INSTDIR\bin\tomcat.exe" -uninstall "Apache Tomcat 5.0"'
  ClearErrors

  DeleteRegKey HKCR "JSPFile"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 5.0"
  DeleteRegKey HKLM "SOFTWARE\Apache Group\Tomcat\5.0"
  RMDir /r "$SMPROGRAMS\Apache Tomcat 5.0"
  Delete "$INSTDIR\tomcat.ico"
  Delete "$INSTDIR\LICENSE"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\common"
  Delete "$INSTDIR\conf\*.dtd"
  RMDir /r "$INSTDIR\shared"
  RMDir "$INSTDIR\logs"
  RMDir /r "$INSTDIR\server"
  Delete "$INSTDIR\webapps\*.xml"
  RMDir /r "$INSTDIR\webapps\ROOT"
  RMDir /r "$INSTDIR\webapps\tomcat-docs"
  RMDir /r "$INSTDIR\webapps\servlets-examples"
  RMDir /r "$INSTDIR\webapps\jsp-examples"
  Delete "$INSTDIR\webapps\servlets-examples.war"
  Delete "$INSTDIR\webapps\jsp-examples.war"
  RMDir "$INSTDIR\webapps"
  RMDir /r "$INSTDIR\work"
  RMDir /r "$INSTDIR\temp"
  RMDir /r "$INSTDIR\src"
  RMDir "$INSTDIR"

  ; if $INSTDIR was removed, skip these next ones
  IfFileExists "$INSTDIR" 0 Removed 
    MessageBox MB_YESNO|MB_ICONQUESTION \
      "Remove all files in your Tomcat 5.0 directory? (If you have anything\
 you created that you want to keep, click No)" IDNO Removed
    Delete "$INSTDIR\*.*" ; this would be skipped if the user hits no
    RMDir /r "$INSTDIR"
    Sleep 500
    IfFileExists "$INSTDIR" 0 Removed 
      MessageBox MB_OK|MB_ICONEXCLAMATION \
                 "Note: $INSTDIR could not be removed."
  Removed:

  !insertmacro MUI_UNFINISHHEADER

SectionEnd

;--------------------------------
;Uninstaller Functions

!insertmacro MUI_UNBASICFUNCTIONS

;eof
