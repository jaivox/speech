/*
   Jaivox version 0.5 August 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.jaivox.tools;

import com.jaivox.interpreter.Utils;
import com.jaivox.util.Log;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * The Generator creates all the files for an application, based on
 * specifications. It also uses several templates and common files
 * located in the common directory (you can specify the location of the
 * common files in the configuration file.)
 */

public class Generator {

	Config conf;

/**
 * Patterns to be replaced in templates start with patIndicator,
 * the default value is "PAT"
 */
	public static String patIndicator = "PAT";

	String source = "./";
	String common = "./";
	String destination = "./";
	String dir_recognizer;
	String dir_interpreter;
	String dir_synthesizer;
	boolean onedir = false;
	boolean overwrite = false;

/**
 * Options are essentially
 * iomode: live, batch, console
 * directories: one or multiple
 * recognizer: sphinx or web
 * synthesizer: festival or one of (freetts, espeak, web)
 */

	String input;
	String recognizer;
	String synthesizer;

	static String patFiles [] = {
		"batchMultiWeb.java",
		"batchOneSphinx.java",
		"batchOneWeb.java",
		"batch.xml",
		"consoleTest.java",
		"CxxResponder.cc",
		"CxxServer.cc",
		"CxxSession.cc",
		"interpreterTest.java",
		"liveMultiWeb.java",
		"liveOneSphinx.java",
		"liveOneWeb.java",
		"live.xml",
		"lmgen.sh",
		"project.config.xml",
		"recognizerBatchTest.java",
		"recognizerTest.java",
		"synthesizerTest.java"
	};

	static String LiveOneSphinx [] = {
		"live.xml",
		"liveOneSphinx.java",
		"lmgen.sh",
		"ccs.ccs"
	};

	static String LiveOneWeb [] = {
		"liveOneWeb.java"
 	};

	static String LiveMultiSphinx [] = {
		"recognizerTest.java",
		"project.config.xml",
		"lmgen.sh",
		"ccs.ccs"
	};

	static String LiveMultiWeb [] = {
		"liveMultiWeb.java"
	};


	static String MultiFestival [] = {
		"CxxServer.cc",
		"CxxResponder.cc",
		"CxxSession.cc",
		"CxxData.cc",
		"CxxSocket.cc",
		"CxxThread.cc",
		"CxxData.h",
		"CxxResponder.h",
		"CxxServer.h",
		"CxxSession.h",
		"CxxSocket.h",
		"CxxThread.h",
		"makefile"
	};

	static String MultiSynthesizer [] = {
		"synthesizerTest.java",
	};


	static String BatchOneSphinx [] = {
		"batch.xml",
		"batchOneSphinx.java",
		"lmgen.sh",
		"ccs.ccs"
	};

	static String BatchOneWeb [] = {
		"batchOneWeb.java",
	};

	static String BatchMultiSphinx [] = {
		"recognizerBatchTest.java",
		"batch.xml",
		"lmgen.sh",
		"ccs.ccs"
	};

	static String BatchMultiWeb [] = {
		"batchMultiWeb.java"
	};


	static String MultiInterpreter [] = {
		"interpreterTest.java"
	};


	static String Console [] = {
		"consoleTest.java"
	};

	String lmtool = "lmgen.sh";
	String runsphinx = "runsphinx.sh";
	String runinter = "runinter.sh";

	String commandName;
	String commandFile;
	Questgen qg;
	TreeMap <String, Integer> patternFiles;
	static Integer One = new Integer (1);

	boolean Valid = false;

/**
 * Geneate using information in a configuration file, usually with
 * file extension ".conf"
 * @param confname 
 */
	public Generator (String confname) {
		conf = new Config (confname);
		if (!conf.isValid ()) {
			Log.severe ("Invalid/incomplete configuration info in "+confname);
			Valid = false;
			return;
		}
		generate ();
	}
	
/**
 * Generate using information in a properties map. This form is usually used
 * to call the generator internally from another program.
 * @param pp 
 */
	
	public Generator (Properties pp) {
		conf = new Config (pp);
		if (!conf.isValid ()) {
			Log.severe ("Invalid/incomplete configuration properties");
			Valid = false;
			return;
		}
		generate ();
	}
	
/**
 * Check whether the generator has a valid status.
 * @return 
 */
	
	public boolean isValid () {
		return Valid;
	}
	
	void generate () {
		patternFiles = new TreeMap <String, Integer> ();
		for (int i=0; i<patFiles.length; i++) {
			patternFiles.put (patFiles [i], One);
		}

		String project = conf.getProperty ("project");
		if (project == null) {
			Log.severe ("project name not specified");
			return;
		}

		// get the basic variables
		input = conf.getProperty ("input");
		if (input != null && !find_required ("input", " live batch console ")) {
			Log.severe ("input specification incorrect");
			// return;
		}
		// for backward compatibility
		if (input == null) {
			input = conf.getProperty ("onefile");
			if (input != null && !find_required ("onefile", " live batch console ")) {
				Log.severe ("onefile specification incorrect");
				return;
			}
		}
		// get some basic values

		String base = conf.getProperty ("Base");

		if (!find_required ("overwrite_files", " yes no ")) {
			Log.severe ("overwrite_files not specified correctly");
			return;
		}
		String overwritefiles = conf.getProperty ("overwrite_files");
		overwrite = (overwritefiles.equals ("yes") ? true : false);


		source = conf.getProperty ("source");
		common = conf.getProperty ("common");
		destination = conf.getProperty ("destination");
		commandName = project + "Command";
		commandFile = destination + commandName + ".java";
		Log.info ("Source:"+source+" Common:"+common+" Dest:"+destination);

		// console is the simplified testing framework
		if (input.equals ("console")) {
			genConsole ();
			Valid = true;
			return;
		}

		if (!find_required ("onedirectory", " yes no ")) {
			Log.severe ("onedirectory not specified correctly");
			return;
		}
		String temp = conf.getProperty ("onedirectory");
		if (temp.equalsIgnoreCase ("yes")) onedir = true;
		else onedir = false;

		if (!find_required ("recognizer", " sphinx web google ")) {
			Log.severe ("recognizer not specified correctory");
			return;
		}
		recognizer = conf.getProperty ("recognizer");

		if (!find_required ("synthesizer", " festival espeak freetts web ")) {
			Log.severe ("synthesizer not specified correctory");
			return;
		}
		synthesizer = conf.getProperty ("synthesizer");


		// from here we branch according to combination values. there
		// are 24 possible combinations as of now (May 2013).

		if (input.equals ("live")) {
			if (onedir) {
				dir_interpreter = destination;
				dir_recognizer = destination;
				dir_synthesizer = destination;
				// create custom command file name
				// Use the directory name to create Command name
				commandName = project + "Command";
				commandFile = dir_interpreter + commandName + ".java";
				if (recognizer.equals ("sphinx")) {
					if (synthesizer.equals ("festival")) {
						genLiveOneSphinxFestival ();
						return; // error
					}
					else if (!synthesizer.equals ("festival")) {
						genLiveOneSphinxSynthesizer ();
					}
					else {
						Log.severe ("Invalid synthesizer "+synthesizer);
						return;
					}
				}
				else if (recognizer.equals ("web") || recognizer.equals ("google")) {
					if (synthesizer.equals ("festival")) {
						genLiveOneWebFestival ();
						return; // error
					}
					else {
						genLiveOneWebSynthesizer ();
					}
				}
				else {
					Log.severe ("Invalid recognizer "+ recognizer);
					return;
				}
			}
			else {
				// get the directories for recognizer, synthesizer and interpreter
				String dirinter = conf.getProperty ("dir_interpreter");
				if (dirinter == null) {
					Log.severe ("dir_interpreter not specified");
					return;
				}
				String dirrecog = conf.getProperty ("dir_recognizer");
				if (dirrecog == null) {
					Log.severe ("dir_recognizer not specified");
					return;
				}
				String dirsynth = conf.getProperty ("dir_synthesizer");
				if (dirsynth == null) {
					Log.severe ("dir_synthesizer not specified");
					return;
				}
				dir_interpreter = destination + dirinter + conf.Sep;
				dir_recognizer = destination + dirrecog + conf.Sep;
				dir_synthesizer = destination + dirsynth + conf.Sep;
				// create custom command file name
				// Use the directory name to create Command name
				commandName = project + "Command";
				commandFile = dir_interpreter + commandName + ".java";
				if (recognizer.equals ("sphinx")) {
					if (synthesizer.equals ("festival")) {
						genLiveMultiSphinxFestival ();
					}
					else {
						genLiveMultiSphinxSynthesizer ();
					}
				}
				else if (recognizer.equals ("web") || recognizer.equals ("google")) {
					if (synthesizer.equals ("festival")) {
						genLiveMultiWebFestival ();
					}
					else {
						genLiveMultiWebSynthesizer ();
					}
				}
				else {
					Log.severe ("Invalid recognizer "+ recognizer);
					return;
				}

			}
		}
		else if (input.equals ("batch")) {
			if (onedir) {
				dir_interpreter = destination;
				dir_recognizer = destination;
				dir_synthesizer = destination;
				// create custom command file name
				// Use the directory name to create Command name
				commandName = project + "Command";
				commandFile = dir_interpreter + commandName + ".java";
				if (recognizer.equals ("sphinx")) {
					if (synthesizer.equals ("festival")) {
						genBatchOneSphinxFestival ();
						return; // error
					}
					else {
						genBatchOneSphinxSynthesizer ();
					}
				}
				else if (recognizer.equals ("web") || recognizer.equals ("google")) {
					if (synthesizer.equals ("festival")) {
						genBatchOneWebFestival ();
						return; // error
					}
					else {
						genBatchOneWebSynthesizer ();
					}
				}
				else {
					Log.severe ("Invalid recognizer "+ recognizer);
					return;
				}
			}
			else {
				// get the directories for recognizer, synthesizer and interpreter
				String dirinter = conf.getProperty ("dir_interpreter");
				if (dirinter == null) {
					Log.severe ("dir_interpreter not specified");
					return;
				}
				String dirrecog = conf.getProperty ("dir_recognizer");
				if (dirrecog == null) {
					Log.severe ("dir_recognizer not specified");
					return;
				}
				String dirsynth = conf.getProperty ("dir_synthesizer");
				if (dirsynth == null) {
					Log.severe ("dir_synthesizer not specified");
					return;
				}
				dir_interpreter = destination + dirinter + conf.Sep;
				dir_recognizer = destination + dirrecog + conf.Sep;
				dir_synthesizer = destination + dirsynth + conf.Sep;
				// create custom command file name
				// Use the directory name to create Command name
				commandName = project + "Command";
				commandFile = dir_interpreter + commandName + ".java";
				if (recognizer.equals ("sphinx")) {
					if (synthesizer.equals ("festival")) {
						genBatchMultiSphinxFestival ();
					}
					else {
						genBatchMultiSphinxSynthesizer ();
					}
				}
				else if (recognizer.equals ("web") || recognizer.equals ("google")) {
					if (synthesizer.equals ("festival")) {
						genBatchMultiWebFestival ();
					}
					else {
						genBatchMultiWebSynthesizer ();
					}
				}
				else {
					Log.severe ("Invalid recognizer "+ recognizer);
					return;
				}

			}

		}
		else {
			Log.severe ("Cannot generate from specified options.");
			return;
		}
		Valid = true;
	}

	boolean find_required (String name, String matches) {
		String val = conf.getProperty (name);
		if (val == null) {
			Log.severe (""+name+" not specified, should be one of:" + matches);
			return false;
		}
		// pad value with spaces
		String test = " "+val+" ";
		int pos = matches.indexOf (test);
		if (pos == -1) {
			Log.severe (""+name+" should be one of" + matches);
			return false;
		}
		else return true;
	}


	void genLiveOneSphinxFestival () {
		Log.severe ("Currently we cannot combine Festival with Sphinx in one file.");
		Valid = false;
		return;
	}

	void genLiveOneSphinxSynthesizer () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";

			String livename = conf.getProperty ("live");
			for (int i=0; i<LiveOneSphinx.length; i++) {
				String name = LiveOneSphinx [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, destination, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, destination, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genLiveOneWebFestival () {
		Log.severe ("Currently we cannot combine festival with web recognizer in one file.");
		Valid = false;
		return;
	}

	void genLiveOneWebSynthesizer () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";

			String livename = conf.getProperty ("live");
			for (int i=0; i<LiveOneWeb.length; i++) {
				String name = LiveOneWeb [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, destination, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, destination, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genLiveMultiSphinxFestival () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			// String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<LiveMultiSphinx.length; i++) {
				String name = LiveMultiSphinx [i];
				String dest = dir_recognizer;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateFestival ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genLiveMultiSphinxSynthesizer () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			// String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<LiveMultiSphinx.length; i++) {
				String name = LiveMultiSphinx [i];
				String dest = dir_recognizer;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateSynthesizer ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genLiveMultiWebFestival () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			// String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				else if (name.equals (runsphinx)) dest = dir_recognizer;
				else if (name.equals (runinter)) dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<LiveMultiWeb.length; i++) {
				String name = LiveMultiWeb [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_recognizer;
				if (dest != null) {
					ok = generateFile (dest, name);
					if (!ok) {
						Log.severe ("Could not generate from "+name);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateFestival ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genLiveMultiWebSynthesizer () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			// String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				else if (name.equals (runsphinx)) dest = dir_recognizer;
				else if (name.equals (runinter)) dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<LiveMultiWeb.length; i++) {
				String name = LiveMultiWeb [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_recognizer;
				if (dest != null) {
					ok = generateFile (dest, name);
					if (!ok) {
						Log.severe ("Could not generate from "+name);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateSynthesizer ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genBatchOneSphinxFestival () {
		Log.severe ("Currently we cannot combine festival with Sphinx in one file.");
		Valid = false;
		return;
	}

	void genBatchOneSphinxSynthesizer () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";

			String batchname = conf.getProperty ("batch");
			for (int i=0; i<BatchOneSphinx.length; i++) {
				String name = BatchOneSphinx [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, destination, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, destination, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genBatchOneWebFestival () {
		Log.severe ("Currently we cannot combine festival with web recognizer in one file.");
		Valid = false;
		return;
	}

	void genBatchOneWebSynthesizer () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			String batchname = conf.getProperty ("batch");

			for (int i=0; i<BatchOneWeb.length; i++) {
				String name = BatchOneWeb [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genBatchMultiSphinxFestival () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				else if (name.equals (runsphinx)) dest = dir_recognizer;
				else if (name.equals (runinter)) dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<BatchMultiSphinx.length; i++) {
				String name = BatchMultiSphinx [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_recognizer;
				if (dest != null) {
					ok = generateFile (dest, name);
					if (!ok) {
						Log.severe ("Could not generate from "+name);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateFestival ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genBatchMultiSphinxSynthesizer () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				else if (name.equals (runsphinx)) dest = dir_recognizer;
				else if (name.equals (runinter)) dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<BatchMultiSphinx.length; i++) {
				String name = BatchMultiSphinx [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_recognizer;
				if (dest != null) {
					if (!copyFile (common, dest, name)) {
						Log.severe ("Could not copy "+name+" to "+destination);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateSynthesizer ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genBatchMultiWebFestival () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				else if (name.equals (runsphinx)) dest = dir_recognizer;
				else if (name.equals (runinter)) dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<BatchMultiWeb.length; i++) {
				String name = BatchMultiSphinx [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_recognizer;
				if (dest != null) {
					ok = generateFile (dest, name);
					if (!ok) {
						Log.severe ("Could not generate from "+name);
						return;
					}
				}
			}

			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateFestival ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genBatchMultiWebSynthesizer () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			if (!onedir) {
				fd = new File (dir_interpreter);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_recognizer);
				if (!fd.exists ()) fd.mkdirs ();
				fd = new File (dir_synthesizer);
				if (!fd.exists ()) fd.mkdirs ();
			}
			String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiInterpreter.length; i++) {
				String name = MultiInterpreter [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				else if (name.equals (runsphinx)) dest = dir_recognizer;
				else if (name.equals (runinter)) dest = dir_interpreter;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<BatchMultiWeb.length; i++) {
				String name = BatchMultiSphinx [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_recognizer;
				if (dest != null) {
					ok = generateFile (dest, name);
					if (!ok) {
						Log.severe ("Could not generate from "+name);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}

			generateSynthesizer ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void genConsole () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";

			String livename = conf.getProperty ("console");
			for (int i=0; i<Console.length; i++) {
				String name = Console [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = conf.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = conf.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = conf.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.info ("Could not copy specifications file");
			}
			String grammar = conf.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, destination, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, destination, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public void generateFestival () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			File fd = new File (destination);
			String files [] = src.list ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			for (int i=0; i<MultiFestival.length; i++) {
				String name = MultiFestival [i];
				tpls.put (name, yes);
				String dest = dir_synthesizer;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<files.length; i++) {
				String name = files [i];
				if (tpls.get (name) != null) continue;
				String dest = dir_synthesizer;
				if (!name.endsWith (".cc") &&
					!name.endsWith (".h") &&
					!name.endsWith ("makefile"))
					continue;
				if (!copyFile (common, dest, name)) {
						Log.severe ("Could not copy "+name+" to "+destination);
						return;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public void generateSynthesizer () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			for (int i=0; i<MultiSynthesizer.length; i++) {
				String name = MultiSynthesizer [i];
				String dest = dir_synthesizer;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}

	}

	boolean generateFile (String dest, String name) {
		try {
			String sortedKeys [] = conf.getSortedKeys ();
			int n = sortedKeys.length;
			String filename = common + name;
			String destname = dest + name;
			if (!okOverwrite (destname)) {
				Log.severe (destname+" exists. To overwrite, set overwrite_files to yes");
				return true;
			}
			String text = loadFile (filename);
			String changed = text;
            
            // replace longest keys first
            for (int i=0; i<n; i++) {
				String key = sortedKeys [i];
				String val = conf.getProperty (key);
				if (name.startsWith (key)) {
					String newname = name.replaceFirst (key, val);
					// Log.fine ("Destination name: "+newname);
					destname = dest + newname;
					if (!okOverwrite (destname)) {
						Log.severe (destname+" exists. To overwrite, set overwrite_files to yes");
						return true;
					}
				}
				String pat = patIndicator + key;
				if (text.indexOf (pat) != -1) {
					// Log.fine ("replacing "+pat+" with "+val+" in "+name);
					if (val != null) changed = changed.replace (pat, val);
					else {
						Log.info ("Missing value for "+pat);
					}
				}
			}
			if (changed.indexOf (patIndicator) != -1)
				changed = fixmisses (filename, changed);
			if (!writeFile (destname, changed)) return false;
			Log.info ("wrote: "+destname);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	String loadFile (String filename) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			StringBuffer sb = new StringBuffer ();
			while ((line = in.readLine ()) != null) {
				sb.append (line + "\n");
			}
			in.close ();
			String text = new String (sb);
			return text;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}

	String fixmisses (String filename, String text) {
		try {
			Log.info ("Commenting out lines containing missing patterns with //");
			Log.info ("Plese check output for syntax errors.");
			StringTokenizer st = new StringTokenizer (text, "\n", true);
			StringBuffer sb = new StringBuffer ();
			while (st.hasMoreTokens ()) {
				String token = st.nextToken ();
				if (token.indexOf (patIndicator) != -1 && token.indexOf ("CLASSPATH") == -1) {
                    Log.warning ("Missing value in "+token+" in file "+filename);
					token = "// "+token;
				}
				sb.append (token);
			}
			return new String (sb);
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe ("Errors while trying to remove missing tags in "+filename);
			return text;
		}
	}

	boolean writeFile (String filename, String text) {
		try {
			PrintWriter out = new PrintWriter (new FileWriter (filename));
			out.print (text);
			out.close ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	// easy way is to use jdk 1.7's java.nio.file
	// import static java.nio.file.StandardCopyOption.*;
	// Files.copy (source, target, REPLACE_EXISTING);
	// http://docs.oracle.com/javase/tutorial/essential/io/copy.html
	// will do it compatible with 1.6 here

	boolean copyFile (String src, String dest, String filename) {
		int bufsize = 1024;
		try {
			// is this one that should use generateFile?
			if (patternFiles.get (filename) != null) {
				return generateFile (dest, filename);
			}
			if (src == null) return false;
			if (src.equals ("null")) return false;
			String sourcefile = src + filename;
			String destfile = dest + filename;
			if (sourcefile.equals (destfile) || !okOverwrite (destfile)) { 
				Log.severe (destfile+" exists. To overwrite, set overwrite_files to yes");
				return true;
			}
			FileInputStream in = new FileInputStream (sourcefile);
			FileOutputStream out = new FileOutputStream (destfile);
			byte buffer [] = new byte [bufsize];
			int bytesread = 0;
			while ((bytesread = in.read (buffer)) > 0) {
				out.write (buffer, 0, bytesread);
			}
			in.close ();
			out.close ();
			Log.info ("Copied "+filename);
			return true;
		} catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	boolean copyIncludedFiles (String src, String dest, String filename) {
		try {
			String gramfile = src + filename;
			BufferedReader in = new BufferedReader (new FileReader (gramfile));
			String line;
			while ((line = in.readLine ()) != null) {
				if (line.startsWith (QaList.include)) {
					// should have just two words
					StringTokenizer st = new StringTokenizer (line, " \t\r\n\"");
					if (st.countTokens () < 2) {
						Log.severe ("Incorect include syntax "+line);
						Log.severe ("In grammar file "+filename);
						return false;
					}
					String first = st.nextToken ();
					String included = st.nextToken ();
					int pos = included.lastIndexOf (File.pathSeparator);
					// does not matter if pos = -1
					String name = included.substring (pos+1);
					String includedfile = src + name;
					File f = new File (includedfile);
					if (f.exists ()) {
						boolean copied = copyFile (src, dest, name);
						if (!copied) return false;
						Log.info ("Copied included grammar file "+name);
					}
				}
			}
			in.close ();
			Log.info ("Copied any included grammar files");
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	boolean okOverwrite (String filename) {
		try {
			File f = new File (filename);
			if (f.exists ()) {
				if (!overwrite) return false;
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

/**
 * create the questions from the give specifications
 */

	public void createQuestions () {
		qg = new Questgen (conf);
		qg.generate ();
		String textfile = conf.getProperty ("questions_file");
		String resultfile = dir_interpreter + textfile;
		if (okOverwrite (resultfile)) qg.saveQuestions ();
		if (conf.checkProperty ("lm_training_file")) {
			String sentfile = conf.getProperty ("lm_training_file");
			createLmQuestions (dir_interpreter, dir_recognizer,
					textfile, sentfile);
		}
	}

/**
 * Create only the things needed by the language model of the
 * speech recognizer. This can be called to modify the language model
 * after adding questions manually.
 */

	public void updateLmQuestions () {
		String textfile = conf.getProperty ("questions_file");
		if (conf.checkProperty ("lm_training_file")) {
			String sentfile = conf.getProperty ("lm_training_file");
			createLmQuestions (dir_interpreter, dir_recognizer,
					textfile, sentfile);
		}
	}

	void createLmQuestions (String dirinter, String dirrec,
			String textfile, String sentences) {
		try {
			String srcfile = dirinter + textfile;
			String destfile = dirrec + sentences;
			String line;
			BufferedReader in = new BufferedReader (new FileReader (srcfile));
			PrintWriter out = new PrintWriter (new FileWriter (destfile));

			while ((line = in.readLine ()) != null) {
				String trim = line.trim ();
				if (trim.length () == 0) continue;
				int pos = trim.indexOf ("\t");
				if (pos == -1) continue;
				String q = trim.substring (0, pos).trim ();
				String upper = q.toUpperCase ();
				out.println ("<s> " + upper + " </s>");
			}
			in.close ();
			out.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * This creates a way to handle some commands based on the application's
 * needs. This can be overridden to do non-standard things.
 */
	
	public void createCustomCommands () {
		QaList List = qg.Gram.List;
		TreeMap <String, QaNode> lookup = List.lookup;
		Set <String> lkeys = lookup.keySet ();
		LinkedHashMap <String, Integer> F =
				new LinkedHashMap <String, Integer> ();
		for (Iterator<String> it = lkeys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			addFunctions (F, key);
			QaNode node = lookup.get (key);
			if (node == null) continue;
			/*
			Vector<String> tail = node.tail;
			for (int i=0; i<tail.size (); i++) {
				String t = tail.elementAt (i);
				addFunctions (F, t);
			}*/
		}
		// generate using all these functions as options
		if (F.size () > 0) generateCommand (F);
	}

	void generateCommand (LinkedHashMap <String, Integer> F) {
		try {
			Log.info ("Generating "+commandFile);
			PrintWriter out = new PrintWriter
					(new FileWriter (commandFile));
			out.println ("\nimport java.util.Vector;\n");
			out.println ("import com.jaivox.interpreter.Command;");
			out.println ("import com.jaivox.interpreter.HistNode;");
			out.println ();
			out.println ("public class "+commandName + " extends Command {\n");
			out.println ("\tpublic "+commandName + " () {\n\t}\n");
			out.println ("\tpublic String [] handle (String f,");
			out.println ("\t\tString question, String spec, String instate,");
			out.println ("\t\tVector <HistNode> history) {");
			Set <String> fkeys = F.keySet ();
			boolean started = false;
			for (Iterator<String>it = fkeys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				if (!started) {
					out.println ("\t\tif (f.equals (\""+key+"\"))");
					started = true;
				}
				else {
					out.println ("\t\telse if (f.equals (\""+key+"\"))");
				}
				out.println ("\t\t\treturn "+key+" (question, spec, instate, history);");
			}
			if (started) out.println ("\t\telse return null;");
			out.println ("\t}\n");
			for (Iterator<String>it = fkeys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				out.println ("\tString [] "+key+
						" (String question, String spec, String instate, Vector <HistNode> history) {");
				out.println ("\t\tString result [] = new String [1];");
				out.println ("\t\tresult [0] = \"implement "+key+" command!\";");
				out.println ("\t\treturn result;\n\t}\n");
			}
			out.println ("}\n");
			out.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void addFunctions (LinkedHashMap <String, Integer> F, String t) {
		int pos = t.indexOf ("(");
		if (pos == -1) return;

		int n = t.length ()-1;
		while (pos != -1 && pos<n) {
			int qos = t.indexOf (")", pos+1);
			if (qos == -1) return;
			String inner = t.substring (pos+1, qos);
			StringTokenizer st = new StringTokenizer (inner);
			if (!st.hasMoreTokens ()) return;
			String name = st.nextToken ();
			Integer I = F.get (name);
			if (I == null) {
				F.put (name, One);
			}
			pos = t.indexOf ("(", qos+1);
		}
	}



};




