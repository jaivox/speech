/*
   Jaivox version 0.5 August 2013
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

#ifndef _CxxServerDefined
#define _CxxServerDefined

// c++ headers
#include <cstdio>
#include <pthread.h>

// standard C headers
#include <stdlib.h>
#include <signal.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <resolv.h>
#include <dirent.h>
#include <string.h>
#include <netinet/in.h>			// internet constants
#include <netdb.h>				// for struct hostent
#include <unistd.h>				// for gethostname

#include "CxxSocket.h"
#include "CxxThread.h"
#include "CxxSession.h"

class CxxServer : public CxxThread {
public:
    CxxServer (char* host, int port);
    ~CxxServer ();

    void Initialize (char* host, int port);

    void RunAction ();
    void OnAccept (CxxSocket *link);
	void execute (char* command, char* arg1, char* arg2);

    int Valid;
    int Gotmessage;
    CxxServerSocket* Server;
    char* Host;
    int Port;
    char* buffer;
};

#endif


