
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

char spokenFestival [] = "{action: spoken, from: PATsynthesizer, to: PATinterpreter, message: \"spoke it\"}";
char terminateReply [] = "{action: JviaTerminate, from: PATsynthesizer, to:PATinterpreter, message: \"terminated\"}";
char standardReply  [] = "{action: JviaResponse, from: PATsynthesizer, to:PATinterpreter, message: \"standard reply\"}";
char whoReply       [] = "{action: JviaResponse, from: PATsynthesizer, to:PATinterpreter, message: \"I am festival\"}";
char invalidReply   [] = "{action: JviaInvalid, from: PATsynthesizer, to:PATinterpreter, message: \"Invalid request\"}";


static int wavCount = 1;

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

    festival_initialize (load_init_files, heap_size);

    festival_wait_for_spooler ();
    printf ("Festival completed processing: %s\n", message);
}

