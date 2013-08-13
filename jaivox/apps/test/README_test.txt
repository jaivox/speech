
The files here are for the detailed tutorial
http://www.jaivox.com/tutorial.html

The example here illustrates an application where a user may ask
about the conditions of various roads. Questions involve two properties of
roads, whether they are fast, and whether they are smooth (meaning, good
surface, not too many lights etc.)

To generate the three agents that will work together, change to the

bin

directory (this is created when you installed by running ant) and enter

java com.jaivox.tools.Jvgen path_to_apps_test/test.conf

(In general, refer to test.conf as being in the test directory to make
sure that the paths in test.conf are used correctly.)

The example shows how you can generate the programs for the recognizer
(using google, in a directory gen/asr), for the interpreter (in
gen/inter) and for the speech synthesizer using google tts (gen/tts).

Even though this example is simple, you can expand the architecture
of this application to support many recognizers, each of which handle
some specific range of questions.

For more details, please see http://www.jaivox.com/tutorial.html.
