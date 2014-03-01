
This directory contains several sample applications along with some
data for these applications. This file is based on Jaivox version 0.5
(August, 2013).

The directories are as follows:

1stsphinx: consists of a Sphinx-based recognizer application.
	First try to see if your installed files and classpaths are as they
	should be. This directory contains a batch test that recognizes recoreded
	questions from the audio directory. To answer the questions, batchTest
	uses a customized class that answers qualitative questions about data.
	It uses freetts to convert the answers to spoken sounds.

1stweb: uses Gogole's web-based speech recognizer and audio files in the
	"flac" format. There are two main programs here. roadWeb.java uses
	Google's web-based text to speech system while roadWebFreetts.java uses the
	Freetts text to speech package.
	
	Note: Google's speech recognizer and speech synthesis are not part of
	any official API, nor is it open source. It probably will not be available
	after a short period.
	
1stwebde: uses Google's speech recognizer and synthesizer in German, otherwise
	the program is like 1stweb's roadWeb.java program.
	
1stwebes: uses Google's speech recognizer and synthesier in Spanish, otherwise
	this is just like 1stwebde.

audio: consists of several recorded questions. You can use these in the 1st
	demo to evaluate sphinx recognition and jaivox interpretation
	without using a microphone.
	
audio_de: audio files used for 1stwebde. The "flac" format files are used for
	recognition, but the "wav" files are used for playing the recording.
	
audio_es: audio files similar to audio_de, but for Spanish.

common: there are several template files that are used in generating applications. This
	directory contains all these files.

compareasr: compares google's speech recognition and Sphinx4 recognition
	on the same data.

db: is an application that queries the cookie sqlite database (this is created by the
	mozilla browser.)

find: is another application that searches files using the "find" command.

fixerrors: this is an application that uses the version 0.7 features to
	match recognized strings by comparing phonemes in the string. This
	uses a table of results obtained by Julius Adorf, please see the page
	http://www.jaivox.com/phoneticdistance.html for details.

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

student: is an application that demonstrates the use of some grammar enhancements for
	better questions. The application involves asking questions about students (from
	a teacher or administrator's viewpoint.)

test: is the application discussed in the detailed tutorial. It involves asking questions
	about roads. This vesion uses the Google recognizer and synthesizer in multiple
	agents.
	
testgen: this contains several different configuration files to illustrate options
	in generating applications.
	
testsphinx: this is the same as the test application, but using Sphinx.

voxforge_de: this uses the Voxforge German audio model and is structured
	similar to voxforge_es. Unlike voxforge_es though, this program does not recognize
	anything in our experiements. It is included here just to note how the different
	configuration files should be strucutred.

voxforge_es: this shows how Jaivox dialogs can be adapted to another language. This
	example adpats the files in the test example to Spanish. Corresponding to
	files road.spec, errors.dlg and road.dlg, here we have road_es.spec,
	errors_es.dlg and road_es.dlg. The program live.java in the es subdirectory
	is generated with Jvgen with input spanish.conf. The program is then modified
	to use a configuration script live_es.xml which points to the Voxforge
	Spanish audio model and dictionary. See README_voxforge_es.txt for more
	details.

The 1st*, db and find applications have some customized files. The rest of the
applications are generated using a generator in the tools package. There are
several configuration (.conf) files in the directories, as well as related information
in various spec directories, that are used to generate the applications.

Incidentally, The *.conf files in the applications can be referenced from various
locations.

You can generate a test program by going to the test directory and running

java com.jaivox.tools.Jvgen test.conf

This will generate three directories

gen/asr		containing the speech recognizer
gen/inter	containing the interpreter for dialogs
gen/tts		containing the text to speech system.

You can compile the programs in each directory with a "javac *.java".

There are several other options for the recognizer and the text to speech. The
testgen directory contains five configuration files that cover all the different
options. These show some specific combinations, for example a single program that uses
the web (i.e. for now Google) recognizer with an espeak synthesizer. But you can change
parameters here so that for example you can have a multiagent program using both the
Google recognizer and the Google text to speech.

IMPORTANT: If you are using freetts, note the informaton in the README_tts.txt
about the specification of freetts.jar in the classpath. If you are using Google's
web-based text to speech, please download and install the jLayer library from
Javazoom http://www.javazoom.net






