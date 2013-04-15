
This directory contains a demo using recorded voices and the freetts.

Freetts is a java-based speech synthesizer. The code here used version
1.2.2.

Please download freetts from http://freetts.sourceforge.net/docs/index.php
specifically
http://sourceforge.net/projects/freetts/files/FreeTTS/FreeTTS%201.2.2/freetts-1.2.2-src.zip/download

Freetts installation and classpath set up
-----------------------------------------
Unzip the downloaded freetts-1.2.2-src.zip to a directory. For now assume
that you have unzipped the files into a directory

/home/me/freetts

and that

ls /home/me/freetts

shows the following

ANNOUNCE.txt  build.xml  demo.xml    lib            RELEASE_NOTES
bin           com        docs        mbrola         speech.properties
bld           de         index.html  overview.html  tools
build.bak     demo       javadoc     README.txt

(i.e. assume you have renamed the unzipped freetts-1.2.2 directory to be
/home/me/freetts)

You need to fix the file build.xml so that this compiles. Edit build.xml
and locate the line that says

    <property name="src_dir" value="src" />

(this is close to the top of the file.)

Change it to

    <property name="src_dir" value="." />

Now run ant to build the libraries in freetts/lib, i.e. in the /home/me/freetts
directory, enter

ant

It should complete the process with "BUILD SUCCESSFULL".

Assuming the library has been built, include the following in your classpath

	/home/me/freetts/lib/freetts.jar

(For example, if your classpath previously was ".:/myjavadir/myjavaclasses" now
it will be ".:/myjavadir/myjavaclasses:/home/me/freetts/lib/freetts.jar")

It is IMPORTANT to have the freetts.jar location separately specified in
the classpath. Freetts uses the location of freetts.jar to locate some of
the voice specification files it needs for speaking.

After installing freetts and setting classpath
----------------------------------------------
Now compile the batchTest.java file in this directory. Assuming you have the
sphinx and jaivox classes in your classpath.

java batchTest.java

should compile without complaints.

Running the demo
----------------
Assuming that freetts is set up as above, and batchTest.java compiles without
errors, you can run the demo with

javac batchTest

(Please turn on your speakers to hear the demo as it progresses.)

(the program will end automatically after going through 10 questions listed
in the program.)

You will see some warning messages on the screen, mainly from Sphinx because
it does not have all the words in the questions in its vocabulary. This is
normal, since jaivox can handle some partially recognized questions.

There will be a record of the questions and answers in a file Interact*.txt
in the current directory (the file will contain the date you are running
the program.)

The questions are in audio recordings in the ../audio directory. You can listen
to the questions independant of this program.

This demo reads each of the listed questions in the program from the audio
file, passes it through sphinx to recognize the question (the recognition may
not be perfect, but close enough), then passes it through the jaivox interpreter
to get an answer, then finally uses freetts to speak the answer.
