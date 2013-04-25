

#include "CxxServer.h"
#define MAX_SESSIONS 10

char localhost [] = "localhost";

CxxSession* sessions [MAX_SESSIONS];
int nSession;

int connected = 0;
int identified = 0;

CxxServer::CxxServer (char* host, int port) : CxxThread () {
    Host = host;
    Server = NULL;
    nSession = 0;
    Initialize (host, port);
}

CxxServer::~CxxServer () {
    delete Server;
}

void CxxServer::Initialize (char*host, int port) {
    Port = port;
    fprintf (stdout, "CxxServer created with port %d\n", port);
    Start ();
    Run ();
}

void CxxServer::RunAction () {

    char* hostname = new char [256];
    int err = 0;
    if (err != 0) {
        strcpy (hostname, "localhost");
    } else {
        gethostname (hostname, 256);
    }
	int initialized = 0;

	Server = new CxxServerSocket (hostname, Port);
    if (Server->Create ()) {
        // get the host name
        fprintf (stdout, "Listening to %d\n", Port);

		while (true) {
			if (!initialized) {
		// initial steps
				sleep (0.1);
		// char* casts needed to get over g++ deprecation warning
				execute ((char*)"connect", (char*)"localhost",
						(char*)"3000");
				initialized = 1;
				fprintf (stdout, "nSession = %d\n", nSession);
				CxxSession* session = sessions [0];
				session -> Start ();
				session -> Run ();
				continue;
			}

            CxxSocket *link = Server->Accept ();
            if (link != NULL) {
                OnAccept (link); // should we check whether Killed or Paused?
            }else
                break;
            sleep (0.1);
        }
    }
    Stop ();
}

void CxxServer::OnAccept (CxxSocket *link) {

    CxxSession *session;
    session = new CxxSession (this, link);
    session->Start ();

    // info message
    fprintf (stdout, "Created new session, id %d\n", session->sessionId);
}

// some of the args may be null
void CxxServer::execute (char* command, char* arg1, char* arg2) {
	if (strcmp (command, "connect") == 0) {
		if (nSession >= MAX_SESSIONS) {
			fprintf (stdout, "No more sessions, max is %d\n", MAX_SESSIONS);
			return;
		}
		int port = atoi (arg2);
		CxxSocket* link = new CxxSocket (arg1, port);
		int result = link->openSocket (arg1, port);
		if (result == 0) {
			return;
		}
		CxxSession* session = new CxxSession (this, link);
		fprintf (stdout, "Session %d started to %s:%d\n",
				 nSession, arg1, port);
		sessions [nSession++] = session;
		return;
	}
	else if (strcmp (command, "disconnect") == 0) {
		int sessionNumber = atoi (arg1);
		if (sessionNumber >= nSession) {
			fprintf (stdout, "No session %d, last session is %d\n",
					 sessionNumber, nSession);
			return;
		}
		CxxSession* session = sessions [sessionNumber];
		session->Close ();
		return;
		// we won't recover the closed session's slot
	}
	else if (strcmp (command, "send") == 0) {
		int sessionNumber = atoi (arg1);
		if (sessionNumber >= nSession) {
			fprintf (stdout, "No session %d, last session is %d\n",
					 sessionNumber, nSession);
			return;
		}
		CxxSession* session = sessions [sessionNumber];
		CxxSocket* socket = session->Link;
		int nchar = strlen (arg2) + 1;
		int sent = socket->sendData (arg2, nchar);
		if (sent == 0) {
			fprintf (stdout, "Could not send %s\n", arg2);
		}
		return;
	}
	else {
		fprintf (stdout, "Command %s is not yet implemented\n", command);
		return;
	}
}


int main () {
    CxxServer* server = new CxxServer (localhost, 4000);
    return 1;
}

