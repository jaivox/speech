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

#ifndef _CxxSessionDefined
#define _CxxSessionDefined

// c++ headers
#include <cstdio>
#include <pthread.h>

// standard C headers
#include <signal.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <resolv.h>
#include <dirent.h>
#include <netinet/in.h>			// internet constants
#include <netdb.h>				// for struct hostent
#include <unistd.h>				// for gethostname

#include "CxxSocket.h"
#include "CxxThread.h"
#include "CxxServer.h"
#include "CxxResponder.h"

class CxxServer;
class CxxSocket;
class CxxThread;
class CxxResponder;

#define defaultConnectionargs 10
#define sendStringSize 1024

class CxxSession : public CxxThread {
public:
    CxxSession ();
    CxxSession (CxxServer *own, CxxSocket *link);

    int IsAlive ();
    int GetPort ();
    int Close ();

    void Initialize (CxxServer* own, CxxSocket* link);

    int readFromSocket ();
    void RunAction ();

    CxxServer* Owner;
    CxxSocket* Link;
	CxxResponder* Responder;

    char outbuffer [1024];
    int sessionId;
    char* Host;
    int Port;
    int QuitLink;
    char buffer [4096];

};

#endif




