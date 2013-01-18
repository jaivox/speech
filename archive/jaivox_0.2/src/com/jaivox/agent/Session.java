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

import java.io.*;
import java.net.*;

import com.jaivox.interpreter.InterServer;
import com.jaivox.util.Log;

/**
 * Sessions are created by agents in response to connection requests.
 * Sessions manage conversations between agents. Each session interprets
 * messages using a Responder.
 * 
 * This class is usually subclassed to produce Responders that have
 * specific functions.
 */

public class Session extends Thread implements Runnable {

	protected String sid;
	protected Server server;
	protected Socket socket;
	protected Responder responder;
	
	public static final int defaultWait = 100; // milliseconds
	public static final int maxWaits = 10;		// checking for In.ready
	protected int waitTime;
	protected BufferedReader in;
	protected PrintWriter out;
	public String outbuffer;

	public static String 
		terminateMessage = "JviaTerminate",
		invalidMessage = "JviaInvalid",
		responseMessage = "JviaResponse",
		finishedMessage = "JviaFinished";

/**
 * Create a session with the specific details
@param s		id of the session
@param serve	server that owns this session
@param sock		socket for communicating with this session
@param r		responder associated with this session
 */
	
	public Session (String s, Server serve, Socket sock, Responder r) {
		sid = s;
		server = serve;
		socket = sock;
		responder = r;
		r.owner = this;
		waitTime = defaultWait; // can modify if needed
		try {
			start ();
		}
		catch (Exception e) {
			Log.severe ("Session " + e.toString ());
		}
	}
	
	public Session () {
		
	}

/**
 * The session stays in its run method. It handles each request
 * that is received within this method and creates responses to
 * those requests. Some requests, such as those asking to terminate
 * the session are handled within the run () method.
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
					Log.info ("sent:" + outbuffer);
					outbuffer = null;
				}
				String line = readLineFromSocket ();
				if (line == null) continue;
				Log.fine ("read "+line);
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
					Log.info ("replying: " + outbuffer);
				}
				else if (result.equals (finishedMessage)) {
					Log.info ("Response received, no further action required");
					continue;
				}
				else {
					Log.warning ("Unhandled message:"+line);
					continue;
				}
			}
			Log.info ("Closing session "+sid);
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			Log.info (sid+" interrupted.");
		}
		catch (Exception e) {
			Log.severe (sid+":run "+e.toString ());
			e.printStackTrace ();
		}
	}
	
	protected String readLineFromSocket () {
		// wait for a while
		try {
			for (int i=0; i < maxWaits && !(in.ready ()); i++)
				sleep (waitTime);
			if (in.ready ()) {
				StringBuffer sb = new StringBuffer ();
				while(true) {
					if (in.ready ()) {
						char ch = (char)in.read ();
						if(ch == '\n')
							break;
						sb.append (ch);
					}
					else break;
				}
				String msg = new String (sb);

				msg = msg.trim ();
				if (msg.length() > 1)
					return msg;
				else {
					return null;
				}
			}
			else
				return null;
		}
		catch (Exception e) {
			Log.fine ("readLineFromSocket: " + e.toString ());
			return null;
		}
	}

/**
 * You can call this function to end a session, but not kill the
 * agent.
 */
	public void terminate () {
		try {
			socket.close ();
			server.removeSession (this);
			Log.info ("terminated");
		}
		catch (Exception e) {
			Log.severe (sid+":terminate "+e.toString ());
		}
	}

/**
 * The sid is the string id of the session. You need to refer to this
 * to send a message (since there can be many sessions managed by a
 * single agent, just like there are many connections to an http server. 
@return
 */
	public String getSid () {
		return sid;
	}

/**
 * Use this function to set the string id of the session to something
 * you like.
@param id
 */
	public void setSid (String id) {
		this.sid = id;
	}
	
/**
 * Get a link to the server that owns this session.
@return
 */

	public Server getServer () {
		return server;
	}
	
/**
 * Set the server to be a specific one
@param s
 */
	public void setServer (Server s) {
		server = s;
	}
	
/**
 * Get the id of the agent that owns this session.
@return
 */
	public String getServerId () {
		InterServer server = (InterServer)getServer ();
		String serverId = server.getServerId ();
		return serverId;
	}

/**
 * Get the socket used by this session
@return
 */
	public Socket getSocket () {
		return socket;
	}
	
/**
 * Set the socket to a particular one. This is used for example by
 * an HtpSession
@param socket
 */
	public void setSocket (Socket sock) {
		socket = sock;
	}
/**
 * Get the responder attached to this session.
@return
 */

	public Responder getResponder () {
		return responder;
	}

/**
 * Set the responder of this session to be a particular previously
 * created one. This amounts to changing the behavior of the session.
@param responder
 */
	public void setResponder (Responder responder) {
		this.responder = responder;
	}

/**
 * The wait time is the time that the session waits while looking
 * for messages. Increasing this will make the session respond slower.
 * See the run () method where this is used.
@return
 */
	public int getWaitTime () {
		return waitTime;
	}
	
/**
 * Normally the wait time is defaultWait, but this can be changed by
 * calling the setWaitTime function. The time is in milliseconds.
@param waitTime
 */

	public void setWaitTime (int waitTime) {
		this.waitTime = waitTime;
	}

/**
 * Get the default wait time, wait time is normally set to this value
 * unless it is changed with setWaitTime.
@return
 */
	public static int getDefaultWait () {
		return defaultWait;
	}


}

