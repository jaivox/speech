
#ifndef _CxxThreadDefined
#define _CxxThreadDefined

#define DefaultSleepTime 10L

class CxxThread {
public:

    CxxThread ();
    ~CxxThread ();

    // void* ThreadStart (void* pThread);
    // void* startThread (void* pThread);
    int Run ();
    virtual void RunAction ();

    void Start ();
    void Stop ();
    void Pause ();
    void Resume ();

protected:
    int ThreadId;
    int Paused;
    int Killed;
    long SleepTime;

};

#endif

