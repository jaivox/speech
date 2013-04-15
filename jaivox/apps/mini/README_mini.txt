
This directory contains five simple programs. These show how we can create
a variety of speech interpreters starting with simple specifications.

The programs here are as follows:

cold: is a conversation about a patient talking to a doctor.

course: is a student talking to an advisor.

fire: is a caller talking to a 911 operator about fire.

insure: is someone asking about car insurance options.

tomato: is a conversation about growing tomatoes in central Texas.

The explanation below is about the "cold" conversation.

To generate the program

java com.jaivox.tools.Jvgen cold.conf

This creates a subdirectory withe the name "cold". Change to that
directory.

The logic of the conversation in this case is captured in a file
spec/cold.dlg. This logic can also be explained by the following
if - then structure:

IF user = i am feeling sick THEN System = do you feel hot and THEN
	IF user = a little THEN System = is your nose runny and THEN
		IF user = yes THEN System = how is your throat and THEN
			IF user = itchy THEN System = it is probably just a cold
			ELSE IF user = hurts THEN System = could be an infection. are you coughing and THEN
				IF user = a lot THEN System = go to a doctor
				ELSE IF user = a little THEN System = may be best to go to a doctor
				ELSE IF user = no THEN System = try drinking some fluids
			ELSE IF user = fine THEN System = are you wheezing and THEN
				IF user = yes THEN System = a lot or just a little and THEN
					IF user = a lot THEN System = could be asthma
					ELSE IF user = a little THEN System = could be allergies
		ELSE IF user = no THEN System = do you feel congested and THEN
			IF user = yes THEN System = did you have a cold earlier and THEN
				IF user = yes THEN System = well give it some time
			ELSE IF user = no THEN System = are you allergic to anything and THEN
				IF user = yes THEN System = what and THEN
					IF user = pollen THEN System = well it is the season
					ELSE IF user = dust THEN System = try cleaning your house
					ELSE IF user = dogs THEN System = do you have a dog and THEN
						IF user = no THEN System = did you visit somebody who has a dog and THEN
							IF user = no THEN System = well come in for an evaluation and THEN
							ELSE IF user = yes THEN System = that could be it

The questions and answers are tabbed so that you can tell which ELSE goes with
which IF. The things that the user can say are prefixed with user = .
Note that the user may say "yes" in several different contexts depending on
the previous questions and answers.

You can create this conversation by running

javac terminalTest.java
then
java terminalTest

in the cold directory

Here is one interaction that follows the above logic. The statements with >
are user's input (that is typed on the console), the statements with : are
the system responses.

Use control-C to end the program.

> i am feeling sick
: do you feel hot
> a little
: is your nose runny
> yes
: how is your throat
> itchy
: it is probably just a cold

Note that this application cannot handle variations in the wording of the
questions. The programs using the road.dlg grammar contains ways to handle
grammatic variations. This program does not interface with any data. Again
the program in the "1st" directory contains an interface to data.

You can create the other applications in the same way. You need to create
a .conf file for creating that application. This conf file should use the
appropriate dlg file to generate a conversation that follows that dialog
grammar. We have included fire.conf here so that you can generate the fire
application using Jvgen.

