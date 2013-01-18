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


public class Tags {
	TreeMap <String, String []> gtags;

	public Tags (String filename) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			gtags = new TreeMap <String, String []> ();
			String line;
			while ((line = in.readLine ()) != null) {
				StringTokenizer st = new StringTokenizer (line, "\t\r\n");
				if (st.countTokens () < 2) continue;
				String tag = st.nextToken ();
				st.nextToken ();
				if (st.hasMoreTokens ()) {
					String words = st.nextToken ();
					if (words.startsWith ("(")) continue;
					String terms [] = getterms (words);
					gtags.put (tag, terms);
				}
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	String [] getterms (String words) {
		StringTokenizer st = new StringTokenizer (words);
		int n = st.countTokens ();
		String terms [] = new String [n];
		for (int i=0; i<n; i++) {
			terms [i] = st.nextToken ();
		}
		return terms;
	}

};



