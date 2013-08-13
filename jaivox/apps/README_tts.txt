
Jaivox can use four different text to speech systems (as of August, 2013.)

1. Festival is a C-based system for Linux
2. Freetts is a java-based open source package
3. Espeak can be used as an executable program on various platforms
4. Google's web-based system can be used (for now)

1. Festival
-----------
This is installed by default on most Linux systems and may not be available
in other platforms. It is C-based, thus the most natural way to connect it
to Jaivox is to use some C++ agents that are part of the Jaivox library.

2. Freetts
-----------
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

Our applications assume that freetts.jar (along with other files in the freetts
package) are copied to /usr/local/freetts.

Assuming the library has been built, include the following in your classpath

	/usr/localfreetts/lib/freetts.jar

(For example, if your classpath previously was ".:/myjavadir/myjavaclasses" now
it will be ".:/myjavadir/myjavaclasses:/usr/local/freetts/lib/freetts.jar")

Alternately, you can use the compbatch.sh complive.sh runbatch.sh and runlive.sh
files that are created when applications are generated using Jvgen in the
jaivox.tools package. The location of freetts.jar can be specified in the
*.conf files that are used by Jvgen.

It is IMPORTANT to have the freetts.jar location separately specified in
the classpath. Freetts uses the location of freetts.jar to locate some of
the voice specification files it needs for speaking.

3. Espeak
---------
Espeak is usually available in /usr/bin/espeak on Linux systems. It is also
available for other platforms. Jaivox uses this directly as an executable.

4. Google's web-based TTS
--------------------------
This is part of the Google translation service. While the translation itself
is a paid service, the TTS seems to be available directly through a web-based
call.

To use this in a library and to hear things directly spoken by the tts through
a Java program, we found it convenient to use the jLayer library from
javazoom. Please install jLayer from http://www.javazoom.net and make the
classes available in your classpath.

