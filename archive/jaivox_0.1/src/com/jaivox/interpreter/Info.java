
/**
 * The Info class holds all the data associated with a particular
 * interpreter. The raw data is stored in an array of strings.
 * All the data comes from a particular directory.
 * The Info class could possibly get its data from a database. But
 * in this implementation, the data is in text files.
 */

package com.jaivox.interpreter;

import java.io.*;
import java.util.*;

public class Info {

	public boolean Valid;

	static String datadir;
	TreeMap <String, Infonode> specs;
	Adjective Adj;

	String data [][];
	String fields [];
	String categories [];
	int nr, nf;

/**
 * Create an info class. All the required data is assumed to be present
 * in a specified directory. The specifications provide details of the
 * data beyond the usual data specifications. For instance, the spec file
 * should contain information about adjectives used to describe data. 
@param dir	The directory containing all the required data
@param specfile	Specifications of the data
 */

	public Info (String dir, String specfile) {
		datadir = dir;
		Adj = new Adjective ();
		if (!loadSpecs (specfile)) return;
		if (!loadData ()) return;
		Valid = true;
	}

	void Debug (String s) {
		System.out.println ("[Info]" + s);
	}

	boolean loadSpecs (String specfile) {
		try {
			String filename = datadir + specfile;
			BufferedReader in = new BufferedReader (new FileReader (filename));
			StringBuffer sb = new StringBuffer ();
			String line;
			specs = new TreeMap <String, Infonode> ();

			while ((line = in.readLine ()) != null) {
				if (line.trim ().startsWith ("//")) continue;
				if (line.trim ().startsWith ("{")) {
					Vector <String> hold = new Vector <String> ();
					while ((line = in.readLine ()) != null) {
						if (line.trim ().startsWith ("//")) continue;
						line = line.toLowerCase ();
						if (line.trim ().startsWith ("}")) {
							Infonode node = new Infonode (datadir, hold);
							node.buildAdjectives (Adj);
							specs.put (node.name, node);
							break;
						}
						else hold.add (line);
					}
				}
			}
			in.close ();

			if (specs.size () > 0) return true;
			else {
				Debug ("No nodes created");
				return false;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	boolean loadData () {
		// find a table specification
		Set <String> keys = specs.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Infonode node = specs.get (key);
			// will load only one file
			String type = node.tagvals ("type");
			// Debug ("loadData, node:"+node.name+" type:"+type);
			if (type.equals ("table")) {
				String filename = datadir + key;
				return loadFile (node, filename);
			}
		}
		Debug ("Data not loaded");
		return false;
	}

	boolean loadFile (Infonode node, String filename) {
		try {
			// get nf, fields, categories
			// Debug ("Loading data for "+node.name+" from "+filename);
			fields = node.tagvalarray ("columns");
			nf = fields.length;
			categories = new String [nf];
			for (int i=0; i<nf; i++) {
				String col = fields [i];
				Infonode sub = specs.get (col);
				if (sub == null) {
					Debug ("No information for column "+col);
					return false;
				}
				String cat = sub.tagvals ("category");
				if (cat == null) {
					Debug ("No category information for column "+col);
					return false;
				}
				categories [i] = cat;
			}

			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			Vector <String []> hold = new Vector <String []> ();
			while ((line = in.readLine ()) != null) {
				if (line.trim ().length () == 0) continue;
				line = line.toLowerCase ();
				StringTokenizer st = new StringTokenizer (line, ",\r\n");
				if (st.countTokens () != nf) {
					Debug ("Expected "+nf+" fields in "+line);
					return false;
				}
				String words [] = new String [nf];
				for (int i=0; i<nf; i++) {
					words [i] = st.nextToken ().trim ();
				}
				hold.add (words);
			}
			in.close ();

			nr = hold.size ();
			data = new String [nr][nf];
			for (int i=0; i<nr; i++) {
				String words [] = hold.elementAt (i);
				for (int j=0; j<nf; j++) {
					data [i][j] = words [j];
				}
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	void showData () {
		StringBuffer sb = new StringBuffer ();
		sb.append ("nf = "+nf+"\n");
		for (int i=0; i<nf; i++) {
			sb.append (fields [i]);
			sb.append (" - ");
			sb.append (categories [i]);
			sb.append ("\n");
		}

		sb.append ("nr = "+nr+"\n");
		for (int i=0; i<nr; i++) {
			for (int j=0; j<nf; j++) {
				sb.append (data [i][j]);
				if (j < nf-1) sb.append ("\t");
				else sb.append ("\n");
			}
		}
		String all = new String (sb);
		System.out.println (all);
	}

};

