# Microsoft Developer Studio Project File - Name="apache" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=apache - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "mod_jk.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "mod_jk.mak" CFG="apache - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "apache - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "apache - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "apache - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MD /W3 /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /FD /c
# ADD CPP /nologo /MD /W3 /Zi /O2 /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /I "..\common" /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\win32" /I "$(APACHE2_HOME)\include" /I "$(APACHE2_HOME)\srclib\apr\include" /I "$(APACHE2_HOME)\srclib\apr-util\include" /Fd"Release/mod_jk_src" /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib advapi32.lib /nologo /dll /machine:I386
# ADD LINK32 libhttpd.lib libapr.lib libaprutil.lib kernel32.lib user32.lib advapi32.lib wsock32.lib /nologo /base:"0x6A6B0000" /subsystem:windows /dll /incremental:no /debug /machine:I386 /out:"Release/mod_jk.so" /opt:ref /libpath:"$(APACHE2_HOME)\Release" /libpath:"$(APACHE2_HOME)\srclib\apr\Release" /libpath:"$(APACHE2_HOME)\srclib\apr-util\Release" /libpath:"$(APACHE2_HOME)\lib"

!ELSEIF  "$(CFG)" == "apache - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MDd /W3 /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /FD /c
# ADD CPP /nologo /MDd /W3 /GX /Zi /Od /D "_DEBUG" /D "WIN32" /D "_WINDOWS" /I "..\common" /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\win32" /I "$(APACHE2_HOME)\include" /I "$(APACHE2_HOME)\srclib\apr\include" /I "$(APACHE2_HOME)\srclib\apr-util\include" /Fd"Debug/mod_jk_src" /FD /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept
# ADD LINK32 libhttpd.lib libapr.lib libaprutil.lib kernel32.lib user32.lib advapi32.lib wsock32.lib /nologo /base:"0x6A6B0000" /subsystem:windows /dll /incremental:no /debug /machine:I386 /out:"Debug/mod_jk.so" /libpath:"$(APACHE2_HOME)/Debug" /libpath:"$(APACHE2_HOME)\srclib\apr\Debug" /libpath:"$(APACHE2_HOME)\srclib\apr-util\Debug" /libpath:"$(APACHE2_HOME)\lib"

!ENDIF 

# Begin Target

# Name "apache - Win32 Release"
# Name "apache - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=..\common\jk_ajp12_worker.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp13.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp13_worker.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp14.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp14_worker.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp_common.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_connect.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_context.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_jni_worker.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_lb_worker.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_map.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_md5.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_msg_buff.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_pool.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_sockbuf.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_uri_worker_map.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_util.c
# End Source File
# Begin Source File

SOURCE=..\common\jk_worker.c
# End Source File
# Begin Source File

SOURCE=.\mod_jk.c
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=..\common\jk_ajp12_worker.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp13.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp13_worker.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp14.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp14_worker.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp23_worker.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_ajp_common.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_connect.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_context.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_global.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_jni_worker.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_lb_worker.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_logger.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_map.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_md5.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_msg_buff.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_mt.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_pool.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_service.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_sockbuf.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_uri_worker_map.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_util.h
# End Source File
# Begin Source File

SOURCE=..\common\jk_worker.h
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# End Group
# End Target
# End Project
