
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


