/*
 * as Windows does not support signal, OnServe use event to emulate them.
 * The supported signal is SIGTERM.
 * signals.c contains the signal handler logic.
 */
#include <windows.h>
#include <stdio.h>

/*
 * Send a clean termination signal to a process
 * it is like kill(pid,SIGTERM);
 */
int TermPid(long pid)
{
char Name[256];
HANDLE hevint;
BOOL  rc;

  sprintf(Name,"TERM%ld",pid);

  hevint = OpenEvent(EVENT_MODIFY_STATE,FALSE,Name);
  if (hevint == NULL) return(-1); // failed

  rc = SetEvent(hevint);
  CloseHandle(hevint);
  
  if (rc) return(0);
  return(-1);
}
