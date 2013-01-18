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
 * An Infonode is a piece of data. Infonodes are maintained by the
 * Info class. The Infonode uses the Adjective class to create comparative
 * and superlative forms of adjectives.
 */

public class Infonode {

	String datadir;
	String name;
	LinkedHashMap <String, String []> tagval;

/**
 * Creates an Infonode instance from some text information
@param dir	directory containing all data
@param info	A vector of Strings holding details of the data
 */
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

	String [] processFile (String line) {
		try {
			StringTokenizer st = new StringTokenizer (line, "[ \t\r\n]");
			if (st.countTokens () != 2) {
				Log.severe ("syntax of file spec is [comma_separated_file_name column_number]");
				return null;
			}
			String name = st.nextToken ();
			String filename = datadir + name;
			int col = Integer.parseInt (st.nextToken ());
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String data;
			Vector <String> hold = new Vector <String> ();
			while ((data = in.readLine ()) != null) {
				data = data.toLowerCase ();
				st = new StringTokenizer (data, ",\r\n");
				int n = st.countTokens ();
				if (n > 0 && n <= col) {
					Log.severe ("data from "+filename+" seems to have only "+n+" fields, <= "+col);
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
		String [] pos = tagval.get ("jj-p");
		if (pos == null) return;
		Log.finest ("Positive adjectives");
		int n = pos.length;
		String [] pcomp = new String [n];
		String [] psup = new String [n];
		for (int i=0; i<pos.length; i++) {
			String a = pos [i];
			String s = Adj.analyze (a);
			Log.finest (s);
			StringTokenizer st = new StringTokenizer (s, ",\r\n");
			String token = st.nextToken ();
			String comp = st.nextToken ().trim ();
			pcomp [i] = comp;
			String sup = st.nextToken ().trim ();
			psup [i] = sup;
		}
		tagval.put ("jjr-p", pcomp);
		tagval.put ("jjs-p", psup);
		String [] neg = tagval.get ("jj-n");
		if (neg == null) return;
		Log.finest ("Negative adjectives");
		n = neg.length;
		String [] ncomp = new String [n];
		String [] nsup = new String [n];
		for (int i=0; i<n; i++) {
			String a = neg [i];
			String s = Adj.analyze (a);
			Log.finest (s);
			StringTokenizer st = new StringTokenizer (s, ",\r\n");
			String token = st.nextToken ();
			String comp = st.nextToken ().trim ();
			ncomp [i] = comp;
			String sup = st.nextToken ().trim ();
			nsup [i] = sup;
		}
		tagval.put ("jjr-n", ncomp);
		tagval.put ("jjs-n", nsup);
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

