
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




