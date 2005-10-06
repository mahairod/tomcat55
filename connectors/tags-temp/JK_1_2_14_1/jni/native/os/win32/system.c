/* Copyright 2000-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 * @author Mladen Turk
 * @version $Revision$, $Date$
 */

#define _WIN32_WINNT 0x0500
#include <windows.h>
#include <winsock.h>
#include "apr.h"
#include "apr_pools.h"
#include "apr_poll.h"
#include "apr_network_io.h"
#include "apr_arch_misc.h" /* for apr_os_level */
#include "apr_arch_atime.h"  /* for FileTimeToAprTime */

#include "tcn.h"
#include "ssl_private.h"

#pragma warning(push)
#pragma warning(disable : 4201)
#include <winternl.h>
#include <psapi.h>
#pragma warning(pop)


static CRITICAL_SECTION dll_critical_section;   /* dll's critical section */
static HINSTANCE        dll_instance = NULL;
static SYSTEM_INFO      dll_system_info;
static HANDLE           h_kernel = NULL;
static HANDLE           h_ntdll  = NULL;

typedef BOOL (WINAPI *pfnGetSystemTimes)(LPFILETIME, LPFILETIME, LPFILETIME);
typedef NTSTATUS (WINAPI *pfnNtQuerySystemInformation)(SYSTEM_INFORMATION_CLASS, PVOID, ULONG, PULONG);

static pfnGetSystemTimes fnGetSystemTimes = NULL;
static pfnNtQuerySystemInformation fnNtQuerySystemInformation = NULL;

BOOL
WINAPI
DllMain(
    HINSTANCE instance,
    DWORD reason,
    LPVOID reserved)
{

    switch (reason) {
        /** The DLL is loading due to process
         *  initialization or a call to LoadLibrary.
         */
        case DLL_PROCESS_ATTACH:
            InitializeCriticalSection(&dll_critical_section);
            dll_instance = instance;
            GetSystemInfo(&dll_system_info);
            if ((h_kernel = LoadLibrary("kernel32.dll")) != NULL)
                fnGetSystemTimes = (pfnGetSystemTimes)GetProcAddress(h_kernel,
                                                            "GetSystemTimes");
            if (fnGetSystemTimes == NULL) {
                FreeLibrary(h_kernel);
                h_kernel = NULL;
                if ((h_ntdll = LoadLibrary("ntdll.dll")) != NULL)
                    fnNtQuerySystemInformation =
                        (pfnNtQuerySystemInformation)GetProcAddress(h_ntdll,
                                                "NtQuerySystemInformation");

                if (fnNtQuerySystemInformation == NULL) {
                    FreeLibrary(h_ntdll);
                    h_ntdll = NULL;
                }
            }
            break;
        /** The attached process creates a new thread.
         */
        case DLL_THREAD_ATTACH:
            break;

        /** The thread of the attached process terminates.
         */
        case DLL_THREAD_DETACH:
            break;

        /** DLL unload due to process termination
         *  or FreeLibrary.
         */
        case DLL_PROCESS_DETACH:
            /* Make sure the library is always terminated */
            apr_terminate();
            if (h_kernel)
                FreeLibrary(h_kernel);
            if (h_ntdll)
                FreeLibrary(h_ntdll);
            DeleteCriticalSection(&dll_critical_section);
            break;

        default:
            break;
    }

    return TRUE;
    UNREFERENCED_PARAMETER(reserved);
}


TCN_IMPLEMENT_CALL(jboolean, OS, is)(TCN_STDARGS, jint type)
{
    UNREFERENCED_STDARGS;
#ifdef _WIN64
    if (type == 4)
        return JNI_TRUE;
    else
#endif
    if (type == 3)
        return JNI_TRUE;
    else
        return JNI_FALSE;
}

TCN_IMPLEMENT_CALL(jint, OS, info)(TCN_STDARGS,
                                   jlongArray inf)
{
    MEMORYSTATUSEX ms;
    ULONGLONG st[4];
    FILETIME ft[4];
    PROCESS_MEMORY_COUNTERS pmc;
    jint rv;
    int i;
    jsize ilen = (*e)->GetArrayLength(e, inf);
    jlong *pvals = (*e)->GetLongArrayElements(e, inf, NULL);

    if (ilen < 16) {
        return APR_EINVAL;
    }
    for (i = 0; i < 16; i++)
        pvals[i] = 0;

    ms.dwLength = sizeof(MEMORYSTATUSEX);

    UNREFERENCED(o);
    if (GlobalMemoryStatusEx(&ms)) {
        pvals[0] = (jlong)ms.ullTotalPhys;
        pvals[1] = (jlong)ms.ullAvailPhys;
        pvals[2] = (jlong)ms.ullTotalPageFile;
        pvals[3] = (jlong)ms.ullAvailPageFile;
        /* Slots 4 and 5 are for shared memory */
        pvals[6] = (jlong)ms.dwMemoryLoad;
    }
    else
        goto cleanup;

    memset(st, 0, sizeof(st));

    if (fnGetSystemTimes) {
        if ((*fnGetSystemTimes)(&ft[0], &ft[1], &ft[2])) {
            st[0] = ((ft[0].dwHighDateTime << 32) | ft[0].dwLowDateTime) / 10;
            st[1] = ((ft[1].dwHighDateTime << 32) | ft[1].dwLowDateTime) / 10;
            st[2] = ((ft[2].dwHighDateTime << 32) | ft[2].dwLowDateTime) / 10;
        }
        else
            goto cleanup;
    }
    else if (fnNtQuerySystemInformation) {
        BYTE buf[2048]; /* This should ne enough for 32 processors */
        NTSTATUS rs = (*fnNtQuerySystemInformation)(SystemProcessorPerformanceInformation,
                                           (LPVOID)buf, 2048, NULL);
        if (rs == 0) {
            PSYSTEM_PROCESSOR_PERFORMANCE_INFORMATION pspi = (PSYSTEM_PROCESSOR_PERFORMANCE_INFORMATION)&buf[0];
            DWORD i;
            /* Calculate all processors */
            for (i = 0; i < dll_system_info.dwNumberOfProcessors; i++) {
                st[0] += pspi[i].IdleTime.QuadPart / 10;
                st[1] += pspi[i].KernelTime.QuadPart / 10;
                st[2] += pspi[i].UserTime.QuadPart / 10;
            }
        }
        else
            goto cleanup;
    }
    pvals[7] = st[0];
    pvals[8] = st[1];
    pvals[9] = st[2];

    memset(st, 0, sizeof(st));
    if (GetProcessTimes(GetCurrentProcess(), &ft[0], &ft[1], &ft[2], &ft[3])) {
        FileTimeToAprTime((apr_time_t *)&st[0], &ft[0]);
        st[1] = ((ft[2].dwHighDateTime << 32) | ft[2].dwLowDateTime) / 10;
        st[2] = ((ft[3].dwHighDateTime << 32) | ft[3].dwLowDateTime) / 10;
    }
    pvals[10] = st[0];
    pvals[11] = st[1];
    pvals[12] = st[2];

    if (GetProcessMemoryInfo(GetCurrentProcess(), &pmc, sizeof(pmc))) {
        pvals[14] = pmc.WorkingSetSize;
        pvals[14] = pmc.PeakWorkingSetSize;
        pvals[15] = pmc.PageFaultCount;
    }

    (*e)->ReleaseLongArrayElements(e, inf, pvals, 0);
    return APR_SUCCESS;
cleanup:
    rv = apr_get_os_error();
    (*e)->ReleaseLongArrayElements(e, inf, pvals, 0);
    return rv;
}

static DWORD WINAPI password_thread(void *data)
{
    tcn_pass_cb_t *cb = (tcn_pass_cb_t *)data;
    MSG msg;
    HWND hwnd = CreateDialog(dll_instance, MAKEINTRESOURCE(1001), NULL, NULL);
    if (hwnd != NULL)
        ShowWindow(hwnd, SW_SHOW);
    else  {
        ExitThread(1);
        return 1;
    }
    while (1) {
        if (PeekMessage(&msg, hwnd, 0, 0, PM_REMOVE)) {
            if (msg.message == WM_KEYUP) {
                int nVirtKey = (int)msg.wParam;
                if (nVirtKey == VK_ESCAPE) {
                    DestroyWindow(hwnd);
                    break;
                }
                else if (nVirtKey == VK_RETURN) {
                    HWND he = GetDlgItem(hwnd, 1002);
                    if (he) {
                        int n = GetWindowText(he, cb->password, SSL_MAX_PASSWORD_LEN - 1);
                        cb->password[n] = '\0';
                    }
                    DestroyWindow(hwnd);
                    break;
                }
            }
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
        else
            Sleep(100);
    }
    ExitThread(0);
    return 0;
}

int WIN32_SSL_password_prompt(tcn_pass_cb_t *data)
{
    DWORD id;
    HANDLE thread;
    /* TODO: See how to display this from service mode */
    thread = CreateThread(NULL, 0,
                password_thread, data,
                0, &id);
    WaitForSingleObject(thread, INFINITE);
    CloseHandle(thread);
    return (int)strlen(data->password);
}


