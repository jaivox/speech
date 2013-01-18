/*
   Jaivox version 0.3 December 2012
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
