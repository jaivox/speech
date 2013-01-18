
/**
 * Sessions are created by agents in response to connection requests.
 * Sessions manage conversations between agents. Each session interprets
 * messages using a Responder.
 * 
 * This class is usually subclassed to produce Responders that have
 * specific functions.
 */

package com.jaivox.agent;

import java.io.*;
import java.net.*;

import com.jaivox.interpreter.InterServer;

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
			Debug ("Session " + e.toString ());
		}
	}
	
	public Session () {
		
	}

	void Debug (String message) {
		System.out.println ("[Session]"+sid+":"+message);
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
					Debug ("sent:" + outbuffer);
					outbuffer = null;
				}
				String line = readLineFromSocket ();
				if (line == null) continue;
				Debug ("read "+line);
				MessageData response = responder.respond (line);
				String result = response.getValue ("action");
				
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
					Debug ("Unhandled message:"+line);
					continue;
				}
			}
			Debug ("Closing session "+sid);
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			Debug (sid+" interrupted.");
		}
		catch (Exception e) {
			Debug (sid+":run "+e.toString ());
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
					/*
					String dstring = new String ("Bytes ");
					for (int i=0; i<msg.length(); i++) {
						int j = (int)(msg.charAt(i));
						dstring+= (" "+j);
					}
					Debug (dstring);
					*/
					return null;
				}
			}
			else
				return null;
		}
		catch (Exception e) {
			Debug ("readLineFromSocket: " + e.toString ());
			return null;
		}
	}

	public void terminate () {
		try {
			socket.close ();
			server.removeSession (this);
			Debug ("terminated");
		}
		catch (Exception e) {
			Debug (sid+":terminate "+e.toString ());
		}
	}

	public String getSid () {
		return sid;
	}

	public void setSid (String id) {
		this.sid = id;
	}

	public Server getServer () {
		return server;
	}

	public void setServer (Server server) {
		this.server = server;
	}

	public String getServerId () {
		InterServer server = (InterServer)getServer ();
		String serverId = server.getServerId ();
		return serverId;
	}

	public Socket getSocket () {
		return socket;
	}

	public void setSocket (Socket socket) {
		this.socket = socket;
	}

	public Responder getResponder () {
		return responder;
	}

	public void setResponder (Responder responder) {
		this.responder = responder;
	}

	public int getWaitTime () {
		return waitTime;
	}

	public void setWaitTime (int waitTime) {
		this.waitTime = waitTime;
	}

	public static int getDefaultWait () {
		return defaultWait;
	}


}

