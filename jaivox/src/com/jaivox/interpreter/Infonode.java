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

package com.jaivox.interpreter;

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An Infonode is a piece of data. Infonodes are maintained by the
 * Info class.
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

/**
 * get the values associated with a tag as a comma separated string.
 * In addition to the comma separator, there is a space between
 * succeeding values.
 * @param tag
 * @return
 */
	
	public String tagvals (String tag) {
		String [] vals = tagval.get (tag);
		if (vals == null) return null;
		else {
			StringBuffer sb = new StringBuffer ();
			for (int j=0; j<vals.length; j++) {
				if (j > 0) sb.append (", ");
				sb.append (vals [j]);
			}
			String result = new String (sb).trim ();
			return result;
		}
	}

/**
 * Gt the values associated with a  tag as an array of strings.
 * @param tag
 * @return
 */
	
	public String [] tagvalarray (String tag) {
		String [] vals = tagval.get (tag);
		return vals;
	}

/**
 * Show the details of information within this Infonode. The information
 * is printed to the screen.
 */
	
	public void showdetails () {
		System.out.println (name);
		Set <String> keys = tagval.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext ();) {
			String key = it.next ();
			String vals [] = tagvalarray (key);
			System.out.print (key+" : ");
			for (int i=0; i<vals.length; i++) {
				System.out.print (vals [i]+" ");
			}
			System.out.println ();
		}
	}

};

