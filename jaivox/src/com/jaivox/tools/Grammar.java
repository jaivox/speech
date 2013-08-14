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

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Questions are generated according to a set of grammatic templates.
 * For example, the template "NN VB NN" can be the grammar of "dog eat dog".
 */
public class Grammar {

	QaList List;
	TreeMap <String, QaNode> lookup;
	Vector <String> allpaths;
	static int nfsm = 4;
	String fsm [][];

	Vector <String []> patterns;

	static final String terms = " \t\r\n~`!@#$%^&*()+={}[]|\\:;<>,.?/\"\'";
	static final String tagchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ-";

/**
 * Create a grammar from a file. The file is the grammar of a QaList
 * but contains several grammar forms for questions. Extract those
 * forms to create questions.
@param filename
 */
	public Grammar (String filename) {
		List = new QaList (filename);
		lookup = List.lookup;
		createAllPaths ();

		patterns = new Vector <String []> ();

		for (int i=0; i<allpaths.size (); i++) {
			String path = allpaths.elementAt (i);
			String pats [] = getTokens (path);
			patterns.add (pats);
		}
	}

	String [] gettags (String words) {
		StringTokenizer st = new StringTokenizer (words, terms, true);
		int n = st.countTokens ();
		String seq [] = new String [n];
		for (int i=0; i<n; i++) {
			seq [i] = st.nextToken ();
		}
		if (seq [0].trim ().length () == 0) {
			Log.severe ("First word blank in "+words);
		}
		return seq;
	}

	void createAllPaths () {
		allpaths = new Vector <String> ();
		createFsm ();
		for (int i=0; i<fsm.length; i++) {
			String input = fsm [i][1];
			String tokens [] = getTokens (input);
			generatePaths (tokens, 0, "");
		}
		/*
		for (int i=0; i<allpaths.size (); i++) {
			String s = allpaths.elementAt (i);
			System.out.println ("path "+i+" "+s);
		}*/
	}

	void generatePaths (String tokens [], int stage, String sofar) {
		int n = tokens.length;
		int m = n - (stage+1);
		if (stage >= n) {
			allpaths.add (sofar);
			return;
		}
		String token = tokens [stage];
		// don't expand expressions
		if (isExpression (token)) {
			generatePaths (tokens, stage+1, sofar);
		}
		else if (isHead (token)) {
			QaNode node = lookup.get (token);
			String tail [] = node.tail;
			for (int i=0; i<tail.length; i++) {
				String toks [] = getTokens (tail [i]);
				if (tokens == null) continue;
				// System.out.println ("\t\ttail["+i+"] "+display (tokens));
				int l = toks.length;
				String newtok [] = new String [l+m];
				for (int a=0; a<l; a++) newtok [a] = toks [a];
				for (int b=0; b<m; b++) newtok [l+b] = tokens [stage+1+b];
				generatePaths (newtok, 0, sofar);
			}
		}
		else {
			String extended = sofar + " " + token;
			generatePaths (tokens, stage+1, extended);
		}
	}

	void createFsm () {
		Vector <String []> hold = new Vector <String []> ();
		Set <String> keys = lookup.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			if (key.startsWith ("[")) {
				QaNode node = lookup.get (key);
				String tail [] = node.tail;
				// check that size is right
				if (tail.length != nfsm) {
					// Debug ("State node "+key+" should have exactly four elements");
					continue;
				}
				hold.add (tail);
			}
		}
		int n = hold.size ();
		fsm = hold.toArray (new String [n][nfsm]);
	}

	String [] getTokens (String text) {
		// split then merge anything inside ( )
		Vector <String> hold = new Vector <String> ();
		StringTokenizer st = new StringTokenizer (text);
		while (st.hasMoreTokens ()) {
			hold.add (st.nextToken ());
		}
		// merge
		Vector <String> merged = new Vector <String> ();
		int n = hold.size ();
		outer: for (int i=0; i<n; i++) {
			String token = hold.elementAt (i);
			if (token.startsWith ("(")) {
				// find the token ending with )
				int j=i;
				for (; j<n; j++) {
					String test = hold.elementAt (j);
					if (test.endsWith (")")) break;
				}
				if (j == n) {
					// Debug (token+" +unclosed <>");
					return null; // will be bad anyway
				}
				// combine things from i to j
				StringBuffer sb = new StringBuffer ();
				for (int k=i; k<=j; k++) {
					sb.append (" " + hold.elementAt (k));
				}
				String combined = new String (sb).trim (); //get rid of leading space
				merged.add (combined);
				i = j;
				continue;
			}
			else {
				merged.add (token);
			}
		}
		int m = merged.size ();
		String values [] = merged.toArray (new String [m]);
		return values;
	}

	boolean isExpression (String token) {
		String inside = token.trim ();
		if (inside.startsWith ("(") && inside.endsWith (")")) return true;
		else return false;
	}

	boolean isHead (String token) {
		QaNode node = lookup.get (token);
		return (node != null);
	}

};


