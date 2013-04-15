/*
   Jaivox version 0.4 April 2013
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

import com.jaivox.util.Log;

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
import java.util.Vector;


/**
 * The Generator creates all the files for an application, based on
 * specifications. It also uses several templates and common files
 * located in the common directory (you can specify the location of the
 * common files in the configuration file.)
 */

public class Generator {

	static Properties kv;
	
/**
 * Patterns to be replaced in templates start with patIndicator,
 * the default value is "PAT"
 */
	public static String patIndicator = "PAT";
	
	Set <String> keys;
	String source = "./";
	String common = "./";
	String destination = "./";
	String dir_recognizer;
	String dir_interpreter;
	String dir_synthesizer;
	String onefile;
	boolean onedir = false;
	boolean overwrite = false;

	static String templates [] = {
		"interpreterTest.java",
		"recognizerTest.java",
		"project.config.xml",
		"lmgen.sh",
		"runinter.sh",
		"runsphinx.sh"
	};
	
	static String festival [] = {
		"CxxServer.cc",
		"CxxResponder.cc",
		"CxxSession.cc"
		};
	
	static String freetts [] = {
		"synthesizerTest.java",
		"compsynth.sh",
		"runsynth.sh"
	};
	
	static String batch [] = {
		"batch.xml",
		"batchTest.java",
		"lmgen.sh",
		"compbatch.sh",
		"runbatch.sh",
		"ccs.ccs"
	};
	
	static String live [] = {
		"live.xml",
		"liveTest.java",
		"lmgen.sh",
		"complive.sh",
		"runlive.sh",
		"ccs.ccs"
	};
	
	static String console [] = {
		"consoleTest.java"
	};

	String lmtool = "lmgen.sh";
	String runsphinx = "runsphinx.sh";
	String runinter = "runinter.sh";

	String commandName;
	String commandFile;
	Questgen qg;
	static Integer One = new Integer (1);



/**
 * Generate files based on a configuration file
@param confname
 */
	public Generator (String confname) {
		new Config (confname);
		kv = Config.kv;
		String base = (String) (kv.get ("Base"));
		String overwrite_answer = kv.getProperty ("overwrite_files");
		if (overwrite_answer != null) {
			if (overwrite_answer.equals ("yes")) overwrite = true;
		}
		String useonedir = kv.getProperty ("onedirectory");
		if (useonedir != null) {
			if (useonedir.equals ("yes")) onedir = true;
		}
		onefile = kv.getProperty ("onefile");
		String src = kv.getProperty ("source");
		if (src != null && !src.equals ("null")) source = base + src;
		String cmn = kv.getProperty ("common");
		if (cmn != null) common = base + cmn;
		String dest = kv.getProperty ("destination");
		if (dest != null) destination = base + dest;
		Log.info ("Source:"+source+" Common:"+common+" Dest:"+destination);
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
		// Use the directory name to create Command name
		commandName = kv.getProperty ("project") + "Command";
		commandFile = dir_interpreter + commandName + ".java";

	}

/**
 * generate everything, i.e. speech, festival and interpreter agents,
 * and files required by these agents.
 */
	public void generateAll () {
			// branch away if a single file generation
		if (onefile != null) {
			if (onefile.equals ("batch")) {
				generateBatch ();
				return;
			}
			else if (onefile.equals ("live")) {
				generateLive ();
				return;
			}
			else if (onefile.equals ("console")) {
				generateConsole ();
				return;
			}
		}
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
			for (int i=0; i<files.length; i++) {
				String name = files [i];
				if (tpls.get (name) != null) continue;
				String dest = null;
				if (name.endsWith (".cc")) continue;
				else if (name.endsWith (".h")) continue;
				else if (name.endsWith ("ccs.ccs")) dest = dir_recognizer;
				else if (name.endsWith ("makefile")) continue;
				if (dest != null) {
					if (!copyFile (common, dest, name)) {
						Log.severe ("Could not copy "+name+" to "+destination);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = kv.getProperty ("data_file");
			if (datafile != null && !copyFile (source, dir_interpreter, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = kv.getProperty ("common_words");
			if (!copyFile (common, dir_interpreter, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = kv.getProperty ("specs_file");
			if (specs != null && !copyFile (source, dir_interpreter, specs)) {
				Log.severe ("Could not copy specifications file");
				return;
			}
			String grammar = kv.getProperty ("grammar_file");
			if (grammar != null && !copyFile (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy grammar file ");
				return;
			}
			if (!copyIncludedFiles (source, dir_interpreter, grammar)) {
				Log.severe ("Could not copy all included files in "+grammar);
				return;
			}
			
			// generate for festival or freetts
			String synth = kv.getProperty ("synthesizer");
			if (synth.equals ("festival")) {
				generateFestival ();
			}
			else if (synth.equals ("freetts")) {
				generateFreetts ();
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	void generateBatch () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			
			String batchname = kv.getProperty ("batch");
			if (batchname == null) {
				kv.setProperty ("batch", "batch");
			}
			for (int i=0; i<batch.length; i++) {
				String name = batch [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = kv.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = kv.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = kv.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.severe ("Could not copy specifications file");
				return;
			}
			String grammar = kv.getProperty ("grammar_file");
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

	void generateLive () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			
			String livename = kv.getProperty ("live");
			if (livename == null) {
				kv.setProperty ("live", "live");
			}
			for (int i=0; i<live.length; i++) {
				String name = live [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = kv.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = kv.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = kv.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.severe ("Could not copy specifications file");
				return;
			}
			String grammar = kv.getProperty ("grammar_file");
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
	
	void generateConsole () {
		boolean ok = false;
		try {
			File fd = new File (destination);
			if (!fd.exists ()) fd.mkdirs ();
			Hashtable <String, String> tpls = new Hashtable <String, String> ();
			String yes = "yes";
			
			String livename = kv.getProperty ("console");
			if (livename == null) {
				kv.setProperty ("console", "console");
			}
			for (int i=0; i<console.length; i++) {
				String name = console [i];
				tpls.put (name, yes);
				ok = generateFile (destination, name);
				if (!ok) {
					Log.severe ("Could not generate from "+name);
					return;
				}
			}
			// copy specific files to interpreter
			String datafile = kv.getProperty ("data_file");
			if (datafile != null && !copyFile (source, destination, datafile)) {
				Log.info ("Could not find data file "+datafile);
				// return;
			}
			String commonwords = kv.getProperty ("common_words");
			if (!copyFile (common, destination, commonwords)) {
				Log.severe ("Could not copy common words file");
				return;
			}
			String specs = kv.getProperty ("specs_file");
			if (specs != null && !copyFile (source, destination, specs)) {
				Log.severe ("Could not copy specifications file");
				return;
			}
			String grammar = kv.getProperty ("grammar_file");
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
			for (int i=0; i<festival.length; i++) {
				String name = festival [i];
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
	
	public void generateFreetts () {
		boolean ok = false;
		try {
			// File src = new File (source);
			File src = new File (common);
			for (int i=0; i<freetts.length; i++) {
				String name = freetts [i];
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
			keys = kv.stringPropertyNames ();
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
		qg = new Questgen (kv);
		qg.generate ();
		String textfile = kv.getProperty ("questions_file");
		String resultfile = qg.datadir + textfile;
		if (okOverwrite (resultfile)) qg.saveQuestions ();
		String sentfile = kv.getProperty ("lm_training_file");
		if (sentfile == null) return;
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

	void createCustomCommands () {
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




