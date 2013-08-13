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

#include <festival/festival.h>

#include "CxxResponder.h"

/*
// make same as in CxxSession.cc
char terminateMessage [] = "JviaTerminate";
char requestUwho [] = "JviaWho";
char invalidMessage [] = "JviaInvalid";
char responseMessage [] = "JviaResponse";
char finishedMessage [] = "JviaFinished";
char requestFestival [] = "JviaFestival";
*/

extern char
    terminateMessage [],
    requestUwho [],
    invalidMessage [],
    responseMessage [],
    finishedMessage [],
    requestFestival [];

char spokenFestival [] = "{action: spoken, from: festival, to: inter, message: \"spoke it\"}";
char terminateReply [] = "{action: JviaTerminate, from: festival, to:inter, message: \"terminated\"}";
char standardReply  [] = "{action: JviaResponse, from: festival, to:inter, message: \"standard reply\"}";
char whoReply       [] = "{action: JviaResponse, from: festival, to:inter, message: \"I am festival\"}";
char invalidReply   [] = "{action: JviaInvalid, from: festival, to:inter, message: \"Invalid request\"}";


static int wavCount = 1;
int festival_initialized = 0;

CxxResponder::CxxResponder (CxxSession* owner) {
    Owner = owner;
}

CxxResponder::CxxResponder () {
    Owner = NULL;
}

CxxData* CxxResponder::Respond (char* input) {
    CxxData* pjd = new CxxData (input);
    char* action = pjd -> action;
    CxxData* preply = new CxxData ();

    if (strcmp (action, "speak") == 0) {
        char* message = pjd -> message;
        handleFestival (message);
        preply -> parse (spokenFestival);
        return preply;
    }
    else if (strcmp (action, terminateMessage) == 0) {
        preply -> parse (terminateReply);
        return preply;
    }
    else if (strcmp (action, requestUwho) == 0) {
        preply -> parse (whoReply);
        return preply;
    }
	else if (strcmp (action, responseMessage) == 0) {
		fprintf (stdout, "No response required: %s\n", input);
		return NULL;
	}
    else {
        preply -> parse (invalidReply);
        return preply;
    }
}

void CxxResponder::handleFestival (char* message) {
    fprintf (stdout, "Calling festival to say: %s\n", message);

    EST_Wave wave;
    int heap_size = 210000; // default scheme heap size
    int load_init_files = 1; // we want the festival init files loaded

	if (!festival_initialized) {
	    festival_initialize (load_init_files, heap_size);
		festival_initialized = 1;
	}
	festival_say_text (message);
    festival_wait_for_spooler ();
    printf ("Festival completed processing: %s\n", message);
}

