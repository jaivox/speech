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

package com.jaivox.tools;

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.TreeMap;
import java.util.Vector;

/**
 * QaList hold the information in the grammar (i.e. dialog) file
 * in a list, with each node being one of the things inside { }.
 * It is used in this package as well as the interpreter.
 * @author jaivox
 */
public class QaList {

/**
* the tag for including a dialog file inside another.
*/

	public static final String include = "#include ";

	TreeMap <String, QaNode> lookup;
	TreeMap <String, Integer> location;
	TreeMap <Integer, QaNode> listed;
	TreeMap <String, Vector<QaNode>> parents;
	boolean Valid = false;
	int globalloc = 1;

	public QaList (String filename) {
		lookup = new TreeMap <String, QaNode> ();
		location = new TreeMap <String, Integer> ();
		listed = new TreeMap <Integer, QaNode> ();
		parents = new TreeMap <String, Vector<QaNode>> ();
		if (read (filename))
			Valid = true;
	}

	void Debug (String s) {
		Log.info ("[QaList]" + s);
	}

	boolean read (String filename) {
		try {
			// get the directory
			String dir = "";
			int pos = filename.lastIndexOf ("/");
			if (pos != -1) dir = filename.substring (0, pos+1);
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;

			outer:
			while ((line = in.readLine ()) != null) {
				String rest = line.trim ();
				if (rest.startsWith ("//")) continue;
				if (rest.startsWith (include)) {
					String name = dir + rest.substring (include.length ()).trim ();
					if (!read (name)) {
						Log.severe ("Error reading "+name);
						return false;
					}
					else {
						Log.info ("Including "+name);
					}
				}
				if (rest.startsWith ("{")) {
					StringBuffer sb = new StringBuffer ();
					sb.append (rest);
					sb.append ("\n");
					while ((line = in.readLine ()) != null) {
						rest = line.trim ();
						if (rest.startsWith ("//")) continue;
						if (rest.indexOf ("{") != -1) {
							System.err.println ("Mismatched { }, please check your input");
						}
						sb.append (rest);
						sb.append ("\n");
						if (rest.endsWith ("}")) {
							String production = new String (sb);
							QaNode n = new QaNode (this, production);
							if (n.Valid) {
								// Debug ("Created node "+n.head);
								lookup.put (n.head, n);
								Integer Loc = new Integer (globalloc);
								globalloc++;
								location.put (n.head, Loc);
								listed.put (Loc, n);
								addParent (n);
								continue outer;
							}
						}
					}
				}
			}
			in.close ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}

	void addParent (QaNode node) {
		String tail [] = node.tail;
		for (int i=0; i<tail.length; i++) {
			String child = tail [i];
			Vector <QaNode> before = parents.get (child);
			if (before == null) before = new Vector <QaNode> ();
			before.add (node);
			parents.put (child, before);
		}
	}
	
/**
 * Get the tree map that relates tags to individual QaNodes (the stuff
 * within a { } in the grammar (i.e. dialog.)
 * @return
 */
	public TreeMap <String, QaNode> getLookup () {
		return lookup;
	}

/**
 * Get the QaNode associated with a single tag. If you want to get te
 * whole list, use getLookup.
 * @param tag
 * @return
 */
	public QaNode get (String tag) {
		QaNode match = lookup.get (tag);
		return match;	// may be null
	}

	
/**
 * Where does this tag occur in the list of all QaNodes?
 * @param tag
 * @return
 */
	public int getLocation (String tag) {
		Integer I = location.get (tag);
		if (I != null) return I.intValue ();
		else return -1;
	}
	
/**
 * Get the i-th QaNode
 * @param loc
 * @return
 */

	public QaNode getListed (int loc) {
		Integer Loc = new Integer (loc);
		QaNode node = listed.get (Loc);
		return node;
	}

}

