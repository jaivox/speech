
This directory contains three independent programs.

testGoogle.java: uses Google's online recognizer. It needs audio files in
	the FLAC format in the ../audio directory.

testSphinx.java: needs Sphinx4 and WAV audio files in the ../audio directory.
	It also uses a DMP file used in other Jaivox applications.

compare.java: is a simple program to compare the results from the two
	Java programs above.

You do not need to install Jaivox libraries to compile or run these
programs. The testSphinx.java program compiles if Sphinx4 is installed and
available in your classpath. You do not need the CMU/Cambridge language
modeling tools since the required DMP file is provided in this directory.

testGoogle uses only the standard java.net classes, and does not use an HTTP
library such as Apache's  httpcomponents-client. The testGoogle program simply
submits a POST request to the google URL and retrieves the results in JSON
format. The recognized text is extracted from the JSON result without
parsing the JSON format.

The testGoogle program works only if the Google speech recognizer is
available at the URL specified in the program. This is not an official
API, it works as of today (May 6, 2013). However Google may change its
availability any time in the future. The actual results you get from testGoogle
may be different based on changes to Google's recognizer.

The other files in this directory are as follows:

batch.xml: configuration file used by testSphinx
google.txt: results of running testGoogle
recorded.txt: text of recordings in the ../audio directory
road.arpabo.DMP: language model used by testSphinx
sphinx.txt: results of running testSphinx

More details about the applications and results can be found at
http://www.jaivox.com/comparinggoogle.html
