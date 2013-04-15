
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

