
(from http://www.jaivox.com/recordeddemo.html)

Recorded Demo

Jaivox applications typically require a speaker to talk through a microphone. The Sphinx open source speech recognizer used as default generally works best with a noise canceling microphone. But you can test a Jaivox application without such a microphone, indeed without any microphone.

The downloads include a demo program that does not require a microphone. This uses some recorded questions that are then sent through the application just as if a speaker was asking them. You can play the recordings to see what they sound like. When you run them through the demo, each recording is sent through Sphinx. The recognition is almost completely accurate. (Incidentally, the best results are obtained with a native speaker of American English, though we get somewhat reasonable results even with non-native speakers.)

To use the demo, you need to download and install Sphinx 4. the Sphinx classes should be in your classpath. Sphinx will normally include the trained speech model WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz needed for the recognition. To make sure that you have installed Sphinx correctly, run the Transcriber demo included with Sphinx.

After installing Sphinx and making sure that the Sphinx libraries are in your classpath, you can run the recorded demo. If you have not done so, download the Jaivox files using downloads. The recorded demo is included in a folder named recorded.

The recorded files are in a directory called audio. You can listen to them to see what they sound like. There is a slight hissing noise in these introduced by the noise canceling microphone. This however seems to make the audio more understandable to Sphinx.

To run the demo, you need the files in recorded/gen. There are three subdirectories, each includes an independent program.

1. The sphinx directory reads some of the recorded files from audio and recognizes the audio. The recognized audio is passed through sockets to the interpreter.

2. The inter directory receives the recognized question and figures out the answer, then passes the answer to the speech synthesizer in the festival directory. 

3. The festival directory uses the festival program (normally included in Linux distributions) to synthesize the answer and speak the answer.

This process is repeated with each of the questions considered in the demo.

To run the demo, create three command windows. 

In each window, make sure that both the sphinx libraries and the Jaivox libraries are in the classpath.

In the first window, cd to the sphinx directory of the demo.
In the second window, cd to the inter directory of the demo.
In the third window, cd to the festival directory of the demo.

Now you need to compile the programs.

In the first window, enter javac sphinxBatchTest.java
In the second window, enter javac interTest.java
In the third window, enter make. This will build an executable program festivaltest. Note that this is a C++ program unlike the other two windows. There is a makefile in this directory that makes the executable program.

It is important to do the following in the order given below

First, in the inter window, start the program with java interTest

Second, in the festival window, enter ./festival

Wait for about 10 seconds while the festival program in the third window establishes a socket connection with the interpreter program in the second window. You will see some messages printed in the second and third windows as the connection between the two are established.

Now in the first window, enter java sphinxBatchTest.

This will cause some activity in the first and second window as connection between sphinx and the interpreter are established.

Soon after establishing connection, the first window will read the first recording as mentioned in the program, recognize it using the sphinx libraries, then send the recognized question to the second window. The second window will interpret the question, create an answer and send it to the third window. If you have the speakers on, you should be able to hear the questions and the answers.

(In this demo, we use a Java function to play recorded sounds. This conflicts with Festival's sound output. To get around this problem, we record the festival response in a file wave.wav in the festival directory. When running the demo, the Sphinx program sends the recognized text to inter, then waits for four seconds, then plays both the recorded question from the human speaker and then the sound of Festival's synthesized speaker answering the question.)

This process will continue with another nine questions. After this the program in the first window will end on its own. You can end the demo by using Control-C to kill the program in the third window, and then in the second window.
