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

import com.jaivox.agent.MessageData;
import com.jaivox.agent.Responder;
import com.jaivox.agent.Server;
import com.jaivox.agent.Session;
import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;


/**
 * An InterSession is a session between the InterServer and any other
 * agent. The session manages a particular conversation. It is created
 * by the server and uses an InterResponder to process requests that
 * are received.
 */

public class InterSession extends Session implements Runnable {

	public String From;
	public String To;
	
/**
 * Creates a session with the designated parameters
@param s	id of this session (useful in debugging)
@param serve	server that creates this session
@param sock	socket for communications
@param r	responder associated with this session
 */
	
	public InterSession (String s, Server serve, Socket sock, Responder r) {
		super (s, serve, sock, r);
		From = null;
		To = null;
	}
	
	public InterSession () {
		super ();
		From = null;
		To = null;
	}
	
/**
 * Runs the session, handling each request. Most of the requests
 * are routed to the InterResponder.
 */
	public void run () {
		try {
			in = new BufferedReader (new InputStreamReader (
					socket.getInputStream ()));
			out = new PrintWriter (socket.getOutputStream ());

			while (true) {
				if (outbuffer != null) {
					out.println (outbuffer);
					out.flush ();
					sleep (waitTime);
					Log.fine ("sent:" + outbuffer);
					outbuffer = null;
				}
				String line = readLineFromSocket ();
				if (line == null) continue;
				Log.fine ("read "+line);
				
				if (From == null || To == null) getFromTo (line);
				
				MessageData response = responder.respond (line);
				String result = response.getValue ("action");
				
				// check destination
				String to = response.getValue ("to");
				if (To == null) {
					Log.warning ("Connection has no destination: "+getSid ());
					continue;
				}
				if (!to.equals (To)) {
					Session toSession = appSessionTo (to);
					if (toSession != null) {
						toSession.outbuffer = response.createMessage ();
						continue;
					}
				}
				
				if (result.equals (terminateMessage)) {
					break;
				}
				else if (result.equals (invalidMessage)) {
					Log.warning ("Invalid message: "+line);
					continue;
				}
				else if (result.equals (responseMessage)) {
					outbuffer = response.createMessage ();
					Log.info ("replying: " + outbuffer);
				}
				else if (result.equals (finishedMessage)) {
					Log.fine ("Response received, no further action required");
					continue;
				}
				else {
					outbuffer = response.createMessage ();
					continue;
				}
			}
			// Log.fine ("Closing session "+sid);
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			// Log.fine (sid+" interrupted.");
		}
		catch (Exception e) {
			Log.severe (sid+":run "+e.toString ());
			e.printStackTrace ();
		}
	}
	
	void getFromTo (String message) {
		MessageData jd = new MessageData (message);
		From = jd.getValue ("to");
		int pos = From.indexOf ("_");
		if (pos != -1) From = From.substring (0, pos);
		To = jd.getValue ("from");
		pos = To.indexOf ("_");
		if (pos != -1) To = To.substring (0, pos);
		// Log.fine ("Patched From: "+From+" To: "+To);
	}
	
/**
 * Finds the session to a designated agent. The agent is identified
 * by its name. This function is useful to see if there is a connection
 * to another agent and to reuse that connection if it exists
@param to	name of the agent
@return		returns the session (or null)
 */
	
	public InterSession appSessionTo (String to) {
		try {
			Vector <Session> clients = server.getClients ();
			int n = clients.size ();
			// Log.fine ("looking for client to "+to);
			for (int i=0; i<n; i++) {
				Session ias = clients.elementAt (i);
				InterSession aps = (InterSession) ias;
				// Log.fine ("client "+i+" id is "+aps.sid + " to "+ aps.To);
				if (aps.To.equals (to)) return aps;
			}
			return null;
		}
		catch (Exception e) {
			Log.severe ("Could not find session to "+to);
			e.printStackTrace ();
			return null;
		}
	}

}
