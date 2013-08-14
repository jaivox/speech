/*
   Jaivox version 0.5 August 2013
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

package com.jaivox.recognizer.sphinx;

import com.jaivox.agent.Server;
import com.jaivox.agent.Session;
import com.jaivox.agent.TestResponder;
import com.jaivox.util.Log;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes.Name;


/**
 * This server is wrapped by a SpeechInput class, so that recognized
 * strings can be sent through this agent's sessions.
 */

public class SphinxServer extends Server implements Runnable {
	int waitTime = 5000;

/**
 * Create a server listening at a designated port
@param port
 */
	public SphinxServer (int port) {
		super (port);
	}
	
/**
 * Create a server with a specific name that listens at a designated port
@param name
@param port
 */
	
	public SphinxServer (String name, int port) {
		super (name, port);
	}
	
/**
 * Run the agent's thread, making connections on requests to create
 * a speech session
 */
	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				int count = getIdCount ();
				String id = Name+"_"+getIdCount ();
				setIdCount (count+1);
				SphinxResponder r = new SphinxResponder ();
				SpeechSession ias = new SpeechSession (id, this, link, r);
				addSession (ias);
				Log.info ("Added client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			Log.severe (Name+e.toString ());
		}
	}

	public void execute (String command) {
		try {
			Vector <String> tokens = new Vector <String> ();
			StringTokenizer st = new StringTokenizer (command);
			while (st.hasMoreTokens ()) {
				String token = st.nextToken ();
				tokens.add (token);
			}
			int ntok = tokens.size ();
			if (command.startsWith ("connect")) {
				if (ntok != 3) {
					Log.warning ("Syntax: connect host port");
					return;
				}
				String host = tokens.elementAt (1);
				int port = Integer.parseInt (tokens.elementAt (2));
				Socket link = new Socket (host, port);
				int count = sessionCount ();
				String id = Name+"_"+count;
				SphinxResponder rtest = new SphinxResponder ();
				SpeechSession ias = new SpeechSession (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				Log.fine ("Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					Log.warning ("Sytax: disconnect sessionid");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Log.warning ("No session with id "+id);
					return;
				}
				// send a terminate message to the session
				Log.info ("disconnecting from "+id);
				String req = "{action: JviaTerminate, from: "+getId ()+", to: "+ias.getSid ();
				req += ", message: JviaTerminate}";
				ias.outbuffer = req;
				sleep (waitTime);
				ias.terminate ();
				// then terminate it
			}
			else if (command.startsWith ("send")) {
				// write it to the outstream of the session?
				if (ntok < 3) {
					Log.warning ("Sytax: send sessionid message");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Log.warning ("No session with id "+id);
					return;
				}
				StringBuffer sb = new StringBuffer ();
				sb.append (tokens.elementAt (2));
				for (int i=3; i<ntok; i++) {
					sb.append (' ');
					sb.append (tokens.elementAt (i));
				}
				String message = new String (sb);
				// String req = "{action: send, from: "+getId ()+", to: "+ias.getSid ();
				// req += ", message: "+message+"}";
				// send a terminate message to the session
				ias.outbuffer = message;
				Log.fine ("sending \""+message+"\" to "+id);
				if (message.equals (TestResponder.terminateRequest)) {
					sleep (waitTime);
					ias.terminate ();
				}
			}
			else if (command.equals ("terminate")) {
				// terminate all clients, then stop
				Vector <Session> clients = getClients ();
				for (int i=0; i<clients.size (); i++) {
					Session ias = clients.elementAt (i);
					String req = "{action: JviaTerminate, from: "+getId ()+", to: "+ias.getSid ();
					req += ", message: JviaTerminate}";
					ias.outbuffer = req;
					sleep (waitTime);
					Log.info ("Terminating "+ias.getSid ());
					ias.terminate ();
				}
				server.close ();
				interrupt ();
				return;
			}
			else {
				Log.warning ("illegal command: "+command);
			}
		}
		catch (Exception e) {
			Log.severe ("sphinxServer:execute "+e.toString ());
		}
	}

/**
 * Execute a request with a reply
@param command
@return
 */
	
	public String executeReply (String command) {
		try {
			Vector <String> tokens = new Vector <String> ();
			StringTokenizer st = new StringTokenizer (command);
			while (st.hasMoreTokens ()) {
				String token = st.nextToken ();
				tokens.add (token);
			}
			int ntok = tokens.size ();
			if (command.startsWith ("connect")) {
				if (ntok != 3) {
					Log.warning ("Syntax: connect host port");
					return "Error: invalid syntax";
				}
				String host = tokens.elementAt (1);
				int port = Integer.parseInt (tokens.elementAt (2));
				Socket link = new Socket (host, port);
				int count = sessionCount ();
				String id = Name+"_"+count;
				SphinxResponder rtest = new SphinxResponder ();
				SpeechSession ias = new SpeechSession (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				return ("OK: Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					return ("Error: Sytax: disconnect sessionid");
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					return ("Error: No session with id "+id);
				}
				// send a terminate message to the session
				Log.info ("disconnecting from "+id);
				String req = "{action: JviaTerminate, from: "+getId ()+", to: "+ias.getSid ();
				req += ", message: JviaTerminate}";
				ias.outbuffer = req;
				sleep (waitTime);
				ias.terminate ();
				// then terminate it
				sleep (waitTime);
				return ("OK: Terminate requested.");
			}
			else if (command.startsWith ("send")) {
				// write it to the outstream of the session?
				if (ntok < 3) {
					Log.warning ("Error: Syntax: send sessionid message");
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					return ("Error: No session with id "+id);
				}
				StringBuffer sb = new StringBuffer ();
				sb.append (tokens.elementAt (2));
				for (int i=3; i<ntok; i++) {
					sb.append (' ');
					sb.append (tokens.elementAt (i));
				}
				String message = new String (sb);
				// String req = "{action: send, from: "+getId ()+", to: "+ias.getSid ();
				// req += ", message: "+message+"}";
				// send a terminate message to the session
				ias.outbuffer = message;
				Log.fine ("sending \""+message+"\" to "+id);
				if (message.equals (TestResponder.terminateRequest)) {
					sleep (waitTime);
					ias.terminate ();
					sleep (waitTime);
					return ("OK: Terminate requested.");
				}
				else return ("OK: Message sent:"+message);
			}
			else if (command.equals ("terminate")) {
				// terminate all clients, then stop
				Vector <Session> clients = getClients ();
				for (int i=0; i<clients.size (); i++) {
					Session ias = clients.elementAt (i);
					String req = "{action: JviaTerminate, from: "+getId ()+", to: "+ias.getSid ();
					req += ", message: JviaTerminate}";
					ias.outbuffer = req;
					sleep (waitTime);
					Log.info ("Terminating "+ias.getSid ());
					ias.terminate ();
				}
				server.close ();
				interrupt ();
				return ("OK: terminated.");
			}
			else {
				return ("Error: illegal command: "+command);
			}
		}
		catch (Exception e) {
			Log.severe ("sphinxServer:executeReply "+e.toString ());
			return ("Error: "+e.toString ());
		}
	}

	public static void main (String args []) {
		String name = args [0];
		int port = Integer.parseInt (args [1]);
		new SphinxServer (name, port);
	}

}
