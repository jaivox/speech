
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

The three agents were originally generated in the gen subdirectory.
This is renamed to gen_saved due to some modifications that were made to
give meaningful answers. The gen_save/sphinx directory was modified to
create sphinxBatchTest that goes through some questions in the apps/audio
directory. The questions are available in text form (for comparisons with
the recognized versions) in recorded.txt.  The gen_save inter directory
is modified to include several Java programs and files to give meaningful
answers from road.txt using user-defined functions "find" and "ask."
This is as in the apps/1st directory. The festival directory in
gen_save/festival is unmodified.

If you cannot hear festival, it could be due to a conflict between Java
and the audio system. This is dependent on your Linux distro and hardware.
In such cases, try using the freetts directory as noted below.

The description in recordeddemo.html assumes that the C/C++ festival
program is used for synthesizing speech. This may not work very well in all
Linux distributions (and in Windows, we have not been able to find a working
festival implementation for Windows.) Instead you can use synthesizerTest
in the freetts directory.  To do so, you need to download and install
freetts as described in apps/README_freetts.txt. Compile the java program
with

sh compbatch.sh

and run the program instead of ./festivaltest with

sh runbatch.sh

The freetts synthesizerTest program also uses agents, thus run it in a separate
window just as with ./festivaltest. This agent is also named "festival" so that
the interpreter agent in the inter directory does not have to be changed to
work with freetts instead of festival.
