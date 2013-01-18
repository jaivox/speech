/*
   Jaivox version 0.3 December 2012
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
 * Questions are generated according to a set of grammatic templates.
 * For example, the template "NN VB NN" can be the grammar of "dog eat dog".
 */
public class Grammar {

	Vector <String []> patterns;
	Vector <String> patorig;
	Hashtable <String, String> specs;
	static final String terms = " \t\r\n~`!@#$%^&*()+={}[]|\\:;<>,.?/\"\'";
	static final String tagchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ-";

/**
 * Create a grammar from a file. The file contains various grammar templates.
 * For example, see files named grammar.txt.
@param filename
 */
	public Grammar (String filename) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			patterns = new Vector <String []> ();
			patorig = new Vector <String> ();
			specs = new Hashtable <String, String> ();
			String line;
			while ((line = in.readLine ()) != null) {
				StringTokenizer st = new StringTokenizer (line, "\t\r\n");
				if (st.countTokens () < 1) continue;
				String pattern = st.nextToken ().trim ();
				patorig.add (pattern);
				String seq [] = gettags (pattern);
				patterns.add (seq);
				if (st.hasMoreTokens ()) {
					String spec = st.nextToken ().trim ();
					specs.put (pattern, spec);
				}
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
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

	String getspec (String pattern) {
		String val = specs.get (pattern);
		if (val == null) return null;
		if (val.startsWith ("(") && val.endsWith (")")) return val;
		else return null;
	}
};


