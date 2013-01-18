
This directory contains several sample applications along with some
data for these applications.

The directories are as follows:

audio: consists of several recorded questions. You can use these in the recorded and
	freetts demos to evaluate sphinx recognition and jaivox interpretation
	without using a microphone.
	
common: there are several template files that are used in generating applications. This
	directory contains all these files.
	
db: is an application that queries the cookie sqlite database (this is created by the
	mozilla browser.)
	
files: is another application that searches files using the "find" command.

freetts: requires downloaing the java-based text to speech system Free TTS. This is
	then used in a simple application using only one java file as source.
	
recorded: is an application that does not require a microphone, since it uses prerecorded
	questions in the audio directory. The sphinx recognition and the rest of the
	processing is the same as in other applications. This version uses festival.
	
student: is an application that demonstrates the use of some grammar enhancements for
	better questions. The application involves asking questions about students (from
	a teacher or administrator's viewpoint.)
	
test: is the application discussed in the detailed tutorial. It involves asking questions
	about roads.
	
Of the applications here, test and student are generated from the specs given. For the
test application, change to the test directory, make sure that you have the Jaivox files
in the classpath and enter

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



