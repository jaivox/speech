
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

