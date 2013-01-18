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

package com.jaivox.interpreter;

import java.io.*;
import java.util.*;

import com.jaivox.util.Log;

/**
 * The Info class holds all the data associated with a particular
 * interpreter. The raw data is stored in an array of strings.
 * All the data comes from a particular directory.
 * The Info class could possibly get its data from a database. But
 * in this implementation, the data is in text files.
 */


public class Info {

	public boolean Valid;

	static String datadir;
	TreeMap <String, Infonode> specs;
	Adjective Adj;

	String data [][];
	String fields [];
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
				Log.warning ("No nodes created");
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
			// Log.fine ("loadData, node:"+node.name+" type:"+type);
			if (type.equals ("table")) {
				String filename = datadir + key;
				return loadFile (node, filename);
			}
		}
		Log.severe ("Data not loaded");
		return false;
	}

	boolean loadFile (Infonode node, String filename) {
		try {
			// get nf, fields
			// Log.fine ("Loading data for "+node.name+" from "+filename);
			fields = node.tagvalarray ("columns");
			nf = fields.length;
			for (int i=0; i<nf; i++) {
				String col = fields [i];
				Infonode sub = specs.get (col);
				if (sub == null) {
					Log.severe ("No information for column "+col);
					return false;
				}
			}

			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			Vector <String []> hold = new Vector <String []> ();
			while ((line = in.readLine ()) != null) {
				if (line.trim ().length () == 0) continue;
				line = line.toLowerCase ();
				StringTokenizer st = new StringTokenizer (line, ",\r\n");
				if (st.countTokens () != nf) {
					Log.severe ("Expected "+nf+" fields in "+line);
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
			Log.severe ("Info:loadFile "+e.toString ());
			e.printStackTrace ();
			return false;
		}
	}

	void showData () {
		StringBuffer sb = new StringBuffer ();
		sb.append ("nf = "+nf+"\n");
		for (int i=0; i<nf; i++) {
			sb.append (fields [i]);
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

