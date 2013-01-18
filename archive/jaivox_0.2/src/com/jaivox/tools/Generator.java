/*
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
*/

package com.jaivox.tools;

import java.io.*;
import java.util.*;

import com.jaivox.util.Log;

/**
 * The Generator creates all the files for an application, based on
 * specifications. It also uses several templates and common files
 * located in the common directory (you can specify the location of the
 * common files in the configuration file.)
 */

public class Generator {

	Properties kv;
	Set <String> keys;
	String source = "./";
	String common = "./";
	String destination = "./";
	String dir_recognizer;
	String dir_interpreter;
	String dir_synthesizer;
	boolean onedir = false;
	boolean overwrite = false;

	static String templates [] = {
		"interpreterTest.java",
		"recognizerTest.java",
		"project.config.xml",
		"lmgen.sh",
		"CxxServer.cc",
		"CxxResponder.cc",
		"CxxSession.cc"
		};

	String lmtool = "lmgen.sh";

/**
 * Generate files based on a configuration file
@param confname
 */
	public Generator (String confname) {
		Config conf = new Config (confname);
		kv = conf.kv;
		keys = kv.stringPropertyNames ();
		String overwrite_answer = kv.getProperty ("overwrite_files");
		if (overwrite_answer != null) {
			if (overwrite_answer.equals ("yes")) overwrite = true;
		}
		String useonedir = kv.getProperty ("onedirectory");
		if (useonedir != null) {
			if (useonedir.equals ("yes")) onedir = true;
		}
		String src = kv.getProperty ("source");
		if (src != null && !src.equals ("null")) source = src;
		String cmn = kv.getProperty ("common");
		if (cmn != null) common = cmn;
		String dest = kv.getProperty ("destination");
		if (dest != null) destination = dest;
		if (onedir) {
			dir_interpreter = destination;
			dir_recognizer = destination;
			dir_synthesizer = destination;
		}
		else {
			dir_interpreter = destination + kv.getProperty ("dir_interpreter") + "/";
			dir_recognizer = destination + kv.getProperty ("dir_recognizer") + "/";
			dir_synthesizer = destination + kv.getProperty ("dir_synthesizer") + "/";
		}
	}

/**
 * generate everything, i.e. speech, festival and interpreter agents,
 * and files required by these agents.
 */
	public void generateAll () {
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
			for (int i=0; i<templates.length; i++) {
				String name = templates [i];
				tpls.put (name, yes);
				String dest = dir_interpreter;
				if (name.endsWith (".cc")) dest = dir_synthesizer;
				else if (name.endsWith (".h")) dest = dir_synthesizer;
				else if (name.endsWith ("makefile")) dest = dir_synthesizer;
				else if (name.startsWith ("recognizer") || name.endsWith (".config.xml") ||
				name.equals (lmtool)) dest = dir_recognizer;
				ok = generateFile (dest, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			for (int i=0; i<files.length; i++) {
				String name = files [i];
				if (tpls.get (name) != null) continue;
				String dest = null;
				if (name.endsWith (".cc")) dest = dir_synthesizer;
				else if (name.endsWith (".h")) dest = dir_synthesizer;
				else if (name.endsWith ("ccs.ccs")) dest = dir_recognizer;
				else if (name.endsWith ("makefile")) dest = dir_synthesizer;
				if (dest != null) {
					if (!copyFile (common, dest, name)) {
						Log.severe ("Could not copy "+name+" to "+destination);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = kv.getProperty ("data_file");
			if (!datafile.equals ("null") && !copyFile (source, dir_interpreter, datafile)) {
				Log.severe ("Could not copy data file ");
				return;
			}
			String commonwords = kv.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = kv.getProperty ("specs_file");
			if (!specs.equals ("null") && !copyFile (source, dir_interpreter, specs)) {
				Log.severe ("Could not copy specifications file");
				return;
			}
			String grammar = kv.getProperty ("grammar_file");
			if (!grammar.equals ("null") && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	boolean generateFile (String dest, String name) {
		try {
			String filename = common + name;
			String destname = dest + name;
			if (!okOverwrite (destname)) {
				Log.severe (destname+" exists. To overwrite, set overwrite_files to yes");
				return true;
			}
			String text = loadFile (filename);
			String changed = text;
			for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				String val = kv.getProperty (key);
				if (name.startsWith (key)) {
					String newname = name.replaceFirst (key, val);
					// Log.fine ("Destination name: "+newname);
					destname = dest + newname;
					if (!okOverwrite (destname)) {
						Log.severe (destname+" exists. To overwrite, set overwrite_files to yes");
						return true;
					}
				}
				String pat = "PAT"+key;
				if (text.indexOf (pat) != -1) {
					// Log.fine ("replacing "+pat+" with "+val+" in "+name);
					changed = changed.replace (pat, val);
				}
			}
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
			if (src == null) return false;
			if (src.equals ("null")) return false;
			String sourcefile = src + filename;
			String destfile = dest + filename;
			if (!okOverwrite (destfile)) {
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
		Questgen qg = new Questgen (kv);
		qg.generate ();
		String textfile = kv.getProperty ("questions_file");
		String resultfile = qg.datadir + textfile;
		if (okOverwrite (resultfile)) qg.saveQuestions ();
		String sentfile = kv.getProperty ("lm_training_file");
		createLmQuestions (dir_interpreter, dir_recognizer, 
				textfile, sentfile);
	}
	
/**
 * Create only the things needed by the language model of the
 * speech recognizer. This can be called to modify the language model
 * after adding questions manually.
 */
	
	public void updateLmQuestions () {
		String textfile = kv.getProperty ("questions_file");
		String sentfile = kv.getProperty ("lm_training_file");
		createLmQuestions (dir_interpreter, dir_recognizer, 
				textfile, sentfile);
	}

	void createLmQuestions (String dirsrc, String dirtarget, 
			String textfile, String sentences) {
		try {
			String srcfile = dirsrc + textfile;
			String destfile = dirtarget + sentences; // should give it a differnet name, later
			String line;
			BufferedReader in = new BufferedReader (new FileReader (srcfile));
			PrintWriter out = new PrintWriter (new FileWriter (destfile));

			while ((line = in.readLine ()) != null) {
				String trim = line.trim ();
				if (trim.length () == 0) continue;
				int pos = trim.indexOf ("(");
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

};




