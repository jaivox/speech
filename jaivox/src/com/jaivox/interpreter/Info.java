/*
   Jaivox version 0.6 December 2013
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

package com.jaivox.interpreter;

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 * The Info class holds all the data associated with a particular
 * interpreter. The raw data is stored in an array of strings.
 * All the data comes from a particular directory.
 * The Info class could possibly get its data from a database. But
 * in this implementation, the data is in text files.
 */

public class Info {

	boolean Valid;

	String datadir;
	TreeMap <String, Infonode> specs;
	TreeMap <String, Datanode> data;

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
		if (!loadSpecs (specfile)) return;
		if (!loadData ()) return;
		Valid = true;
	}

/**
 * The specifications for data include information about various fields and their
 * attributes, along with alternate ways of asking about these. The data is loaded
 * as a set of Infonodes.
 * @param specfile
 * @return
 */
	
	boolean loadSpecs (String specfile) {
		try {
			String filename = datadir + specfile;
			BufferedReader in = new BufferedReader (new FileReader (filename));
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
	
/**
 * Use information in the specifications to locate data files that may
 * contain details about some fields and their attributes. A data file
 * is generally indicated in the specifications with "type table". It does
 * not have to be present, since any data processing to answer questions
 * will have to be created by the programmer. But if the file is present
 * it is loaded by this function.
 * @return
 */

	boolean loadData () {
		// find a table specification
		Set <String> keys = specs.keySet ();
		data = new TreeMap <String, Datanode> ();
		boolean found = false;
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Infonode node = specs.get (key);
			// will load only one file
			String type = node.tagvals ("type");
			// Log.fine ("loadData, node:"+node.name+" type:"+type);
			if (type.equals ("table")) {
				String filename = datadir + key;
				// is there a data file present? If so, it will be used
				// in recognizing words in the file
				File F = new File (filename);
				if (F.exists ()) {
					Datanode detail = new Datanode (this, node, filename);
					data.put (key, detail);
					found = true;
				}
				else {
					Log.info ("Data file "+filename+" not found, not loaded");
					return true;
				}
			}
		}
		return found;
	}
	
/**
 * Show all the specifications that are loaded by printing them to the
 * screen. This is just a function that can quickly check if the information
 * in the specification is correctly parsed and loaded.
 */
	
	public void showspecs () {
		Set <String> keys = specs.keySet ();
		for (Iterator <String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Infonode node = specs.get (key);
			node.showdetails ();
		}
		
	}
		
	// getters and setters
	
/**
 * Get a node described in the specifications and identified by the unique
 * tag. The tag here is the first word in the specifications for that
 * infonode.
 * @param tag
 * @return
 */
	public Infonode getInfonode (String tag) {
		Infonode node = specs.get (tag);
		return node;
	}


	public boolean isValid () {
		return Valid;
	}

	public void setValid (boolean Valid) {
		this.Valid = Valid;
	}

	public String getDatadir () {
		return datadir;
	}

	public void setDatadir (String datadir) {
		this.datadir = datadir;
	}

	public TreeMap<String, Infonode> getSpecs () {
		return specs;
	}

	public void setSpecs (TreeMap<String, Infonode> specs) {
		this.specs = specs;
	}

	public TreeMap<String, Datanode> getData () {
		return data;
	}

	public void setData (TreeMap<String, Datanode> data) {
		this.data = data;
	}

};

