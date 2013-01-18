
The files here are for the detailed tutorial
http://www.jaivox.com/tutorial.html

The example here illustrates an application where a user may ask
about the conditions of various roads. Questions involve two properties of
roads, whether they are fast, and whether they are smooth (meaning, good
surface, not too many lights etc.)

To generate the three agents that will work together, change to the 

bin 

directory (this is created when you installed by running ant) and enter 

java com.jaivox.tools.Jvgen test.conf

The example shows how you can generate the programs for the recognizer
(using sphinx, in a directory gen/sphinx), for the interpreter (in
gen/inter) and for the speech synthesizer using festival (gen/festival).
The festival program is in C++, the others are in Java. The exmaple
connects between the programs using sockets, so that the language
differences between Java and C++ do not matter.

Even though this example is simple, you can expand the architecture
of this application to support many recognizers, each of which handle
some specific range of questions.

For all the details, please see tutorial.html.
