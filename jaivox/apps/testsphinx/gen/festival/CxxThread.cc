/*
   Jaivox version 0.4 April 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h> // needed?
#include <pthread.h>
#include <unistd.h>

#include "CxxThread.h"

/*
This is a simple thread class that uses the _beginthreadex function
to start the thread.  To use, create the class, then call the
Start function.  The Pause, Resume and Stop functions exist as
in equivalent java.lang.Thread class.
 */

static int threadNumber = 0;

CxxThread::CxxThread () {
    ThreadId = ++threadNumber;
    Paused = 1; // it is running now
    Killed = 0;
    SleepTime = DefaultSleepTime;
    // Info message
    fprintf (stdout, "Thread %d started\n", threadNumber);
}

CxxThread::~CxxThread () {
    ThreadId = ++threadNumber;
    // Info message
    fprintf (stdout, "Thread %d killed\n", ThreadId);
}

/*
The run function itself is not used in derived classes since it contains
the variables required for controlling action.  The Run action is done
in the RunAction () function.  RunAction is called repeatedly while the
Thread is active.
 */

int CxxThread::Run () {

    while (!Killed) {
        sleep (0.1);
        if (Paused)
            continue;
        // do other things in derived classes
        RunAction ();
    }
    pthread_exit (&ThreadId);
    // Info message
    fprintf (stdout, "Thread %d ended\n", ThreadId);
    return 0;
}

void CxxThread::RunAction () {
    // do things in derived classes
}

/*
void* CxxThread::ThreadStart (void* pThread)
{
    CxxThread* p = static_cast<CxxThread*> (pThread);
    if (p != NULL)
        return p->Run ();
    else
        return 0;
}
 */

// void* CxxThread::startThread (void* pThread) {

void* startThread (void* pThread) {
    long threadloc = (long) pThread;
}

void CxxThread::Start () {
    pthread_t Addr;
    pthread_create (&Addr, 0, &startThread, &Addr);
    Paused = 0;
	Run ();
}

void CxxThread::Pause () {
    if (!Killed)
        Paused = 1;
}

void CxxThread::Resume () {
    if (!Killed)
        Paused = 0;
}

void CxxThread::Stop () {
    Paused = 1;
    Killed = 1;
}

