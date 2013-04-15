/*
   Jaivox version 0.4 April 2013
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
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * Information about a specific field or attribute
 */

public class Infonode {

	String datadir;
	String name;
	LinkedHashMap <String, String []> tagval;

/**
 * Create an infonode from a set of lines that describe a field or
 * attribute.
@param dir
@param info
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

