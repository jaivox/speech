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

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * Check an input specification and report errors.
 */

public class Check {

	Questgen quest;
	Properties kv;
	String [] names;
	TreeMap <String, Infonode> infos;
	Infonode [] specs;
	int N;

	String dataname;
	String datafile;

	String msgWP = "Fields should specify question form, such as \"What\", \"Who\" etc.";
	String msgELS = "Please specify how the user may ask for alternative answers, suggestion \"besides\"";
	String msgNN = "Please specify othe synonyms for this field";
	String msgNNS = "Please specify plural forms corresponding to noun forms";
	// String msgNNP = "Please indicate the column of the table containing proper names";
	// add a note here about the syntax?

	String msgJJ_P = "Please specify positive adjective forms of this attribute";
	String msgJJ_N = "Please specify negative adjdtive forms of this attribute";
	String msgRB = "Please specify positive and negative adverbial forms of this attribute";
	String msgRBR = "Please specify the positive and negative comparative adverbial forms of this attribute";
	String msgRBS = "Please specify the positive and negative superlative adverbial forms of this attribute";

/**
 * See if the specifications available to generate questions are given
 * in the correct format.
@param q
 */

	public Check (Questgen q) {
		quest = q;
		TreeMap <String, Infonode> infos = quest.infos;
		boolean OK = false;
		OK = createspecs (infos);
		if (!OK) {
			Log.severe ("Check: error in specifications, returning ...");
			return;
		}
	}

	boolean createspecs (TreeMap <String, Infonode> inf) {
		try {
			infos = inf;
			Set <String> keys = infos.keySet ();
			N = infos.size ();
			names = new String [N];
			specs = new Infonode [N];
			int i = 0;
			for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				Infonode info = infos.get (key);
				names [i] = key;
				specs [i] = info;
				i++;
			}
			if (i < N) {
				Log.warning ("Specifications table in Questgen counting incorrectly.");
				return false;
			}
			else {
				return true;
			}
		}
		catch (Exception e) {
			Log.severe ("Check: createspecs - "+e.toString ());
			return false;
		}
	}

/**
 * called from Questgen to check whether everything is ok before generating
 * questions. This function calls several other functions to check various
 * details of the specified data.
@return
 */
	public boolean checkAll () {
		boolean OK = false;
		OK = checkForTable ();
		if (!OK) return false;
		OK = checkFields ();
		if (!OK) return false;
		OK = checkAttributes ();
		return OK;
	}


	boolean checkForTable () {
		try {
			// see if there is a table and whether that exists
			for (int i=0; i<N; i++) {
				Infonode node = specs [i];
				if (node.name == null) {
					Log.severe ("Check: No name for specification #"+i);
					return false;
				}
				String type = node.tagvals ("type");
				if (type == null) {
					Log.severe ("Check: No type in specification "+node.name);
					return false;
				}
				if (type.equalsIgnoreCase ("table")) {
					boolean OK = checkTableForDetails (i, node);
					return OK;
				}
			}
			// if we came here did not find the table
			Log.severe ("No specification for data table, of type \"table\"");
			return false;
		}
		catch (Exception e) {
			Log.severe ("Check: checkForTable - "+e.toString ());
			return false;
		}
	}

	boolean checkTableForDetails (int pos, Infonode node) {
		try {
			String name = node.name;
			// assume this is file in the source directory
			String base = quest.kv.getProperty ("Base");
			String specdir = quest.kv.getProperty ("source");
			Log.fine ("Assumes "+name+" is a file in "+specdir);
			datafile = base + specdir + name;
			dataname = name;
			File F = new File (datafile);
			if (!F.exists ()) {
				Log.warning ("Could not check Data file "+datafile+" (does not exist.) ");
				// return false;
				return true;
			}
			// check that the data file contains the columns that are mentioned
			String fields [] = quest.fields;
			// is it possible to have multiple fields and attributes in the same table?
			// fa will contain the value "" for fields and the name of the field
			// itself for attributes.
			Hashtable <String, String> fa = new Hashtable <String, String> ();
			for (int i=0; i<fields.length; i++) {
				String field = fields [i];
				if (field.equals ("")) {
					Log.warning ("Field "+i+" is an empty string, ingored");
					continue;
				}
				fa.put (field, "");
				Infonode finfo = infos.get (field);
				String [] attrs = finfo.tagval.get ("attributes");
				for (int j=0; j<attrs.length; j++) {
					fa.put (attrs [j], field);
					// check that there are nodes defined for each of these attributes
					Infonode atnode = infos.get (attrs [j]);
					if (atnode == null) {
						Log.severe ("Atrribute "+attrs [j]+" for field "+field+" not specified.");
						Log.severe ("Each attribute should have a detailed specification");
						return false;
					}
				}
			}
			// get the columns of the table
			String columns [] = node.tagvalarray ("columns");
			if (columns == null) {
				Log.severe ("Specifications for "+name+" missing line tagged \"columns\"");
				return false;
			}
			// first check if the column names are in the fa field
			for (int i=0; i<columns.length; i++) {
				String col = columns [i];
				if (fa.get (col) == null) {
					Log.severe ("No specifications for column "+col+ " in "+datafile);
					return false;
				}
			}
			boolean OK = checkData (dataname, datafile, columns, fa);
			return OK;
		}
		catch (Exception e) {
			Log.severe ("Check: checkTableForDetails - "+e.toString ());
			return false;
		}
	}

	boolean checkData (String dataname, String datafile, String [] columns,
		Hashtable <String, String> fa) {
		try {
			Log.fine ("Looking for comma separated data in "+datafile);
			BufferedReader in = new BufferedReader (new FileReader (datafile));
			String line;
			int n = columns.length;
			boolean found = false;
			while ((line = in.readLine ()) != null) {
				if (line.trim ().length () == 0) continue;
				StringTokenizer st = new StringTokenizer (line, ",");
				int m = st.countTokens ();
				if (m == n) found = true;
				else {
					Log.severe ("Expected "+n+" comma separated fields in "+line);
					Log.severe ("Data check failed");
					in.close ();
					return false;
				}
				for (int j=0; j<n; j++) {
					String token = st.nextToken ().trim ();
					String col = columns [j];
					// checked in checkTableForDetails that the following is non null
					String f = fa.get (col);
					if (!f.equals ("")) { // it is an attribute
						if (!isNumeric (token)) {
							Log.severe ("Attribute "+col+" in line "+line+" should be numeric");
							in.close ();
							return false;
						}
					}
				}
			}
			if (!found) {
				Log.severe ("Data file "+datafile+" does not contain "+n+" comma=separated data");
				return false;
			}
			Log.info ("Data seems to be in correct format.");
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: checkData - "+e.toString ());
			return false;
		}
	}

	boolean isNumeric (String s) {
		try {
			new Double (s).doubleValue ();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	boolean checkFields () {
		try {
			String fields [] = 	quest.fields;
			for (int i=0; i<fields.length; i++) {
				boolean OK = checkField (fields [i]);
				if (!OK) return OK;
			}
			Log.info ("Fields seem to be properly specified.");
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: checkFields - "+e.toString ());
			return false;
		}
	}


	boolean checkField (String field) {
		try {
			// get the infonode
			Infonode fnode = null;
			for (int i=0; i<specs.length; i++) {
				Infonode node = specs [i];
				if (node.name.equals (field)) {
					fnode = node;
					break;
				}
			}
			if (fnode == null) {
				Log.severe ("No secifications found for field "+field);
				return false;
			}

			// continue with checking fnode
			// node should have attributes
			// if there are attributes, we have already checked in checkTableForDetails
			// that they attributes are specified in detail
			String attrs [] = fnode.tagvalarray ("attributes");
			if (attrs == null) {
				Log.severe ("Field "+field+" has no attributes");
				return false;
			}
			// check various required grammar tags
			/*
			if (!checkGrammar (field, fnode, "WP", msgWP)) return false;
			if (!checkGrammar (field, fnode, "ELS", msgELS)) return false;
			if (!checkGrammar (field, fnode, "NN", msgNN)) return false;
			if (!checkGrammar (field, fnode, "NNS", msgNNS)) return false;
			// if (!checkGrammar (field, fnode, "NNP", msgNNP)) return false;
			// NNP may need an extra check of the table
			Log.info ("Field "+field+" seems to be specified correctly.");
			*/
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: checkField - "+e.toString ());
			return false;
		}
	}

	boolean checkGrammar (String head, Infonode node, String tag, String msg) {
		try {
			String vals [] = node.tagvalarray (tag);
			if (vals == null) {
				Log.severe ("Expected values for "+tag+" in specs for "+head);
				Log.severe (msg);
				return false;
			}
			Log.info ("Grammar tag "+tag+" used correctly for "+head);
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: xxx - "+e.toString ());
			return false;
		}
	}

	boolean checkAttributes () {
		try {
			String fields [] = 	quest.fields;
			for (int i=0; i<fields.length; i++) {
				boolean OK = checkFieldAttributes (fields [i]);
				if (!OK) return OK;
			}
			Log.info ("Attributes seem to be correctly specified.");
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: checkFields - "+e.toString ());
			return false;
		}
	}


	boolean checkFieldAttributes (String field) {
		try {
			// get the infonode
			Infonode fnode = null;
			for (int i=0; i<specs.length; i++) {
				Infonode node = specs [i];
				if (node.name.equals (field)) {
					fnode = node;
					break;
				}
			}
			// already checked in checkField whether fnode is null
			String attrs [] = fnode.tagvalarray ("attributes");
			// already checked in checkField whether attrs is null
			for (int i=0; i<attrs.length; i++) {
				if (!checkAttribute (field, attrs [i])) return false;
			}
			Log.info ("Attributes for field "+field+" seem to be correctly specified.");
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: checkFieldAttributes - "+e.toString ());
			return false;
		}
	}

	boolean checkAttribute (String field, String attr) {
		try {
			// looks like we just have to check the various tags
			Infonode anode = infos.get (attr);
			if (anode == null) {
				Log.severe ("No specifications for attribute "+attr+" of field "+field);
				return false;
			}
			if (!checkGrammar (attr, anode, "JJ-P", msgJJ_P)) return false;
			if (!checkGrammar (attr, anode, "JJ-N", msgJJ_N)) return false;
			if (!checkGrammar (attr, anode, "RB", msgRB)) return false;
			if (!checkGrammar (attr, anode, "RBR", msgRBR)) return false;
			if (!checkGrammar (attr, anode, "RBS", msgRBS)) return false;
			Log.info ("Attribute "+attr+" for field "+field+" seems to be correctly specified.");
			return true;
		}
		catch (Exception e) {
			Log.severe ("Check: checkAttribute - "+e.toString ());
			return false;
		}
	}
}

