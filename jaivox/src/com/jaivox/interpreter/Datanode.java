/*
   Jaivox version 0.7 March 2014
   Copyright 2010-2014 by Bits and Pixels, Inc.

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
import java.util.StringTokenizer;
import java.util.Vector;


public class Datanode {
	
	Info parent;
	boolean Valid;
	String data [][];
	String columns [];
	int nrow, ncol;

/**
 * Create a data node from information from specifications in an
 * Infonode, from a specific filename. The filename may be in a directory
 * "datadir" stored in the parent Info.
 * @param mom
 * @param node
 * @param filename
 */
	public Datanode (Info mom, Infonode node, String filename) {
		parent = mom;
		Valid = loadFile (node, filename);
	}
	
/**
 * Loads a specific data file if it exists, as detected by loadFile.
 * 
 * @param node
 * @param filename
 * @return
 */

	boolean loadFile (Infonode node, String filename) {
		try {
			// get ncol, columns
			// Log.fine ("Loading data for "+node.name+" from "+filename);
			columns = node.tagvalarray ("columns");
			ncol = columns.length;
			for (int i=0; i<ncol; i++) {
				String col = columns [i];
				Infonode sub = parent.specs.get (col);
				if (sub == null) {
					Log.severe ("No information for column "+col);
					return false;
				}
			}

			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			Vector <String []> hold = new Vector <String []> ();
			while ((line = in.readLine ()) != null) {
				if (line.trim ().length () == 0) continue;
				line = line.toLowerCase ();
				StringTokenizer st = new StringTokenizer (line, ",\r\n");
				if (st.countTokens () != ncol) {
					Log.severe ("Expected "+ncol+" columns in "+line);
					return false;
				}
				String words [] = new String [ncol];
				for (int i=0; i<ncol; i++) {
					words [i] = st.nextToken ().trim ();
				}
				hold.add (words);
			}
			in.close ();

			nrow = hold.size ();
			data = new String [nrow][ncol];
			for (int i=0; i<nrow; i++) {
				String words [] = hold.elementAt (i);
				for (int j=0; j<ncol; j++) {
					data [i][j] = words [j];
				}
			}
			return true;
		}
		catch (Exception e) {
			Log.severe ("Info:loadFile "+e.toString ());
			e.printStackTrace ();
			return false;
		}
	}
	
/**
 * Show the data stored in the data array array by printing it out
 * to the screen.
 */
	
	public void showData () {
		StringBuffer sb = new StringBuffer ();
		sb.append ("ncol = "+ncol+"\n");
		for (int i=0; i<ncol; i++) {
			sb.append (columns [i]);
			sb.append ("\n");
		}

		sb.append ("nrow = "+nrow+"\n");
		for (int i=0; i<nrow; i++) {
			for (int j=0; j<ncol; j++) {
				sb.append (data [i][j]);
				if (j < ncol-1) sb.append ("\t");
				else sb.append ("\n");
			}
		}
		String all = new String (sb);
		System.out.println (all);
	}
	
/**
 * Did we manage to obtain an array of data correctly?
 * @return
 */
	
	public boolean isValid () {
		return Valid;
	}

/**
 * Get the data within this data node as an array of strings
 * @return
 */
	
	public String [][] getData () {
		return data;
	}

/**
 * Get the names of columns in the data array.
 * @return
 */
	public String [] getColumnNames () {
		return columns;
	}

/**
 * Get the number of columns in the data array
 * @return
 */
	
	public int getNcol () {
		return ncol;
	}

/**
 * Get the number of data rows
 * @return
 */
	public int getNrow () {
		return nrow;
	}

}
