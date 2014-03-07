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

package com.jaivox.tools;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A QaNode holds a single node of the grammar (i.e. dialog.)
 * It can also hold a finite state machine transition, which is
 * also part of the dialog.
 * @author jaivox
 */

public class QaNode {

	String head;
	String tail [];

	public int ntail;
	double dtail;
	QaList parent;

	public boolean Valid = false;
	
/**
 * Create a QaNode out of information within a { }.
 * If there is some problem, the QaNode will not be valid.
 * Validity can be tested with isValid ()
 * @param p
 * @param production
 */

	public QaNode (QaList p, String production) {
		parent = p;
		StringTokenizer st =
			new StringTokenizer (production, "{}\r\n");
		if (!st.hasMoreTokens ()) return; 		// invalid
		// first token is head
		head = st.nextToken ();
		if (!st.hasMoreTokens ()) {
			tail = null; 	// terminal node
			ntail = 0;
			dtail = 0.0;
		}
		else {
			Vector <String> hold = new Vector <String> ();
			StringBuffer sb = new StringBuffer ();
			while (st.hasMoreTokens ()) {
				String line = st.nextToken ();
				String rest = line.trim ();
				sb.append (rest);
				if (rest.endsWith (";")) {
					String s = new String (sb);
					int n = s.length ();
					String t = s.substring (0, n-1).trim ();
					hold.add (t);
					sb = new StringBuffer ();
				}
			}
			String last = new String (sb).trim ();
			if (last.length () > 0) hold.add (last);
			ntail = hold.size ();
			tail = hold.toArray (new String [ntail]);
			dtail = (double)ntail+0.5;
		}
		Valid = true;
	}

/**
 * Is this a valid QaNode. False if there was some problem parsing the
 * text within { }
 * @return
 */
	public boolean isValid () {
		return Valid;
	}

/**
 * Get the head i.e. the tag, of a QaNode
 * @return
 */
	
	public String getHead () {
		return head;
	}

/**
 * Get the rest of the QaNode, as an array of strings
 * @return
 */
	public String[] getTail () {
		return tail;
	}
	
/**
 * Pick a random element from the tail. This is useful
 * when generating responses introducing some variation in
 * the system responses.
 * @return
 */
	
	public String pickRandomTail () {
		if (ntail == 0) return "";
		int i = ntail;
		while (i >= ntail) {
			i = (int)(Math.random () * dtail);
		}
		return tail [i];
	}

}


