
This directory contains several sample applications along with some
data for these applications.

The directories are as follows:

1st: First try to see if your installed files and classpaths are as they
	should be. This directory contains a batch test that recognizes recoreded
	questions from the audio directory. To answer the questions, batchTest
	uses a customized class that answers qualitative questions about data.
	It uses freetts to convert the answers to spoken sounds.

audio: consists of several recorded questions. You can use these in the 1st
	demo to evaluate sphinx recognition and jaivox interpretation
	without using a microphone.

common: there are several template files that are used in generating applications. This
	directory contains all these files.

db: is an application that queries the cookie sqlite database (this is created by the
	mozilla browser.)

find: is another application that searches files using the "find" command.

mini: this shows how the jaivox interpreter can handle a wide variety of
	conversations. There are five examples here. These examples do not contain
	sentence variations in questions (as in test, 1st or onefile) and no data
	or specifications of data. It can be tested using console programs that
	essentially step through the logic expressed in the various dialog files
	cold.dlg: is a conversation about a patient talking to a doctor.
	course.dlg: is a student talking to an advisor.
	fire.dlg: is a caller talking to a 911 operator about fire.
	insure.dlg: is someone asking about car insurance options.
	tomato.dlg: is a conversation about growing tomatoes in central Texas.
	Each of the conversations have few options, but illustrates how the
	dialog descriptions can be used for all these different conversations.

onefile: shows how the test example, which generates three agents, can instead
	be used to create batch tests and live tests.

student: is an application that demonstrates the use of some grammar enhancements for
	better questions. The application involves asking questions about students (from
	a teacher or administrator's viewpoint.)

test: is the application discussed in the detailed tutorial. It involves asking questions
	about roads.

The 1st, db and find applications have some customized files. The rest of the
applications are generated using a generator in the tools package. There are
several configuration (.conf) files in the directories, as well as related information
in various spec directories, that are used to generate the applications.

For example, assuming your classpath settings are done correctly, you can generate
the test application with

java com.jaivox.tools.Jvgen test.conf

For the student application similarly, change the student directory and enter

java com.jaivox.tools.Jvgen student.conf

In both cases, Jvgen will generate three directories in a gen directory. The three
directories are

gen/festival
gen/inter
gen/sphinx

gen/festival consists of C++ programs compiled by running "make". The other two contain
a java program each. Assuming you have the classpath set up right, you can compile them
with "javac *.java" in each directory.

An option in the .conf file can be used to generate

gen/ftts

to create a Javab-based synthesizer that uses the freetts package. To use
freetts, you must download. Please see README_freetts.txt for more information
on freetts.

IMPORTANT: If you are using freetts, note the informaton in the README_freetts.txt
about the specification of freetts.jar in the classpath.





