/**
 * MessageData parses messages between agents. The messages are in a
 * simplified Json format.
 * For example, a message may be like
 * {action: respond, from: sphinx, to: coordinator, session: 1234, 
 * passcode: xyz, message: \"recognized words\"}
 */

package com.jaivox.agent;

import java.awt.Point;
import java.util.*;

// can test on a message like
// 	{action: respond, from: sphinx, to: coordinator, session: 1234, 
// 	passcode: xyz, message: \"recognized words\"}

public class MessageData {
	
	static String markers = " \t\r\n~`!@#$%^&*()+-=|\\;\"\'<>,.?/";
	static String standardFields = "action from to session message";

	Hashtable <String, String> kv;
	boolean Valid = false;
	
	public MessageData () {
		kv = new Hashtable <String, String> ();
	}

/**
 * Creates the key-value pairs from a message in a simple
 * Json-like format. Note we allow _ in keys. Also, : is not a marker.
 *  This is due to the fact that : is used to indicate keys. There 
 *  is a way to allow : tobe a separator of tokens and still recognize
 *   keys, but that makes the parsing a bit too messy.
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
			// Debug ("msg: "+msg);
			// msg should not contain {}[]
			if (msg.indexOf ("{") != -1 || msg.indexOf ("[") != -1 ||
					msg.indexOf ("}") != -1 || msg.indexOf ("]") != -1) {
				Debug ("Message: "+message+" contains illegal embedded {}{}");
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
					// Debug ("token "+token+" from "+tstart+" to "+tend);
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
			Debug (e.toString ());
			e.printStackTrace ();
		}
	}

	void Debug (String s) {
		System.out.println ("[MessageData]" + s);
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

	public String getValue (String key) {
		return kv.get (key);
	}

	public void setValue (String key, String value) {
		kv.put (key, value);
	}

	public boolean isValid () {
		return Valid;
	}
}
