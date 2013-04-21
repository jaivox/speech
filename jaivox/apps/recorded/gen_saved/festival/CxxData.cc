
#include "CxxData.h"

char actiontag [] = "action:";
char fromtag [] = "from:";
char totag [] = "to:";
char messagetag [] = "message:";
char empty [] = "none";

CxxData::CxxData (char* input) {
    action = NULL;
    from = NULL;
    to = NULL;
    message = NULL;
    parse (input);
}

CxxData::CxxData () {
    action = NULL;
    from = NULL;
    to = NULL;
    message = NULL;
}

void CxxData::parse (char* input) {
	action = parseValue (input, actiontag);
	from = parseValue (input, fromtag);
	to = parseValue (input, totag);
	message = parseValue (input, messagetag);
}

int CxxData::issep (char c) {
	if (isspace (c)) return 1;
	if (c == '{' || c == '}' || c == ',') return 1;
	return 0;
}

char* CxxData::parseValue (char* line, char* tag) {
	int ntag = strlen (tag);
	int nline = strlen (line);
	int nvalue = 0;
	int inquote = 0;
	int started = 0;
	int i, start, end;
	char c;
	char* value;

	char* pos = strstr (line, tag);
	if (pos == NULL) {
		// printf ("Tag %s not found in %s\n", tag, line);
		return empty;
	}

	start = (int)(pos - line) + ntag;

	for (i=start; i<nline; i++) {
		c = line [i];
		if (c == '\"') {
			inquote = 1;
			started = 1;
			start = i;
			break;
		}
		else if (!issep (c)) {
			started = 1;
			start = i;
			break;
		}
	}
	if (!started) {
		// printf ("Cannot find start of value after %tag after %d\n", tag, start);
		return empty;
	}

	// printf ("value for %s starts at %s\n", tag, line+start);
	if (inquote) {
		// printf ("value for %s is quoted\n", tag);
	}
	end = -1;
	for (i=start; i<nline; i++) {
		c = line [i];
		if (inquote && i>start) {
			if (c == '\"') {
				end = i+1;
				break;
			}
		}
		else {
			if (issep (c)) {
				end = i;
				break;
			}
		}
	}
	if (end == -1) {
		// printf ("tag %s Cannot find end after %d in %s\n", tag, start, line+start);
		return empty;
	}
	// printf ("value for %s ends at %s\n", tag, line+end);
	nvalue = end - start;
	value = new char [nvalue + 1];
	strncpy (value, line+start, nvalue);
	value [nvalue] = '\0';

	// printf ("Value for tag %s is %s\n", tag, value);
	return value;
}

char* CxxData::toText () {
    char* text = new char [1024];
    strcpy (text, "{action: ");
    strcat (text, action);
    strcat (text, ", ");
    strcat (text, "from: ");
    strcat (text, from);
    strcat (text, ", ");
    strcat (text, "to: ");
    strcat (text, to);
    strcat (text, ", ");
    strcat (text, "message: ");
    strcat (text, message);
    strcat (text, "}");
    return text;
}

