
(from http://www.jaivox.com/tutorial.html)

How to create an application

This tutorial describes how to create a speech based application using Jaivox. (We use Jvia to indicate the Jaivox (Interactive) applications.) The application summarizes information so that the user gets short answers that are easy to understand. This makes it easier to use the information without requiring a keyboard (for example, on the phone.)

Jvia complements other systems that reduce information using analytics procedures. Analytics (or Business Analytics) analyzes vast amounts of data to discover useful patterns. This usually involves various mathematical methods. While these methods are of great interest, Jaivox handles the problem of making effective use of these patterns.

This tutorial illustrates a simple example where we assume that the analytics has been performed already. The original data may be vast and complex, but usually analytics method reduce the data to a form where one can look for various qualified situations.

We consider an example of various roads and their properties. To keep things simple, we consider two aspects of roads: the speed that one can expect from these roads, and the difficulty involving using the road (for example, traffic lights and rough road surface may be considered as causing some difficulty.) The raw data that is used to get these properties may be quite extensive, such as speed measurements, and congestion reports and reports of road surface improvements. For this tutorial we assume that some analytic methods have reduced all this information to a table that has just three fields.
road name
speed
smoothness

While road name is usually some specific identifier such as "Elm Street" we assume that the other two fields are attributes that are numerical values between 0 and 100. Thus the fastest roads will have the speed attribute close to 100 and the roughest road may have a value close to 0 for the smoothness attribute
Prepare Data

We have a table, called road.txt that contains some data. This data fits the description above, consisting of three columns. The first column is a string, the other two columns are numbers between 0 and 100.

Here is a sample of the data


Old Mill Road, 67.45, 64.61
Paerdegat 7th Street, 34.97, 7.36
Mc Guinness Boulevard, 51.65, 92.2
Elmwood Avenue, 31.44, 96.9
Skidmore Place, 63.11, 43.64

The data is comma separated. We assume that the data is contained in a text file. We could alternately fetch this data from a database but for simple applications, we can read this data into a table.
Create Specifications

Once we have the data ready, we need to create some specifications that describe the data. This description is necessary to convert facts from the data into English sentences.
Data specifications

The data specifications consist of a description of the data, along with some information that helps us to create sentences about the data. The first part is just the usual database-type information.


{
road.txt
type: table
columns: road, fast, smooth
}

In this case we may think of road as a field while fast and smooth may be considered to be attributes. There are differences in the way fields and attributes are described. The following description is for the field in this table.


{
road
type: field
category: street
attributes: fast, smooth
WP: what, which
ELS: other, besides, else
NN: road, route, highway, freeway
NNS: roads, routes, highways, freeways
NNP: [road.txt 0]
}

The format for attributes is similar, here is the description from the first field.


{
fast
type: attribute
fields: road
category: measure
JJ-P: fast, quick
JJ-N: slow, congested, busy
RB: fast, slow
RBR: quite fast, quite slow
RBS: very fast, very slow
}
Details about fields and attributes

Some of the fields in the specifications above for fields and attributes may be obvious. Others involve grammar tags that may not be well-known. What follows is a line by line explanation of the different fields in the specifications above.

The first line of the specification of roads simply gives the name of the field. The next one tells that this particular word, road is a field, rather than an attribute.


type: field

The type of a column of the data is important. If it is numeric, then we can sort it to find what is better in some sense. But if it is a text field, then sorting will only put it in alphabetical order. In the current implementation, the category measure indicates that the value is numeric, everything else is considered to be alphabetic.


category: street

The next line indicates the attributes that are associated with this field.


attributes: fast, smooth

The next line involves what are called WH-questions, that is, "what", "who", "when" etc. This is generally identified by a grammar tag. The tag here is from the Penn tag set (Penn here is the University of Pennsylvania.) We generally use the tags from the Alphabetical list of part-of-speech tags used in the Penn Treebank Project. sometimes though we create some new tags to express something that is not considered in the Penn tag set.


WP: what, which

The following tag ELS is something that is not in the Penn tag set. But we need to have something that describes how people may ask for something not fitting a certain description. For instance they may ask "what road besides 15th street, is fast?"


ELS: other, besides, else

The next line in the specification gives the singular nouns that all stand for the same thing (i.e. they are synonyms of the field.)


NN: road, route, highway, freeway

Some questions may be asked in the form of plurals, such as "what are the fast roads?". The NNS tag describes the plural forms of the nouns above.


NNS: roads, routes, highways, freeways

There are many specific road names that may be involved in the questions. The data file, in this case road.txt contains the names of various roads in the first column. The tag NNP is used to indicate proper nouns. We can say that the proper nouns are in the first column of the file road.txt with the expression [road.txt 0] (since the first item in any list has index 0 in Java and various other programming languages.


NNP: [road.txt 0]

Attributes are generally adjectives. We can describe the ways adjectives may appear in questions. The following explains the fields of the attribute "fast".


type: attribute

The line below associates this adjective with a field, in this case "fast" is a description about a "road."


fields: road

The next line gives the type of the attribute in the table. We generally assume that some analytics system has reduced the "fastness" of a road to a percentage value, we use the term "measure" to describe this type of value.


category: measure

The JJ-P tag gives various positive ways of saying that something is fast.


JJ-P: fast, quick

The JJ-N tag describes negative ways of saying something is fast, i.e. ways of saying that something is not fast.


JJ-N: slow, congested, busy

RB gives the adverbial forms of saying the same thing. For example we may ask "what are the fast roads?" using the "fast" as an adjective, but we may also ask "what roads are fast?" using "fast" as an adverb.


RB: fast, slow

RBR gives the comparative adverbial forms.


RBR: quite fast, quite slow

RBS gives the superlative adverbial forms.


RBS: very fast, very slow

The other attribute in this example, "smooth" is also described in similar ways. With all these descriptions, we can create various questions involving the field and these attributes.
Grammar and other configurations

With the specifications and the data, we can generate questions that a user may ask. These questions can be modified manually. But we do need a set of questions that can be passed to the speech recognizer to form a grammar. When the user speaks, the recognizer uses this set of questions to determine if the user asked one of the questions in the
list.

A grammar file is used to specify the kinds of questions that may be asked. This file describes the form of the questions. Any words in uppercase letters is considered as a grammar tag like WP, NN etc. described above. You can specify the kind of questions you have in mind as a second column (separated with a tab) in the grammar file. For example, we can look at some of the lines in grammar.txt. Incidentally the example question following the tab is not required, it is just something to remind yourself the reason for adding a specific grammar pattern. 

are the NNS JJ-N are the highways slow?
are the NNS RBS are the highways very slow?
is NNP DT NN is Adams Street a highway?
is NNP JJR-P is Adams Street faster?

In each of these questions, we can substitute any value for the grammar tags. For example, NNS in this example can be "roads","routes" etc. Similarly JJ-N can be slow, congested etc. We can use the different optional values for each of the grammar tags. This will create many questions from the same grammar template.

The generator needs some additional information. The generator creates three programs that communicate via sockets. These three programs (a speech recognizer, an interpreter and a speech synthesizer) has to be identified in terms of names, socket ports and directories containing their source code.

A configuration file, such as one called test.conf for this example, contains these and a few other details. This file is a Java Properties file. Comments in the file explain each of the properties. Most of the properties provide names of various data files. All of the files involved are in the test/spec subdirectory of the directory created for installing Jaivox.
Generate

Generating involves one command, providing the configuration information. In this case, the installation directory will contain
bin
build.xml
src
test
(The bin directory is not part of the original downloaded files, it is created when you use ant to build the classes, for details see the installation instructions.)

To generate the three agents that will work together, change to the bin directory and enter java com/jaivox/tools/Jvgen ../apps/test/test.conf Based on the entries in test.conf this will generate a directory test/gen. This directory will contain three subdirectories.
festival containing C++ programs for speech synthesis.
inter a Java program for interpreting questions.
sphinx another Java program that uses the Sphinx libraries and models to create a speech recognizer to determine if the user asked one of the questions we have generated. 
Test

After the agents are generated, you can test the application using three windows.
In the first window change to the sphinx directory of the generated code.
In the second window change to the inter directory of the generated code.
In the last window, change to the festival directory. 
Compiling the generated code

The first two windows, sphinx and inter, you can compile the code with
javac *.java

In addition, in the sphinx window, you need to build the language model. Recall from the installation that you need to install the CMU-Cambridge Language Modeling Toolkit and sphinxbase. Assuming these are installed, the language model can be built using

sh lmgen.sh

in the first window.
The third window, fest contains a C++ program. This can be compiled with
make

This should produce an executable programs festivaltest.
Running the program

Make sure that your microphone is plugged in and operational. You can try some of the sphinx demos to make sure that speech recognition is working properly with the microphone.
In the second window, i.e. the one for the inter directory, start the agent with
java interTest

In the third window, the one for fest, start the program with
./festivaltest

In the first window, i.e. the one for sphinx enter
java sphinxTest

You should see messages in all three windows as the agents in the first and third windows connect to the one in the second, i.e. inter window. This initialization establishes the path for messages once recognition starts.
After the initialization, the sphinx window will prompt the user to speak.
At that point, ask a question such as
what is the fastest road?

After a little while, you should be able to hear an answer through the speakers or headset. Assuming that the speech was recognized you should here something like (the exact form varies.)
Seems like there is only one answer, Avenue O.

You can ask follow up questions and ask other questions, until you are done with the questions.

At this point, the easiest way to terminate all three agents is to enter Control-C in each of the windows.
