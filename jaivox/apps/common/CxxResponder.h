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
