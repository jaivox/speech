/*
   Copyright 2010-2012 by Bits and Pixels, Inc.

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

