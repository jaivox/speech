
This program contains the same specifications road.spec and grammar road.dlg
as the test program. The test program generates three agents: one for
speech recognition (sphinx), another for interpreting questions (inter)
and a third for synthesizing (festival.)

Here you can combine all three agents into a single file. There are two
versions that can be generated with the specifications here. The file
batch.conf generates a batch version of the program. This will run similar
to the program in the apps/1st directory. (In fact, to work like the one
in apps/1st, you have to modify batchTest and some other programs to fetch
the data and formulate answers.)

The file live.conf creates a version of the program that reacts to questions
asked through a microphone. Again there is no data connection here, so that
the answers generally will not be very useful. But you can see if your
questions are being recognized properly.

This program does not use festival to synthesize sounds for the answers.
Instead it uses free TTS, a Java-based synthesizer. See apps/README_freetts.txt
for details on installing this program.
