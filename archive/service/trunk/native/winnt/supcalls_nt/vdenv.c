#ident "%W% %E%    ONSERVE V3.0"

/* ------------------------------------------------------------	*
 * file:	vdenv.c
 * authors:	OEC BS DC22, Barcelona
 * date:	January 1998
 * compiler:	ANSI C
 * desc:        Read the Win-NT register and set the OnServe environment
 *              variable. Set the PATH (for dynamic linking!).
 * note:
 * bugs:	0
 * ------------------------------------------------------------	*/


#include <windows.h>
#include "moni_inst.h"

#define ENVSIZE 1024

int MySetEnvironmentVariable(char *name, char *data)
{
char Variable[ENVSIZE];

  strcpy(Variable,name);
  strcat(Variable,"=");
  strcat(Variable,data);
  if (putenv(Variable)) return(-1);
  return(0);
}
//
//  FUNCTION: OnServeSetEnv()
//
//  PURPOSE: Actual code of the routine that reads the registry and
//           set the OnServe environment variables.
//           The PATH is needed for the dynamic linking.
//
//  RETURN VALUE:
//    0 : All OK.
//    <0: Something Failed. (Registry cannot be read or one key cannot be read).
//
//
int OnServeSetEnv ()
{
HKEY    hKey=NULL;
DWORD	Type;
char	jakarta_home[ENVSIZE]; // for the path
char	cygwin[ENVSIZE]; // for the path
char	Data[ENVSIZE];
DWORD	LData;
int	qreturn=0;


    // Read the registry and set environment.
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, SZKEY_ONSERVE,
                        0, KEY_READ,&hKey) !=  ERROR_SUCCESS)
      return(-1);

    // read key and set environment.

    // JAKARTA_HOME
    LData = sizeof(Data);
    if (RegQueryValueEx(hKey,"JAKARTA_HOME",NULL,&Type,Data,&LData)==ERROR_SUCCESS) {
      strcpy(jakarta_home,Data);
      MySetEnvironmentVariable("JAKARTA_HOME",Data);
      }
    else 
      qreturn = -2;

    // CYGWIN
    LData = sizeof(Data);
    if (RegQueryValueEx(hKey,"CYGWIN",NULL,&Type,Data,&LData)==ERROR_SUCCESS) {
      strcpy(cygwin,Data);
      MySetEnvironmentVariable("CYGWIN",Data);
      }
    else 
      qreturn = -3;

    // JAVA_HOME
    LData = sizeof(Data);
    if (RegQueryValueEx(hKey,"JAVA_HOME",NULL,&Type,Data,&LData)==ERROR_SUCCESS) {
      MySetEnvironmentVariable("JAVA_HOME",Data);
      }
    else 
      qreturn = -4;

    // HOSTNAME (Where the OnServe Monitor is running).
    LData = sizeof(Data);
    if (RegQueryValueEx(hKey,"HOSTNAME",NULL,&Type,Data,&LData)==ERROR_SUCCESS)
      MySetEnvironmentVariable("HOSTNAME",Data);
    else 
      qreturn = -5;

    // HOSTPORT (Where the OnServe Monitor is listening)
    LData = sizeof(Data);
    if (RegQueryValueEx(hKey,"HOSTPORT",NULL,&Type,Data,&LData)==ERROR_SUCCESS)
      MySetEnvironmentVariable("HOSTPORT",Data);
    else 
      qreturn = -6;


    RegCloseKey(hKey);
    hKey = NULL;

    // set the PATH otherwise nothing works!!!                                  
    LData = sizeof(Data);                                                       
    if (!GetEnvironmentVariable("PATH",Data,LData)) {                           
      strcpy(Data,jakarta_home);
      }
    else {
      strcat(Data,";");
      strcat(Data,jakarta_home);
      }
    strcat(Data,"\\bin");

    strcat(Data,";");
    strcat(Data,cygwin);
    strcat(Data,"\\bin");

    MySetEnvironmentVariable("PATH",Data);

    return(qreturn);
}
