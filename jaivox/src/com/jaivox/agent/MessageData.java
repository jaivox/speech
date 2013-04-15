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

package com.jaivox.agent;

import com.jaivox.util.Log;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * MessageData parses messages between agents. The messages are in a
 * simplified Json format.
 * For example, a message may be like
 * {action: respond, from: sphinx, to: coordinator, session: 1234, 
 * passcode: xyz, message: \"recognized words\"}
 */


public class MessageData {
	
	static String markers = " \t\r\n~`!@#$%^&*()+-=|\\;\"\'<>,.?/";
	static String standardFields = "action from to session message";

/**
 * The key value pairs for the message, for example a message like
 * {action: respond, etc. has a key "action" with value "respond".
 */
	Hashtable <String, String> kv;
	boolean Valid = false;

/**
 * Create an empty MessageData. You can fill the keys and values in a
 * hashtable kv that can be later used to generate a string message.	
 */
	public MessageData () {
		kv = new Hashtable <String, String> ();
	}

/**
 * Creates the key-value pairs from a message in a simple
 * Json-like format. Note we allow _ in keys. Also, : is not a marker.
 * This is due to the fact that : is used to indicate keys. There 
 * is a way to allow : to be a separator of tokens and still recognize
 * keys, but that makes the parsing a bit too messy.
@param message string in a simple Json format
 */
	public MessageData (String message) {
		try {
			kv = new Hashtable <String, String> ();
			int start = message.indexOf ("{");
			int end = message.lastIndexOf ("}");
			// start = -1 is no problem
			if (end == -1) end = message.length ();
			String msg = fixColon (message, start+1, end);
			// Log.info ("MessageData:"+msg);
			// msg should not contain {}[]
			if (msg.indexOf ("{") != -1 || msg.indexOf ("[") != -1 ||
					msg.indexOf ("}") != -1 || msg.indexOf ("]") != -1) {
				Log.info ("MessageData:"+message+" contains illegal embedded {}{}");
				return;
			}
			StringTokenizer st = new StringTokenizer (msg, markers);
			Vector <Point> tokens = new Vector <Point> ();
			int last = 0;
			while (st.hasMoreTokens ()) {
				String token = st.nextToken ();
				if (token.endsWith (":")) {
					// find where it starts and ends
					int tstart = msg.indexOf (token, last);
					int tend = tstart + token.length ();
					Point p = new Point (tstart, tend);
					tokens.add (p);
					// Log.fine ("token "+token+" from "+tstart+" to "+tend);
					last = tend+1;
				}
			}
			int n = tokens.size ();
			int m = msg.length ();
			for (int i=0; i<n; i++) {
				Point p = tokens.elementAt (i);
				int tstart = p.x;
				int tend = p.y;
				int next = m;
				if (i < n-1) {
					Point q = tokens.elementAt (i+1);
					next = q.x;
				}
				String key = msg.substring (tstart, tend-1);
				String val = msg.substring (tend, next).trim ();
				// exactly what to trim? we will remove a comma since
				// that is usually added to values
				if (val.endsWith (",")) val = val.substring (0, val.length () -1);
				kv.put (key, val);
			}
			Valid = true;
		}
		catch (Exception e) {
			Log.severe (e.toString ());
			e.printStackTrace ();
		}
	}

	String fixColon (String line, int start, int end) {
		String part = line.substring (start, end);
		StringBuffer sb = new StringBuffer ();
		int n = part.length ();
		for (int i=0; i<n; i++) {
			char c = part.charAt (i);
			sb.append (c);
			if (c == ':') {
				if (i < n-1) {
					char d = part.charAt (i+1);
					if (!Character.isWhitespace (d)) {
						sb.append (' ');
						continue;
					}
				}
			}
		}
		String changed = new String (sb);
		return changed;
	}
	
/**
 * Creates a MessageData object from the given values. The message key
 * will be associated with "No Message", this can be changed later.
@param action
@param from
@param to
@param session
 */

	public void createKeyValues (String action, String from, String to, String session) {
		setValue ("action", action);
		setValue ("from", from);
		setValue ("to", to);
		setValue ("session", session);
		setValue ("message", "\"No Message\"");
	}
	
/**
 * Creates a MessageData object from the given values
@param action
@param from
@param to
@param session
@param msg
 */
	public void createKeyValuesAndMsg (String action, String from, String to, 
			String session, String msg) {
		setValue ("action", action);
		setValue ("from", from);
		setValue ("to", to);
		setValue ("session", session);
		setValue ("message", msg);
	}
	
/**
 * Creates a string based on information in this MessageData. The
 * resulting string can be sent via socket connections.
@return message in the form of a string
 */
	public String createMessage () {
		StringBuffer sb = new StringBuffer ();
		// fill some standard values
		sb.append ("{");
		String act = getValue ("action");
		if (act != null) {
			sb.append ("action: ");
			sb.append (act);
			sb.append (", ");
		}
		String from = getValue ("from");
		if (from != null) {
			sb.append ("from: ");
			sb.append (from);
			sb.append (", ");
		}
		String to = getValue ("to");
		if (to != null) {
			sb.append ("to: ");
			sb.append (to);
			sb.append (", ");
		}
		String session = getValue ("session");
		if (session != null) {
			sb.append ("session: ");
			sb.append (session);
			sb.append (", ");
		}
		// add any other keyvalue pairs
		Set <String> keys = kv.keySet ();
		for (Iterator <String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			String val = kv.get (key);
			if (standardFields.indexOf (key) != -1) continue;
			sb.append (key);
			sb.append (": ");
			sb.append (val);
			sb.append (", ");
		}
		String message = getValue ("message");
		if (message != null) {
			sb.append ("message: ");
			sb.append (message);
		}
		sb.append (" }");
		
		String all = new String (sb);
		return all;
	}

	void showKeyValues () {
		Set <String> keys = kv.keySet ();
		for (Iterator <String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			String val = kv.get (key);
			System.out.println (key+" = "+val);
		}
	}

/**
 * Get the value for a key in the message. For example, if you want to know
 * the name of the agent that sent the message, you can get the value of
 * the key "from".
@param key
@return
 */
	public String getValue (String key) {
		return kv.get (key);
	}
	
/**
 * Set the value for a key. This can be used for example to set the message
 * text in a MessageData object that has been created already.
@param key
@param value
 */

	public void setValue (String key, String value) {
		kv.put (key, value);
	}

/**
 * Check whether the message is valid. This is a way to handle messages
 * that may contain parse errors.
@return
 */
	public boolean isValid () {
		return Valid;
	}
}
