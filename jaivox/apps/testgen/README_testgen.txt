
This directory contains five test configuration files. These test
various options available in the application generator

To generate from one of these configuaration files,
say lowfree.conf

java com.jaivox.tools.Jvgen lowfree.conf

The different configuration files genearate differen types of
applications.

bmsfest.conf	batch multiagent sphinx and festival system

console.conf	console application that does not use speech recognition or tts

lowespk.conf	live one directory web-based recognizer using espeak tts

lowfree.conf	live one directory web-based recognizer using freetts

lowgoog.conf	live one directory web-based recognizer using Google tts.

Here web-based recognition uses Google's unofficial API.
