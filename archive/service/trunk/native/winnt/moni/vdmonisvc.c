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
#include <tchar.h>
#include "moni_inst.h"

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
        AddToMessageLog(TEXT("ServiceStart: jsvc stopped"));
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
