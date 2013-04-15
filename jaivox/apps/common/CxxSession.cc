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

#include "CxxSession.h"

#define BSIZE 4096
#define LSIZE 1024

int MaxWaitPeriods = 10;

// make same as CxxResponder.cc
char terminateMessage [] = "JviaTerminate";
char requestUwho [] = "JviaWho";
char invalidMessage [] = "JviaInvalid";
char responseMessage [] = "JviaResponse";
char finishedMessage [] = "JviaFinished";
char requestFestival [] = "JviaFestival";

char who [] = "{action: JviaWho, from: PATsynthesizer, to: PATinterpreter, message: \"JviaWho\"}";

static int Count;

CxxSession::CxxSession (CxxServer* own, CxxSocket *link) {
    Owner = own;
    Link = link;

    Responder = new CxxResponder (this);
    Count++;
    sessionId = Count;
}

CxxSession::CxxSession () : CxxThread () {
    Responder = new CxxResponder (this);
    Count++;
    sessionId = Count;
}

int CxxSession::GetPort () {
    return ntohs (Link->addr.sin_port);
}

int CxxSession::Close () {
    Link -> closeSocket ();
    return 1;
}

void CxxSession::Initialize (CxxServer* own, CxxSocket *link) {
    char req[LSIZE];
    Owner = own;
    Link = link;
    if (Responder == NULL) Responder = new CxxResponder (this);
    gethostname (req, LSIZE);
    Host = req;
}


void CxxSession::RunAction () {
    int step = 0;
    int quit = 0;
	int initialized = 0;

    while (true) {
        sleep (0.1);
        // the quit flag may have been raised somewhere
        if (quit == 1) break;
		if (!initialized) {
			Owner->execute ((char*)"send", (char*)"0", (char*)who);
			fprintf (stdout, "sent %s on session 0\n", who);
			initialized = 1;
			continue;
		}
        int charsRead = readFromSocket ();
        if (charsRead > 0) {
            fprintf (stdout, "CxxSession:%d Recived %s step %d\n", sessionId, buffer, step);
            CxxData* preply = Responder->Respond (buffer);
            if (preply == NULL) continue;
            char* replytext = preply -> toText ();
            fprintf (stdout, "CxxSession:%d Response %s at step %d\n", sessionId, replytext, step);
            char* action = preply->action;
            if (strcmp (action, terminateMessage) == 0) {
                quit = 1;
                continue;
            }
            else {
                strcpy (outbuffer, replytext);
            }
        }
        if (strlen (outbuffer) > 0) {
            int charsToSend = strlen (outbuffer);
            Link -> sendData (outbuffer, charsToSend);
        }
        sleep (0.1);
        step++;
    }
    // disconnect
    fprintf (stdout, "Trying to close down Link Id = %d\n", sessionId);
    Link -> closeSocket ();
    Stop ();
}

int CxxSession::readFromSocket () {
    // wait for a while
    // for (int i = 0; i < MaxWaitPeriods; i++)
    //    sleep (1);

    int charsRead = Link -> getData (buffer, BSIZE);
    return charsRead;
}


