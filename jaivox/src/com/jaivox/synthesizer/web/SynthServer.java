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
package com.jaivox.synthesizer.web;

import com.jaivox.agent.Server;
import com.jaivox.agent.Session;
import com.jaivox.agent.TestResponder;
import com.jaivox.util.Log;
import java.net.Socket;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * SynthServer is an synthesizer server. In a typical application, this
 * server communicates with an interpreter and gets requests to speak.
 * This version depends on the availability of Google's TTS, see the
 * Synthesizer class.
 */

public class SynthServer extends Server implements Runnable {
	int waitTime = 5000;

	String basedir;
	Properties kv;
	Synthesizer Synth;


/**
 * The Synthesizer may need information from some files.
 * Here the server sets some variables that can be accessed by
 * the SynthResponder when creating an Synthesizer class
@param name
@param port
@param base
@param kv	Properties for the synthesizer
 */

	public SynthServer (String name, int port, String base, Properties pp) {
		super (name, port);
		basedir = base;
		kv = pp;
		Synth = new Synthesizer (kv);
	}


/**
 * Creates an SynthServer that listens on the designated port
@param port	socket listens on this port
 */
	public SynthServer (int port) {
		super (port);
	}

/**
 * Crates an SynthServer with a specific name, that listens at
 * the specified port
@param name	Name of the server (used in messages)
@param port socket listens on this port
 */

	public SynthServer (String name, int port) {
		super (name, port);
	}

	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				int count = getIdCount ();
				String id = Name+"_"+getIdCount ();
				setIdCount (count+1);
				SynthResponder r = new SynthResponder (basedir, kv, Synth);
				SynthSession ias = new SynthSession (id, this, link, r);
				addSession (ias);
				Log.info ("Added client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe (Name+" "+e.toString ());
		}
	}

/**
 * Execute agent-based commands to connect, disconnect and perform
 * other actions
@param command
 */

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
				SynthResponder rtest = new SynthResponder (basedir, kv, Synth);
				SynthSession ias = new SynthSession (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				Log.info ("Made connection client id "+id);
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
				ias.outbuffer = message;
				// Log.fine ("sending \""+message+"\" to "+id);
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
			Log.severe ("Synthserver: execute "+e.toString ());
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
				SynthResponder rtest = new SynthResponder ();
				SynthSession ias = new SynthSession (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				return ("OK: Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					return ("Error: Syntax: disconnect sessionid");
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
			Log.severe ("SynthServer:executeReply "+e.toString ());
			return ("Error: "+e.toString ());
		}
	}

/**
 * Returns a pointer to the Synthesizer used by this agent
 */
	public Synthesizer getSynthesizer () {
		return Synth;
	}

/**
 * Sets a particular synthesizer to be used by this agent.
@param synth
 */
	public void setSynthesizer (Synthesizer synth) {
		Synth = synth;
	}

}
