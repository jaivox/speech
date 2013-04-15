
Jaivox applications typically require a speaker to talk through a
microphone. The Sphinx open source speech recognizer used as default
generally works best with a noise canceling microphone. But you can test
a Jaivox application without such a microphone, indeed without any
microphone.

Details of this test can be found at http://www.jaivox.com/recordeddemo.html

The downloads include a demo program that does not require a microphone.
This uses some recorded questions that are then sent through the
application just as if a speaker was asking them. You can play the
recordings to see what they sound like. When you run them through the
demo, each recording is sent through Sphinx. The recognition is almost
completely accurate. (Incidentally, the best results are obtained with a
native speaker of American English, though we get somewhat reasonable
results even with non-native speakers.)

This demo requires running three "agents" in separate windows: one for
speech recognition, one for interpreting questions, and another for
synthesizing answers into speech. 

The description in recordeddemo.html assumes that the C/C++ festival
program is used for synthesizing speech. This may not work very well in all
Linux distributions (and in Windows, we have not been able to find a working
festival implementation for Windows.) Instead you can use synthesizerTest
in the freetts directory.  To do so, you need to download and install
freetts as described in http://www.jaivox.com/freetts.html. Here, invoke
synthesizerTest the same way you would invoke batchTest in the freetts
directory. This should be invoked at the same point in the demo as festival.
If you are using freetts, do not start festival. Please remember to set
classpath correctly for freetts as described in freetts.html.