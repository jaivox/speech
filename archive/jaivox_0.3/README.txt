
Jaivox library and applications Version 0.3
Built December 26, 2012

See http://www.jaivox.com for detailed documentation and requirements.

   Copyright 2010-2012 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.


Installation - short version
============================
Youn need to install sphinx4 before compiling Jaivox files. If the individual
class files from sphinx4 are in your classpath, run

ant

to build from this directory using the provided build.xml. This build file assumes
that the classpath that you obtain from antruntime contains the required sphinx4
classes. It is better to create the classpath explicitly by running config.bash
and answering questions about locations of various jar files from sphinx4.

Run

ant jarfile

to create jaivox.jar

The apps directory contains individual applications. Some of the directories
within apps are for data. For each application, there is a readme.txt
file within the application folder.

Javadocs are in the doc directory.

Contact: contact@jaivox.com

Files here
==========
build.xml is the ant build file
src/ contains com.jaivox Java source files
apps/ contains several applications and supporting data.
	apps/audio contains recorded audio for some tests without a microphone.
	apps/common  contains various templates used for generating applications.
	apps/db is a database integration application.
	apps/files illustrates integration of speech with a command shell.
	apps/freetts uses freetts instead of festival for speech synthesis.
	apps/recorded contains a demo using recorded audio.
	apps/student illustrates how the grammar can be expanded.
	apps/test is a tutorial program described in tutorial.html.

Installation
============
You can see more deatails at http://www.jaivox.com/installation.html

This directory includes a bash script config.bash. You can run it to
see if your system has required components. It also creates build.xml
based on information gathered from your answers. (Further it creates two
files runsphinx.sh and runinter.sh - their use is described later in this
file.)

Requirements
------------
1. Java Development Kit for example from the Oracle Java SE download site
2. Ant, from Apache
3. Language Modeling Toolkit from Cambridge University and Carnegie Mellon
   University.
4. sphinxbase from Carnegie Mellon University
5. Sphinx 4 from Carnegie Mellon University
6. Sphinx requires JSAPI from various implementations of the JSAPI.
7. Festival from the University of Edinburgh. You will need the development
   version, if you are installing it as a package, please install both 
   festival and festival-dev.

The Java Development Kit and Ant are installed using instructions at the
respective websites.

The CMU/Cambridge CMU-Cam_Toolkit_v2 is installed with a make install,
but as it says in the README file, you have to set a flag SLM_SWAP_BYTES
before doing so. After this is done, the make procedure creates several
executable files in the bin subdirectory of the toolkit. For convenience
you may want to copy these executables to some location in your path
such as /usr/local/bin as super user.

sphinxbase is required for a part of the language modeling. It can be
installed by following the instructions on the web site. Please note
that you need to be superuser to install the binaries, thus after
running ./configure you need to install as super user by running sudo
make install

Sphinx 4 is installed essentially by building the code using a build
file in its installation directory. JSAPI is often easy to implement by
running a simple script (depends on the implementation.) This script
(usually jsapi.sh) requires uudecode that is installed as part of
sharutils package available with Linux distributions. Finally, Festival
is usually installed by default on Linux systems, but you may need to
install the development version, usually available as a package
festival-dev. On some Linux distributions, festival-dev is automatically
installed with festival (Arch Linux for example.) Jaivox installation

Jaivox installation is like Sphinx installation.

Jaivox requires that the Sphinx4 classes are in the CLASSPATH. For
example, suppose your CLASSPATH environmental variable contains a folder
/home/johndoe/classes. Then there should be Java classes in
/home/johndoe/classes/edu/cmu/sphinx that are from the Sphinx 4 package.

To install the files, unpack the downloaded jaivox.tar.gz or jaivox.zip
to any folder.

Change to this folder, then enter
ant
That should build all the class files from the Java sources.
Move Jaivox class files to the classpath

After building the files in the last step, you need to copy the class
files that will be in the bin directory of the installation directory to
the classpath. The bin directory contains a directory com. Copy this
entire directory to the classpath. If your classpath contains
/home/johndoe/classes, then after copying, you should have a directory
/home/johndoe/classes/com/jaivox which contains subdirectories
recognizer, interpreter and tools.

To help with the appropriate classpath, the config.bash script creates two
scripts, runsphinx.sh and runinter.sh. The runsphinx.sh script includes
classes needed for speech recognition when running applications involving
sphinx (typically in a generated application, these are in the "sphinx"
directory.) The runinter.sh includes classes that are required for Jaivox
interpreter, typically found in the "inter" directory. You may need to change
the name of the main class in each of these scripts depending on your
programs.

Testing
=======
See http://www.jaivox.com/tutorial.html for detailed instructions on
generating and testing an application.

Please note that you can modify runsphinx.sh and/or runinter.sh for running
your application.

See http://www.jaivox.com/recordeddemo.html for a demo where you can hear
one of us asking questions and the Jaivox interpreter creating anwswers
which are spoken through festival.

The simplest test involves using the Java-based Freetts instead of
festival. This can be found in apps/freetts, for instructions please see
README_freetts.txt in that directory or http://www.jaivox.com/freetts.html.

