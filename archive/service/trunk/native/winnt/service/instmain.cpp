/*
 * jsvc.exe install program, create the service JavaService
 */

// includes
#include <windows.h>
#include <string.h>
#include <ostream.h>
#include "moni_inst.h"

VOID Usage()
{
	cout << "\r\n - Java service installer\r\n\r\n"; 
	cout << " - Usage :\r\n";
	cout << "       To install Java service : InstSvc\r\n";
	cout << "       To remove Java service  : InstSvc -REMOVE\r\n\r\n";
	cout << "   Use regedit if you want to change something\r\n\r\n";
	cout << "   Note that the service key is in:\r\n";
    cout << "   HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\";
	cout << SZSERVICENAME;
	cout << "\r\n";
	cout << "   the environment keys in:\r\n";
	cout << "   ";
	cout << SZKEY_ONSERVE;
    cout << "\r\n";
	return;
}

/* from src/os/win32/service.c (httpd-1.3!) */

BOOL isWindowsNT(void)
{
    static BOOL once = FALSE;
    static BOOL isNT = FALSE;
 
    if (!once)
    {
        OSVERSIONINFO osver;
        osver.dwOSVersionInfoSize = sizeof(osver);
        if (GetVersionEx(&osver))
            if (osver.dwPlatformId == VER_PLATFORM_WIN32_NT)
                isNT = TRUE;
        once = TRUE;
    }
    return isNT;
}                                                                               


/* remove the service (first stop it!) NT version */

BOOL RemoveSvcNT (VOID)
{
	BOOL			removed;
	SC_HANDLE		hManager;
	SC_HANDLE		hService;
	SERVICE_STATUS	svcStatus;
	DWORD			dwCount;

	removed = FALSE;
	// open service control manager with full access right
	hManager = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS);
	if (NULL != hManager) {
		// open existing service
		hService = OpenService(hManager, SZSERVICENAME, SERVICE_ALL_ACCESS);
		if (NULL != hService) {
			// get the status of the service
			if (QueryServiceStatus(hService, &svcStatus)) {
				// and see if the service is stopped
				if (SERVICE_STOPPED != svcStatus.dwCurrentState) {
					// if not stop the service
					ControlService(hService, SERVICE_CONTROL_STOP, &svcStatus);
				}
				dwCount = 0;
				do {
					if (SERVICE_STOPPED == svcStatus.dwCurrentState) {
						// delete the service
						if (DeleteService(hService)) {
							removed = TRUE;
							break;
						}
					}
					// wait 10 seconds for the service to stop
					Sleep(10000);
					if (!QueryServiceStatus(hService, &svcStatus)) {
						// something went wrong
						break;
					}
					dwCount++;
				} while (10 > dwCount);
			}
			// close service handle
			CloseServiceHandle(hService);
		}
		// close service control manager
		CloseServiceHandle(hManager);
	}
	return removed;
} /* RemoveSvc */

/* remove service (non NT) stopping it looks ugly!!! */
BOOL RemoveSvc (VOID)
{
	HKEY hkey;
	DWORD rv;

	rv = RegOpenKey(HKEY_LOCAL_MACHINE,
		"Software\\Microsoft\\Windows\\CurrentVersion\\RunServices",
		&hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not open the RunServices registry key.\r\n";
		return FALSE;
	}
	rv = RegDeleteValue(hkey, SZSERVICENAME);
	RegCloseKey(hkey);
	if (rv != ERROR_SUCCESS)
		cout << "Could not delete the RunServices entry.\r\n";

	rv = RegOpenKey(HKEY_LOCAL_MACHINE,
		"SYSTEM\\CurrentControlSet\\Services", &hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not open the Services registry key.\r\n";
		return FALSE;
	}
	rv = RegDeleteKey(hkey, SZSERVICENAME);
	RegCloseKey(hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not delete the Services registry key.\r\n";
		return FALSE;
	}
	return TRUE;
}


/* Install service (NT version) */

BOOL InstallSvcNT (CHAR *svcExePath)
{
	BOOL		installed;
	SC_HANDLE	hManager;
	SC_HANDLE	hService;

	installed = FALSE;
	// open the service control manager with full access right
	hManager = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS);
	if (NULL != hManager) {
		// create the service
		hService = CreateService(hManager,
			SZSERVICENAME,			// name of the service
			SZSERVICEDISPLAYNAME,		// description
			SERVICE_ALL_ACCESS,
			SERVICE_WIN32_OWN_PROCESS,	// type of service
			SERVICE_DEMAND_START, // AUTO_START,	// startmode
			SERVICE_ERROR_NORMAL,		// error treatment
			svcExePath,			// path_name
			NULL,				// no load order enty
			NULL,				// no tag identifier.
			NULL,				// dependencies.
			NULL,		// LocalSystem account
			NULL);		// dummy user password
		if (NULL != hService) {
			// close service handle
			CloseServiceHandle(hService);
			installed = TRUE;
		}
	} else {
		cout << "OpenSCManager failed\r\n";
	}
	return installed;
}

/* Install service */

BOOL InstallSvc (CHAR *svcExePath)
{
	HKEY		hkey;
	DWORD rv;
	char szPath[MAX_PATH];

	cout << "InstallSvc for non-NT\r\n";

	rv = RegCreateKey(HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows"
			  "\\CurrentVersion\\RunServices", &hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not open the RunServices registry key\r\n";
		return FALSE;
	}
        rv = RegSetValueEx(hkey, SZSERVICENAME, 0, REG_SZ,
			   (unsigned char *) svcExePath,
			   strlen(svcExePath) + 1);
	RegCloseKey(hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not add ";
		cout << SZSERVICENAME;
		cout << ":";
		cout << svcExePath;
		cout << "to RunServices Registry Key\r\n";
		return FALSE;
	}

	strcpy(szPath,
		 "SYSTEM\\CurrentControlSet\\Services\\");
	strcat(szPath,SZSERVICENAME);
	rv = RegCreateKey(HKEY_LOCAL_MACHINE, szPath, &hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not create/open the ";
		cout << szPath;
		cout << " registry key\r\n";
		return FALSE;
	}
	rv = RegSetValueEx(hkey, "ImagePath", 0, REG_SZ,
			   (unsigned char *) svcExePath,
			   strlen(svcExePath) + 1);
	if (rv != ERROR_SUCCESS) {
		RegCloseKey(hkey);
		cout << "Could not add ImagePath to our Registry Key\r\n";
		return FALSE;
	}
	rv = RegSetValueEx(hkey, "DisplayName", 0, REG_SZ,
			   (unsigned char *) SZSERVICEDISPLAYNAME,
			   strlen(SZSERVICEDISPLAYNAME) + 1);
	RegCloseKey(hkey);
	if (rv != ERROR_SUCCESS) {
		cout << "Could not add DisplayName to our Registry Key\r\n";
		return FALSE;
	}
	return TRUE;
}

/*
 * Fill the registry with the environment variables
 */
BOOL InstallEnv (char *var, char *value)
{
	BOOL		installed;
	HKEY		hKey;

	installed = FALSE;
	// create the parameters registry tree
	if (ERROR_SUCCESS == RegCreateKeyEx(HKEY_LOCAL_MACHINE, SZKEY_ONSERVE, 0,
			NULL,REG_OPTION_NON_VOLATILE,KEY_ALL_ACCESS,NULL,
			&hKey, NULL)) {
			// key is created or opened
			RegSetValueEx(hKey,var,0,REG_SZ,(BYTE *)value,lstrlen(value)+1);
			RegCloseKey(hKey);
			installed = TRUE;
			}
	return installed;
} /* InstallEnv */

/*
 * Install or remove the OnServe service and Key in the registry.
 * no parameter install the OnServe.
 * -REMOVE: desinstall the OnServe service and Keys.
 */

INT main (INT argc, CHAR *argv[])
{
  BOOL done;

  cout << "\r\n - Copyright (c) 2001 The Apache Software Foundation. \r\n";
  cout << "\r\n";

  if (argc==1) {
	/* install jsvcservice.exe as a service */
	if (isWindowsNT())
		done = InstallSvcNT(SZDEFMONISVCPATH);
	else
		done = InstallSvc(SZDEFMONISVCPATH);

	if (done)
		cout << "InstallSvc done\r\n";
	else
		cout << "InstallSvc failed\r\n";

	/* install the environment variable in registry */
	InstallEnv("JAKARTA_HOME",SZJAKARTA_HOME);
	InstallEnv("CYGWIN",SZCYGWINPATH);
	InstallEnv("JAVA_HOME",SZJAVA_HOME);

	InstallEnv("HOSTNAME", "localhost");
	InstallEnv("HOSTPORT", "1200");

	return(0);
        }

  if (argc==2 && strcmp(argv[1],"-REMOVE")==0) {
	// remove the  service. removing the keys not yet done!!!
	cout << "\r\n - removing Java Service...\r\n\r\n";
	if (isWindowsNT())
		done = RemoveSvcNT();
	else
		done = RemoveSvc();
	if (!done) {
		cout << "\r\n - REMOVE FAILED....\r\n\r\n"; 
		return(2);
		}
	return(0);
        }
  Usage();
  return(1);
}
