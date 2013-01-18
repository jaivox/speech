
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



