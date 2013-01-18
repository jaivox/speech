
(from http://www.jaivox.com/student.html)

Improving the language of questions

When a user asks a question to a Jaivox application, the question is matched with a previously generated set. We need to create a set of questions like that. This set is used to create the language model for the speech recognizer. The speech recognizer needs a language model so that it can guess about the groups of words that may have been in the user's spoken question.

The tutorial describes a set of questions about roads and their conditions. The questions are generated using a set of grammar templates. The tutorial gives some examples of these.

are the NNS JJ-N are the highways slow?
are the NNS RBS are the highways very slow?
is NNP DT NN is Adams Street a highway?
is NNP JJR-P is Adams Street faster?

This example shows how we can expand this grammar. We use an example of an application that talks about the contents of many text files. It is a little complicated to talk about the contents of text files. Therefore we introduce new grammar tags (like NNS meaning plural noun) and grammar rules to create the questions. This also shows how you can create your own grammar tags (we have a few in the tutorial also, for example JJR is a normal grammar tag meaning comparative adjective, but we create something called JJR-P meaning comparative adjective used in a positive sense.) It also shows how you can create your own grammar rules involving these tags and specific words to create questions.
About the example

In this example we imagine that teachers have written notes about various students. The students are identified by their last names.

There are two ways a student's name may appear in the teacher's notes. A particular student might have been esepcially noticeable (or notorious in some cases) during certain times. At the time when they were the subject of discussions or activities, the teacher may have written a lot of details about that student. Thus one way a student's name may appear in the notes is in terms of detailed descriptions.

Whether there are detailed notes or not, a student's name may appear in the teacher's notes frequently. This may be due to a student being monitored for some reason or another. It seems that whether there are frequent mentions is not entirely related to detailed notes. For example the teacher may say "Minella continues to surprise me with her programs" without any further details.

Based on this description, we can create a database from the notes. The raw data will be in the form of names of students and the location of mention, perhaps with the length of the block of words that are related to that instance.

From this database, we can create a table that summarizes the detailed nature of comments about a student and the frequency with which the student is mentioned. In that table we need to have only three columns: the name of the student, a score related to the level of detail in the notes and another score related to the frequency of mention. For example, the table may look like


Greto,99.67,90.43
Stroupe,3.76,89.6
Casivant,25.26,90.71
Minella,17.96,86.1
...

(Incidentally, this is just made up data with no connections to anybody with these names. We just picked some names from census lists of common names.)

We could think of this table as having one field (student) and two attributes (detailed, frequent). Using this field and attributes we can then construct questions and answers that refer to this table.
Original specifications

Following the tutorial example, we started with similar specficiations in a file student.spec and a grammar file grammar.txt.

Here is a part of the original student.spec


{
student.txt
type: table
columns: student, detailed, frequent
}

{
student
type: field
attributes: detailed, frequent
WP: who
ELS: other, besides, else
NN: student, kid, scholar
NNS: students, kids, scholars
NNP: [student.txt 0]
}

And here is a part of the original grammar.txt


do the NNS have JJ-P notes
do the NNS have JJR-P notes
are there just JJ-N notes about the NNS
are there just JJR-N notes about the NNS
WP is the NN with JJR-P notes
which NNS have JJR-P notes
etc.

The resulting English formulations of the questions are not very natural. For example,


do the scholars have more detailed notes (ask, student, detailed, JJR-P)
do the scholars have more extensive notes (ask, student, detailed, JJR-P)
do the scholars have longer notes (ask, student, detailed, JJR-P)

We can make the questions more natural by expanding the specifications and the grammar with more grammar tags (like NNS) and grammar templates (i.e. sentence forms.)
Improved grammar

The following questions may be more natural than the earlier questions.

do the children have more positive notes (ask, student, detailed, JJR-P)
do the children have more positive comments (ask, student, detailed, JJR-P)
do the children have more positive questions (ask, student, detailed, JJR-P)
do the children have more positive concerns (ask, student, detailed, JJR-P)

To generate these, the specifications are expanded with several grammar tags. Here is a part of the new student.spec


{
student.txt
type: table
columns: student, detailed, frequent
}

{
student
type: field
attributes: detailed, frequent
WP: who
JJ-E: other
IN-E: besides, else but, other than, else besides, but
RB-E: else
ELS: other, besides, else
NN: student, kid, scholar, child
NNS: students, kids, scholars, children
NNP: [student.txt 0]
PRO: anybody
}

The grammar templates are also expanded, here is a part of the new grammar.txt


...
are there PH-P NNS-C about the NNS
are there PH-N NNS-C about the NNS
are there any JJ-P comments about the NNS
do the NNS have JJ-P NNS-C
do the NNS have JJR-P NNS-C
are there just JJ-N NNS-C about the NNS
are there just JJR-N NNS-C about the NNS
are there just PH-N NNS-C about the NNS
...

You can experiment by expanding the grammar tags (in student.spec) or grammar templates (in grammar.txt.) You can see the resulting questions that are generated in student.spec in the interpreter directory of the generated files. (These are found in gen/inter/student.quest if you generate the example using

java com.jaivox.tools.Jvgen student.spec

In the apps/student directory of the Jaivox download.
