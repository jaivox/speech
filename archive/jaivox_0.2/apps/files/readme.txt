
(From http://www.jaivox.com/appgen.html)

Integrating with other applications

Search form
Search   

You can integrate the Jaivox interpereter agent with your application using the following steps. The steps are given below, more details about each of the steps is given later in this page.

This article contains all the details on creating a speech recognizer that connects to an application. 
Another article explains the interface to a database.

1. Create a new application as described in the tutorial. If you do not want to use any generated questions, you can just copy everything from an existing application. Assuming that the questions in the application are in test.quest, you may remove all the questions you do not want from the test.quest file in the application after adding your own questions.

2. Add questions you want to test.quest. When you add a question, you also have to add a tab followed by a specification. Suppose your application is called app. Then the specification for each question should start with (command, app which may be followed by additional information before ending with a closing parentheses.

3. In the copied or generated application test, there will be a program for the interpreter (in the examples we have, it is usually called interTest. This program contains a line creating a new Command interpreter. Change this line to create a customized Command class which routes its instructions to your application.

4. Update the questions passed to the speech recognizer. This can be done using the same program that created the application, or it can be done manually.

The rest of this article gives details on each of the above steps. It illustrates integration with two applications: one example is using the "find" command in Linux. The other example uses the Sqlite database used by Mozilla Firefox to illustrate database integration.
Step 1: Create a new application

The jaivox library should be in your classpath. You can use one of the examples here to integrate your application with Jaivox by hand, but it may be easier to generate a new application. As an example, consider an application for integrating with the find command available on Linux computers. This command has various options to find files and directories that match different conditions. We can look at a few of these options, but also look at producing short answers from spoken questions.

But the new application we generate initially will not use find. Instead we create an application that talks about files on a computer. Later we integrate find with this application.
Specifying details of a new application

To create a new application, we follow the same steps as in the tutorial. We need four files.

a. files.conf
b. spec/files.spec
c. spec/files.txt
d. spec/grammar.txt

files.spec contains a description of the columns in the text file files.txt. It also includes parts of speech (given in upper case letters) in which these columns may be used and alternate words for using those parts of speech. Finally grammar.txt contains sentences conisting of some parts of speech. When a question is generated, the grammar.txt sentences are substituted with values from spec.txt.

For example, a sentence in grammar.txt may be

WP is the JJS-P NN

In this sentence, all the words in upper case are parts of speech (also caled "grammar tags".) These tags may appear in files.spec. For example the following describes the first two columns of files.txt

{
topic
type: field
attributes: big, recent
WP: what, which
ELS: other, besides, else
NN: topic, subject, theme
NNS: topics, subjects, themes
NNP: [files.txt 0]
}

{
big
type: attribute
fields: topic
JJ-P: big, large, extensive
JJ-N: small, short
RB: big
RBR: very big
RBS: biggest
}

From this we know that WP can be replaced with "what" or "which". The tag JJS-P means the superlative form of a JJ-P, for example JJS-P could be replaced with "biggest". Finally NN can be replaced for example with "topic". All of this results in a question like

what is the biggest topic

The last thing to be specified is files.conf. This is a configuraiton file that tells the generator various details about the other three files and also information about placing the generated code in some locations.
Generating the application

The generator uses the four files to create a Jaivox application. This will consist of three parts (a)A speech recognizer (b)A speech generator and (c)An interpreter. The speech recognizer used Sphinx as default, the speech synthesizer uses Festival as default. The logic of the application is contained in the interpreter.

We can modify the interpreter to create our application that utilizes the "find" command to answer spoken questions.

To generate the application, change to the directory containing files.conf. Then run

java com.jaivox.tools.Jvgen files.conf

There will be one warning to create a Log object, you can ignore that, it is to create a logger to trace various activities during generation.

The generator creates a file called files.quest containing various questions. Before adding code to customize responses using the find command, we have to modify the questions.
Step 2: Modifying generated questions

You can add any English question to the list of questions in files.quest. The only requirement is to also create the programs to answer each of these questions. We illustrate three questions that we can add. These questions invoke the find command to create an answer.

Each questions already in files.quest has a specification next to it. This specification is used to get the right data to answer the question. For example, consider one question: "what topic is big?" (i.e. occurs in many files.) This question occurs in files.quest with a specification that describes precisely how to answer the question.

what topic is big (find, topic, big, JJ-P)

The specification here is (find, topic, big, JJ-P). This says that the answer involves searching the data, ordering the data in order of the attribute big and selecting the items that could be considered as big. The JJ-P tells us to take a portion of the data that would be considered big or bigger or biggest, certainly not small or smaller or smallest.

When adding our custom questions, we should also add enough information to direct the question to the right formulation of the find command. We do this by adding a specification after each question. The format of this specification is quite flexible, but to distinguish it from other questions, you need to start the specification with (command, find, which may be followed by additional information. The find word is needed here because there are other specifications that may start with (command, .

The following three questions are extra questions we have added to files.quest.

a. do we have anything in Java on entropy
b. when was the last time we looked at docs
c. where do I have a lot of papers

The interpretations of these questions are subjective. The first question is asking whether there is a Java program that contains mention of entropy (a measure of disorder), possibly that is a Java implementation of this function. The second question is asking perhaps about the last time a document of a particular type (perhaps Open Office text) was created. The last question is aking about folders containing a lot of PDF files (as technical papers tend to be.) It could instead ask where I have a lot of old JPG photographs.

The data needed to answer each of these questions can be determined using the find command. We can consider this along with the implementation of the customized handler for these questions.
Step 3: Adding a customized command handler in interTest

Each of the questions a, b, c above can be answered using information obtained with find. We will first consider question b, which has the shortest specification.

(command, find, find /home/you -type f -name '*.odt' | xargs ls -lt, latestTime)

The find command is used to find files of tyoe "odt" (open office text document) within the /home/you folder. This information is piped to xargs ls -lt to organize the result with the latest document first.

The specification starts with (command, find, so that we know it should be routed to the cusotmized handler. The next argument in this specification is the entire command passed to the Linux operating system. The last argument is a customized function we implement to process the results of the command passed to Linux.

The interpreter in interTest creates a default class Command. This can be replaced with a custom class. In this case we create a class FindCommand that extends Command. We add a function latestTime in this that takes runs the find command and then obtains the last modified file from the results. This result is then made into an English sentence and returned to the interpreter which then directs the speech synthesizer to speak the result.

We replace the line

Command cmd = new Command ();

with

FindCommand cmd = new FindCommand ();

This is then used in creating the interTest class.

intertest = new InterServer ("inter", port, dir, kv,cmd);

The class FindCommand implements several functions like latestTest. Ohter questions may require different functions.

The other two questions we have mentioned requires more elaborate specifications and differnet functions in FindCommand.

a. do we have anything in Java on entropy

requires us to find java files, then look inside them with grep to see if it mentions entropy, then arranges the files so that we can get the latest version. All this is done with the following specification.

(command, find, find /home/you/ -type f -name '*.java' -exec grep -il entropy '{}' \; | xargs ls -lt, matchingFile)

Another question to handle is

c. where do I have a lot of papers

(command, find, find /home/you/papers/ -maxdepth 1 -type d | while read -r dir; do printf "%s:\t" "$dir"; find "$dir" | wc -l; done | sort -n -r -k2, mostDir)

This requires a little shell script that goes through each directory, counts the number of files in each directory. In this case we go only one more level of depth beyond the given directory, you can change that level, look for a particular type of file, a particular size of file, one modified within a certain period etc.

These are just a few examples of questions we can ask. Other questions may involve other options on the find command, such as the time of modification or permissions on files.
Step 4: Adding modifications to speech recognizer

The last step involves updating the speech recognizer with the questions that have been added.

At this point, it is also possible to remove questions from files.quest.

The questions in this file are used to build the language model for Sphinx. To update that list of questions, we can just call com.jaivox.tools.Generator.updateLmQuestions. This will modify the copy of questions in the sphinx directory before building the language model for speech recognition.

