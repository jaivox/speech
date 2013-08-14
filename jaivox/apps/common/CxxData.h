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

#ifndef _CxxDataDefined
#define _CxxDataDefined

#include <stdio.h>
#include <ctype.h>
#include <string.h>

class CxxData {
public:
	CxxData (char* input);
	CxxData ();

	void parse (char* input);

	char *action;
	char *from;
	char *to;
	char *message;

	char* toText ();

private:
    int issep (char c);
    char* parseValue (char* line, char* tag);
};

#endif

