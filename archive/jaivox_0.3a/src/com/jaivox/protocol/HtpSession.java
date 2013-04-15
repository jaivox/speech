
package com.jaivox.protocol;

import com.jaivox.agent.*;

import java.io.*;
import java.net.*;

import com.jaivox.util.Log;

/**
 * An HtpServer manages requests for connections by creating an HtpSession.
 * Subsequent requests to the server from this connection are handled
 * through this session.
 */

public class HtpSession extends Session implements Runnable {

/**
 * Creates an HtpSession.
@param s	A session id used to identify connections for debugging
@param serve	the server
@param sock	Socket for communicating with this session
@param r	an HtpResponder that handles requests for URL's
 */
	
	public HtpSession (String s, HtpServer serve, Socket sock, HtpResponder r) {
		super.setSid (s);
		super.setServer (serve);
		super.setSocket (sock);
		super.setResponder (r);
		r.setOwner (this);
		try {
			setDaemon (true);
			start ();
		}
		catch (Exception e) {
			Log.warning (sid+":htpSession " + e.toString ());
		}
	}

/**
 * runs the thread, waiting for requests. This is started when the session
 * is created.
 */
	
	public void run () {
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader (
					socket.getInputStream ()));
			out = new PrintWriter (socket.getOutputStream ());

			String line = null;
			while (true) {
				line = readLineFromSocket ();
				if (line == null) continue;
				else break;
			}
			// Log.fine ("read "+line);
			String result = responder.responseString (line);
			if (result.startsWith ("Error:")) {
				// Log.fine (result);
			}
			else {
				out.println (result);
				out.flush ();
				sleep (waitTime);
				// Log.fine ("sent Response");
			}
			in.close ();
			out.close ();
			terminate ();
			socket.close ();
			server.removeSession (this);
			interrupt ();
			// Log.info (sid+" interrupted.");
		}
		catch (Exception e) {
			// Log.severe (sid+":run "+e.toString ());
		}
	}

}
