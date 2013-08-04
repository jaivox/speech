
package com.jaivox.recognizer.web;

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
 * Manages a session with a web recognizer server.
 * The web recognizer (August 2013) is by Google.
 */

public class SpeechSession extends Session implements Runnable {

	public String From;
	public String To;
	
	
/**
 * Creates a session for a server.
@param s	id of the session, useful for debugging
@param serve	server that owns this session
@param sock	socket for communications
@param r	responder that handles messages from othe ragnets
 */
	public SpeechSession (String s, Server serve, Socket sock, Responder r) {
		super (s, serve, sock, r);
		From = null;
		To = null;
	}
	
	public SpeechSession () {
		super ();
		From = null;
		To = null;
	}

/**
 * Keep the session alive, handling requests
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
				
				if (result.equals (terminateMessage)) {
					break;
				}
				else if (result.equals (invalidMessage)) {
					Log.warning ("Invalid message: "+line);
					continue;
				}
				else if (result.equals (responseMessage)) {
					outbuffer = response.createMessage ();
					Log.fine ("replying: " + outbuffer);
				}
				else if (result.equals (finishedMessage)) {
					Log.fine ("Response received, no further action required");
					continue;
				}
				else {
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
					else {
						outbuffer = response.createMessage ();
						continue;
					}
				}
			}
			Log.fine ("Closing session "+sid);
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			Log.fine (sid+" interrupted.");
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
 * For a given agent, see if there is already a SpeechSession with
 * the agent and return it. Return null if there is no such session.
@param to	id of agent to connect to
@return	a session with the agent if it exists, null otherwise
 */
	public SpeechSession appSessionTo (String to) {
		Vector <Session> clients = server.getClients ();
		int n = clients.size ();
		// Log.fine ("looking for client to "+to);
		for (int i=0; i<n; i++) {
			Session ias = clients.elementAt (i);
			String classname = ias.getClass().getName();
			// Log.fine ("Checking client "+ias.getSid ()+" classname "+classname);
			if (classname.equals ("SpeechSession")) {
				SpeechSession aps = (SpeechSession) ias;
				// Log.fine ("client "+i+" id is "+aps.sid + " to "+ aps.To);
				if (aps.To.equals (to)) return aps;
			}
		}
		return null;
	}


}
