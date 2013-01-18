
package com.jaivox.tools;

import java.io.*;
import java.util.*;
import com.jaivox.interpreter.Adjective;

public class Infonode {

	String datadir;
	String name;
	LinkedHashMap <String, String []> tagval;

	public Infonode (String dir, Vector <String> info) {
		datadir = dir;
		boolean started = false;
		tagval = new LinkedHashMap <String, String []> ();
		for (int i=0; i<info.size (); i++) {
			String line = info.elementAt (i).trim ();
			if (line.equals ("{") || line.equals ("}")) continue;
			if (line.length () == 0) continue;
			if (!started) {
				name = line.trim ();
				started = true;
			}
			else {
				int pos = line.indexOf (":");
				if (pos == -1) continue;
				String tag = line.substring (0, pos).trim ();
				String val = line.substring (pos+1).trim ();
				if (val.startsWith ("[") && val.endsWith ("]")) {
					String vals [] = processFile (val);
					if (vals == null) continue;
					tagval.put (tag, vals);
				}
				else {
					StringTokenizer st = new StringTokenizer (val, ",");
					int n = st.countTokens ();
					String vals [] = new String [n];
					for (int j=0; j<n; j++) {
						vals [j] = st.nextToken ().trim ();
					}
					tagval.put (tag, vals);
				}
			}
		}
	}

	void Debug (String s) {
		System.out.println ("[Infonode]"+s);
	}

	String [] processFile (String line) {
		try {
			StringTokenizer st = new StringTokenizer (line, "[ \t\r\n]");
			if (st.countTokens () != 2) {
				Debug ("syntax of file spec is [comma_separated_file_name column_number]");
				return null;
			}
			String name = st.nextToken ();
			String filename = datadir + name;
			int col = Integer.parseInt (st.nextToken ());
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String data;
			Vector <String> hold = new Vector <String> ();
			while ((data = in.readLine ()) != null) {
				st = new StringTokenizer (data, ",\r\n");
				int n = st.countTokens ();
				if (n > 0 && n <= col) {
					Debug ("data from "+filename+" seems to have only "+n+" fields, <= "+col);
					break;
				}
				for (int i=0; i<n; i++) {
					String token = st.nextToken ();
					if (i == col) {
						hold.add (token.trim ());
						break;
					}
				}
			}
			in.close ();
			int m = hold.size ();
			String vals [] = new String [m];
			for (int i=0; i<m; i++) {
				vals [i] = hold.elementAt (i);
			}
			return vals;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}

	void buildAdjectives (Adjective Adj) {
		String [] pos = tagval.get ("JJ-P");
		if (pos == null) return;
		System.out.println ("-------------------------------");
		System.out.println ("Positive adjectives");
		int n = pos.length;
		String [] pcomp = new String [n];
		String [] psup = new String [n];
		for (int i=0; i<pos.length; i++) {
			String a = pos [i];
			String s = Adj.analyze (a);
			System.out.println (s);
			StringTokenizer st = new StringTokenizer (s, ",\r\n");
			String token = st.nextToken ();
			String comp = st.nextToken ().trim ();
			pcomp [i] = comp;
			String sup = st.nextToken ().trim ();
			psup [i] = sup;
		}
		tagval.put ("JJR-P", pcomp);
		tagval.put ("JJS-P", psup);
		String [] neg = tagval.get ("JJ-N");
		if (neg == null) return;
		System.out.println ("Negative adjectives");
		n = neg.length;
		String [] ncomp = new String [n];
		String [] nsup = new String [n];
		for (int i=0; i<n; i++) {
			String a = neg [i];
			String s = Adj.analyze (a);
			System.out.println (s);
			StringTokenizer st = new StringTokenizer (s, ",\r\n");
			String token = st.nextToken ();
			String comp = st.nextToken ().trim ();
			ncomp [i] = comp;
			String sup = st.nextToken ().trim ();
			nsup [i] = sup;
		}
		tagval.put ("JJR-N", ncomp);
		tagval.put ("JJS-N", nsup);
		System.out.println ("-------------------------------");
	}


	String tagvals (String tag) {
		String [] vals = tagval.get (tag);
		if (vals == null) return null;
		else {
			StringBuffer sb = new StringBuffer ();
			for (int j=0; j<vals.length; j++) {
				sb.append (" "+vals [j]);
			}
			String result = new String (sb).trim ();
			return result;
		}
	}

	String [] tagvalarray (String tag) {
		String [] vals = tagval.get (tag);
		return vals;
	}


};

