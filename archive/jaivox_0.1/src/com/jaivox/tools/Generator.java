
package com.jaivox.tools;

import java.io.*;
import java.util.*;

public class Generator {

	Properties kv;
	Set <String> keys;
	String source = "./";
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
		if (src != null) source = src;
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

	void Debug (String s) {
		System.out.println ("[Generator]" + s);
	}

	public void generateAll () {
		boolean ok = false;

		try {
			File src = new File (source);
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
					Debug ("Could not generate from "+name);
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
					if (!copyFile (source, dest, name)) {
						Debug ("Could not copy "+name+" to "+destination);
						return;
					}
				}
			}
			// copy specific files to interpreter
			String datafile = kv.getProperty ("data_file");
			if (!copyFile (source, dir_interpreter, datafile)) {
				Debug ("Could not copy data file ");
				return;
			}
			String common = kv.getProperty ("common_words");
			if (!copyFile (source, dir_interpreter, common)) {
				Debug ("Could not copy common words file");
				return;
			}
			String specs = kv.getProperty ("specs_file");
			if (!copyFile (source, dir_interpreter, specs)) {
				Debug ("Could not copy specifications file");
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	boolean generateFile (String dest, String name) {
		try {
			String filename = source + name;
			String destname = dest + name;
			if (!okOverwrite (destname)) {
				Debug (destname+" exists. To overwrite, set overwrite_files to yes");
				return true;
			}
			String text = loadFile (filename);
			String changed = text;
			for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				String val = kv.getProperty (key);
				if (name.startsWith (key)) {
					String newname = name.replaceFirst (key, val);
					// Debug ("Destination name: "+newname);
					destname = dest + newname;
					if (!okOverwrite (destname)) {
						Debug (destname+" exists. To overwrite, set overwrite_files to yes");
						return true;
					}
				}
				String pat = "PAT"+key;
				if (text.indexOf (pat) != -1) {
					// Debug ("replacing "+pat+" with "+val+" in "+name);
					changed = changed.replace (pat, val);
				}
			}
			if (!writeFile (destname, changed)) return false;
			Debug ("wrote: "+destname);
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
			String sourcefile = src + filename;
			String destfile = dest + filename;
			if (!okOverwrite (destfile)) {
				Debug (destfile+" exists. To overwrite, set overwrite_files to yes");
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
			Debug ("Copied "+filename);
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

	public void createQuestions () {
		Questgen qg = new Questgen (source, dir_interpreter, kv);
		qg.generate ();
		qg.saveQuestions ();
		if (!onedir) {
			String textfile = kv.getProperty ("questions_file");
			String sentfile = kv.getProperty ("lm_training_file");
			createLmQuestions (dir_interpreter, dir_recognizer, 
					textfile, sentfile);
		}
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




