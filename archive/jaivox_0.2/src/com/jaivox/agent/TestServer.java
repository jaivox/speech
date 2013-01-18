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

package com.jaivox.agent;

import java.net.*;
import java.util.*;

import com.jaivox.protocol.FileServer;
import com.jaivox.util.Log;
/**
 * A TestServer is a simple Server (works with TestResponder.
 * it is used to set and and test server connections and a few
 * simple commands. A command line program SimpleTest creates 
 * a TestSever and sends the commands entered to the execute method
 * in TestServer.
 */

public class TestServer extends Server implements Runnable {
	
	int waitTime = 5000;
	
/**
 * The test server contains a file server that can be used to
 * transfer files. It has to be explicitly created with a file server
 * request, when the TestSever is created, the fileServer is null.
 */
	FileServer FS = null;

/**
 * Create a test server that listens at the designated port.
@param port
 */
	
	public TestServer (int port) {
		super (port);
	}
	
/**
 * Create a test server with a specific name that listens at a specific port.
@param name
@param port
 */
	
	public TestServer (String name, int port) {
		super (name, port);
	}
	
/**
 * Run, processing incoming messages and outgoing requests.	
 */
	
	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				String id = Name+"_"+getIdCount ();
				TestResponder r = new TestResponder ();
				Session ias = new Session (id, this, link, r);
				addSession (ias);
				Log.info ("Added client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			Log.severe (Name+e.toString ());
		}
	}

/**
 * execute a command, usually entered at the command line in SimpleTest.
 * This method handles the following commands
 * connect host port	where the host is a name and port is a number
 * disconnect sessionid	where session id is a string name of a session
 * send sessionid message	where session id and message are strings
 * terminate	to terminate the server
 * to terminate a session, first you should send a message to the remote
 * session to terminate, then terminate the session connected to that
 * remote point.
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
					Log.severe ("Syntax: connect host port");
					return;
				}
				String host = tokens.elementAt (1);
				int port = Integer.parseInt (tokens.elementAt (2));
				Socket link = new Socket (host, port);
				int count = sessionCount ();
				String id = Name+count;
				Responder rtest = new TestResponder ();
				Session ias = new Session (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				Log.info ("Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					Log.info ("Sytax: disconnect sessionid");
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
			Log.severe ("TestServer:execute " + e.toString ());
		}
	}
}
