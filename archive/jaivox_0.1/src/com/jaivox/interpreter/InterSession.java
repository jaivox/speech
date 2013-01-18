/**
 * An InterSession is a session between the InterServer and any other
 * agent. The session manages a particular conversation. It is created
 * by the server and uses an InterResponder to process requests that
 * are received.
 */

package com.jaivox.interpreter;

import com.jaivox.agent.*;

import java.io.*;
import java.net.*;
import java.util.Vector;

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
	
	void Debug (String s) {
		System.out.println ("[InterSession]" + s);
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
					Debug ("sent:" + outbuffer);
					outbuffer = null;
				}
				String line = readLineFromSocket ();
				if (line == null) continue;
				Debug ("read "+line);
				
				if (From == null || To == null) getFromTo (line);
				
				MessageData response = responder.respond (line);
				String result = response.getValue ("action");
				
				// check destination
				String to = response.getValue ("to");
				if (To == null) {
					Debug ("Connection has no destination: "+getSid ());
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
					Debug ("Invalid message: "+line);
					continue;
				}
				else if (result.equals (responseMessage)) {
					outbuffer = response.createMessage ();
					Debug ("replying: " + outbuffer);
				}
				else if (result.equals (finishedMessage)) {
					Debug ("Response received, no further action required");
					continue;
				}
				else {
					outbuffer = response.createMessage ();
					continue;
				}
			}
			// Debug ("Closing session "+sid);
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			// Debug (sid+" interrupted.");
		}
		catch (Exception e) {
			Debug (sid+":run "+e.toString ());
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
		// Debug ("Patched From: "+From+" To: "+To);
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
			// Debug ("looking for client to "+to);
			for (int i=0; i<n; i++) {
				Session ias = clients.elementAt (i);
				InterSession aps = (InterSession) ias;
				// Debug ("client "+i+" id is "+aps.sid + " to "+ aps.To);
				if (aps.To.equals (to)) return aps;
			}
			return null;
		}
		catch (Exception e) {
			Debug ("Could not find session to "+to);
			e.printStackTrace ();
			return null;
		}
	}

}
