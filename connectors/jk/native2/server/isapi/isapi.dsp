# Microsoft Developer Studio Project File - Name="isapi" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=isapi - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "isapi.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "isapi.mak" CFG="isapi - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "isapi - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "isapi - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "isapi - Win32 Release"

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
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "ISAPI_EXPORTS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "ISAPI_EXPORTS" /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0xc0a /d "NDEBUG"
# ADD RSC /l 0xc0a /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386
# ADD LINK32 libapr.lib libaprutil.lib kernel32.lib user32.lib advapi32.lib wsock32.lib /nologo /dll /machine:I386 /out:"Release/isapi_redirector2.dll"

!ELSEIF  "$(CFG)" == "isapi - Win32 Debug"

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
# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "ISAPI_EXPORTS" /YX /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /I "..\..\include" /I "$(JAVA_HOME)\include" /I "$(JAVA_HOME)\include\win32" /I "$(APACHE20_HOME)\include" /I "$(APR_HOME)\apr\include" /I "$(APR_HOME)\apr-util\include" /I "$(APACHE20_HOME)\os\win32" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "ISAPI_EXPORTS" /FR /YX /FD /GZ /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0xc0a /d "_DEBUG"
# ADD RSC /l 0xc0a /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept
# ADD LINK32 libapr.lib libaprutil.lib kernel32.lib user32.lib advapi32.lib wsock32.lib /nologo /dll /debug /machine:I386 /out:"Debug/isapi_redirector2.dll" /pdbtype:sept /libpath:"$(APACHE20_HOME)\Release" /libpath:"$(APR_HOME)\apr\Release" /libpath:"$(APR_HOME)\apr-util\Release"

!ENDIF 

# Begin Target

# Name "isapi - Win32 Release"
# Name "isapi - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=..\..\common\jk_channel_apr_socket.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_channel_socket.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_config.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_endpoint.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_env.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_handler_logon.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_handler_response.c
# End Source File
# Begin Source File

SOURCE=.\jk_isapi_plugin.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_logger_file.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_map.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_md5.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_msg_ajp.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_objCache.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_pool.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_pool_apr.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_registry.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_registry.h
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_requtil.c
# End Source File
# Begin Source File

SOURCE=.\jk_service_iis.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_shm.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_uriEnv.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_uriMap.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_vm_default.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_worker_ajp13.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_worker_jni.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_worker_lb.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_worker_run.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_worker_status.c
# End Source File
# Begin Source File

SOURCE=..\..\common\jk_workerEnv.c
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=..\..\include\jk_channel.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_config.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_endpoint.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_env.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_global.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_handler.h
# End Source File
# Begin Source File

SOURCE=.\jk_iis.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_logger.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_map.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_md5.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_msg.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_mt.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_objCache.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_pool.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_requtil.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_service.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_shm.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_uriEnv.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_uriMap.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_vm.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_worker.h
# End Source File
# Begin Source File

SOURCE=..\..\include\jk_workerEnv.h
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# End Group
# End Target
# End Project
