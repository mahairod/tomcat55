//
// OnServe monitor service module.
//
//  MODULE:   vdmonisvc.c
//
//  PURPOSE:  Implements the body of the service.
//            It reads the register entry and starts the OnServe.
//
//  FUNCTIONS:
//            ServiceStart(DWORD dwArgc, LPTSTR *lpszArgv);
//            ServiceStop( );


#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <process.h>
#ifdef CYGWIN
#else
#include <tchar.h>
#endif
#include "moni_inst.h"

/* globals */
SERVICE_STATUS          ssStatus;
SERVICE_STATUS_HANDLE   sshStatusHandle;
DWORD                   dwErr;

/* Event logger routines */

VOID AddToMessageLog(LPTSTR lpszMsg)
{
    TCHAR   szMsg[256];
    HANDLE  hEventSource;
    LPTSTR  lpszStrings[2];


        dwErr = GetLastError();

        // Use event logging to log the error.
        //
        hEventSource = RegisterEventSource(NULL, TEXT(SZSERVICENAME));

#ifdef CYGWIN
	sprintf(szMsg, TEXT("%s error: %d"), TEXT(SZSERVICENAME), dwErr);
#else
        _stprintf(szMsg, TEXT("%s error: %d"), TEXT(SZSERVICENAME), dwErr);
#endif
        lpszStrings[0] = szMsg;
        lpszStrings[1] = lpszMsg;

        if (hEventSource != NULL) {
            ReportEvent(hEventSource, // handle of event source
                EVENTLOG_ERROR_TYPE,  // event type
                0,                    // event category
                0,                    // event ID
                NULL,                 // current user's SID
                2,                    // strings in lpszStrings
                0,                    // no bytes of raw data
                lpszStrings,          // array of error strings
                NULL);                // no raw data

            (VOID) DeregisterEventSource(hEventSource);
        }
}


//
//  FUNCTION: ReportStatusToSCMgr()
//
//  PURPOSE: Sets the current status of the service and
//           reports it to the Service Control Manager
//
//  PARAMETERS:
//    dwCurrentState - the state of the service
//    dwWin32ExitCode - error code to report
//    dwWaitHint - worst case estimate to next checkpoint
//
//  RETURN VALUE:
//    TRUE  - success
//    FALSE - failure
//
//  COMMENTS:
//
BOOL ReportStatusToSCMgr(DWORD dwCurrentState,
                         DWORD dwWin32ExitCode,
                         DWORD dwWaitHint)
{
    static DWORD dwCheckPoint = 1;
    BOOL fResult = TRUE;


        if (dwCurrentState == SERVICE_START_PENDING)
            ssStatus.dwControlsAccepted = 0;
        else
            ssStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;

        ssStatus.dwCurrentState = dwCurrentState;
        ssStatus.dwWin32ExitCode = dwWin32ExitCode;
        ssStatus.dwWaitHint = dwWaitHint;

        if ( ( dwCurrentState == SERVICE_RUNNING ) ||
             ( dwCurrentState == SERVICE_STOPPED ) )
            ssStatus.dwCheckPoint = 0;
        else
            ssStatus.dwCheckPoint = dwCheckPoint++;


        // Report the status of the service to the service control manager.
        //
        if (!(fResult = SetServiceStatus( sshStatusHandle, &ssStatus))) {
            AddToMessageLog(TEXT("SetServiceStatus"));
        }
    return fResult;
}

// this event is signalled when the
// service should end
//
HANDLE  hServerStopEvent = NULL;


//
//  FUNCTION: ServiceStart
//
//  PURPOSE: Actual code of the service
//           that does the work.
//
//  PARAMETERS:
//    dwArgc   - number of command line arguments
//    lpszArgv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    The default behavior is to read the registry and start jsvc.
//    The service stops when hServerStopEvent is signalled, the jsvc
//    is stopped via TermPid(pid) (see kills.c).
//
VOID ServiceStart (DWORD dwArgc, LPTSTR *lpszArgv)
{
char	Data[256];
DWORD   qreturn;
STARTUPINFO StartupInfo;
PROCESS_INFORMATION ProcessInformation;
char *qptr;


    ///////////////////////////////////////////////////
    //
    // Service initialization
    //
	ProcessInformation.hProcess = NULL;

    // report the status to the service control manager.
    //
    AddToMessageLog(TEXT("ServiceStart: starting"));
    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

    // create the event object. The control handler function signals
    // this event when it receives the "stop" control code.
    //
    hServerStopEvent = CreateEvent(
        NULL,    // no security attributes
        TRUE,    // manual reset event
        FALSE,   // not-signalled
        NULL);   // no name

    if ( hServerStopEvent == NULL)
        goto cleanup;

    // report the status to the service control manager.
    //
    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

    // Read the registry and set environment.
    if (OnServeSetEnv()) {
      AddToMessageLog(TEXT("ServiceStart: read environment failed"));
      goto cleanup;
      }

    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

    // set the start path for jsvc.exe
	qptr = getenv("JAKARTA_HOME");
	if (qptr==NULL || strlen(qptr)==0) {
      AddToMessageLog(TEXT("ServiceStart: read JAKARTA_HOME failed"));
      goto cleanup;
      }
	strcpy(Data,qptr);
    strcat(Data,"\\bin\\jsvc.exe -nodetach");
	strcat(Data," -cp ");
	strcat(Data,qptr);
	strcat(Data,"/lib/service.jar org.apache.service.support.SimpleService");

    // create the jsvc process.
    AddToMessageLog(TEXT("ServiceStart: start jsvc"));
    memset(&StartupInfo,'\0',sizeof(StartupInfo));
    StartupInfo.cb = sizeof(STARTUPINFO);

    if (!CreateProcess(NULL,Data,NULL,NULL,FALSE,NORMAL_PRIORITY_CLASS,   
         NULL,NULL, &StartupInfo, &ProcessInformation))
      goto cleanup;
    AddToMessageLog(TEXT("ServiceStart: jsvc started"));
    CloseHandle(ProcessInformation.hThread);
    ProcessInformation.hThread = NULL;

    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

    // wait until the process is completly created.
    if (!WaitForInputIdle(ProcessInformation.hProcess , INFINITE)) {
      AddToMessageLog(TEXT("ServiceStart: jsvc stopped after creation"));
      goto cleanup;
      }

    //
    // OnServe monitor is now running.
    // report the status to the service control manager.
    //
    if (!ReportStatusToSCMgr(
        SERVICE_RUNNING,       // service state
        NO_ERROR,              // exit code
        0))                    // wait hint
        goto cleanup;

    //
    // End of initialization
    //
    ////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////
    //
    // Service is now running, perform work until shutdown:
    // Check every 60 seconds if the monitor is up.
    //

    for (;;) {
      qreturn = WaitForSingleObject(hServerStopEvent,60000); // each minutes.

      if (qreturn == WAIT_FAILED) break;// something have gone wrong.

      if (qreturn == WAIT_TIMEOUT) {
        // timeout check the monitor.
        if (GetExitCodeProcess(ProcessInformation.hProcess, &qreturn)) {
          if (qreturn == STILL_ACTIVE) continue;
          }
        AddToMessageLog(TEXT("ServiceStart: jsvc crashed"));
        CloseHandle(hServerStopEvent);
        CloseHandle(ProcessInformation.hProcess);
        exit(0); // exit ungracefully so
                 // Service Control Manager 
                 // will attempt a restart.
        break; //failed.
        }

      // stop the monitor by signal(event)
      sprintf(Data,"ServiceStart: stopping jsvc: %d",
              ProcessInformation.dwProcessId);
      AddToMessageLog(Data);

      if (TermPid(ProcessInformation.dwProcessId)) {
        AddToMessageLog(TEXT("ServiceStart: jsvc stop failed"));
        break;
        }
      AddToMessageLog(TEXT("ServiceStart: jsvc stopped"));
      break; // finished!!!
      }

  cleanup:

    AddToMessageLog(TEXT("ServiceStart: stopped"));
    if (hServerStopEvent)
        CloseHandle(hServerStopEvent);

    if (ProcessInformation.hProcess)
        CloseHandle(ProcessInformation.hProcess);

}


//
//  FUNCTION: ServiceStop
//
//  PURPOSE: Stops the service
//
//  PARAMETERS:
//    none
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    If a ServiceStop procedure is going to
//    take longer than 3 seconds to execute,
//    it should spawn a thread to execute the
//    stop code, and return.  Otherwise, the
//    ServiceControlManager will believe that
//    the service has stopped responding.
//    
VOID ServiceStop()
{
    if ( hServerStopEvent )
        SetEvent(hServerStopEvent);
}


//
//  FUNCTION: service_ctrl
//
//  PURPOSE: This function is called by the SCM whenever
//           ControlService() is called on this service.
//
//  PARAMETERS:
//    dwCtrlCode - type of control requested
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
VOID WINAPI service_ctrl(DWORD dwCtrlCode)
{
    // Handle the requested control code.
    //
    switch(dwCtrlCode)
    {
        // Stop the service.
        //
        // SERVICE_STOP_PENDING should be reported before
        // setting the Stop Event - hServerStopEvent - in
        // ServiceStop().  This avoids a race condition
        // which may result in a 1053 - The Service did not respond...
        // error.
        case SERVICE_CONTROL_STOP:
            ReportStatusToSCMgr(SERVICE_STOP_PENDING, NO_ERROR, 0);
            ServiceStop();
            return;

        // Update the service status.
        //
        case SERVICE_CONTROL_INTERROGATE:
            break;

        // invalid control code
        //
        default:
            break;

    }

    ReportStatusToSCMgr(ssStatus.dwCurrentState, NO_ERROR, 0);
}

//
//  FUNCTION: service_main
//
//  PURPOSE: To perform actual initialization of the service
//
//  PARAMETERS:
//    dwArgc   - number of command line arguments
//    lpszArgv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    This routine performs the service initialization and then calls
//    the user defined ServiceStart() routine to perform majority
//    of the work.
//
void WINAPI service_main(DWORD dwArgc, LPTSTR *lpszArgv)
{

    AddToMessageLog(TEXT("service_main:starting"));
    // register our service control handler:
    //
    sshStatusHandle = RegisterServiceCtrlHandler( TEXT(SZSERVICENAME), service_ctrl);

    if (!sshStatusHandle) {
	AddToMessageLog(TEXT("service_main:RegisterServiceCtrlHandler failed"));
        goto cleanup;
	}

    // SERVICE_STATUS members that don't change in example
    //
    ssStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    ssStatus.dwServiceSpecificExitCode = 0;


    // report the status to the service control manager.
    //
    if (!ReportStatusToSCMgr(SERVICE_START_PENDING,NO_ERROR,3000))   {
	AddToMessageLog(TEXT("service_main:ReportStatusToSCMgr failed"));
        goto cleanup;
	}


    ServiceStart( dwArgc, lpszArgv );

cleanup:

    // try to report the stopped status to the service control manager.
    //
    if (sshStatusHandle)
        (VOID)ReportStatusToSCMgr(
                            SERVICE_STOPPED,
                            dwErr,
                            0);

    AddToMessageLog(TEXT("service_main:stopped"));
    return;
}

//
//  FUNCTION: main
//
//  PURPOSE: entrypoint for service
//
//  PARAMETERS:
//    argc - number of command line arguments
//    argv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    main() either performs the command line task, or
//    call StartServiceCtrlDispatcher to register the
//    main service thread.  When the this call returns,
//    the service has stopped, so exit.
//
#ifdef CYGWIN
int main(int argc, char **argv)
#else
void _CRTAPI1 main(int argc, char **argv)
#endif
{
    SERVICE_TABLE_ENTRY dispatchTable[] =
    {
        { TEXT(SZSERVICENAME), (LPSERVICE_MAIN_FUNCTION)service_main },
        { NULL, NULL }
    };

	AddToMessageLog(TEXT("StartService starting"));
        if (!StartServiceCtrlDispatcher(dispatchTable)) {
		AddToMessageLog(TEXT("StartServiceCtrlDispatcher failed."));
		return;
		}
	AddToMessageLog(TEXT("StartService started"));
}
