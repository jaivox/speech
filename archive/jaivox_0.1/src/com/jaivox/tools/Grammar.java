
package com.jaivox.tools;

import java.io.*;
import java.util.*;

public class Grammar {

	Vector <String []> patterns;
	Vector <String> patorig;
	Hashtable <String, String> specs;

	public Grammar (String filename) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			patterns = new Vector <String []> ();
			patorig = new Vector <String> ();
			specs = new Hashtable <String, String> ();
			String line;
			while ((line = in.readLine ()) != null) {
				StringTokenizer st = new StringTokenizer (line, "\t\r\n");
				if (st.countTokens () < 2) continue;
				String pattern = st.nextToken ();
				patorig.add (pattern);
				String seq [] = gettags (pattern);
				patterns.add (seq);
				String spec = st.nextToken ().trim ();
				specs.put (pattern, spec);
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	String [] gettags (String words) {
		StringTokenizer st = new StringTokenizer (words);
		int n = st.countTokens ();
		String seq [] = new String [n];
		for (int i=0; i<n; i++) {
			seq [i] = st.nextToken ();
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


