
#ifndef _iaResponderDefined
#define _iaResponderDefined

#include <stdio.h>
#include <string.h>
#include "CxxSession.h"
#include "CxxData.h"

class CxxSession;

class CxxResponder {
public:
    CxxResponder (CxxSession* owner);
    CxxResponder ();

	CxxData* Respond (char* input);

	void handleFestival (char* message);

    CxxSession* Owner;

	char result [1024];
};

#endif
