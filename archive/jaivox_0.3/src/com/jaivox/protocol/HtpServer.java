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

import java.net.*;
import java.util.*;

import com.jaivox.util.Log;

/**
 * HtpServer implements a very simple HTTP server. This server can be
 * used as a free-standing HTTP server for URL requests. It can also
 * be used in a more complex application to serve files. For example,
 * this can be used to serve files in response to a request made through
 * a speech agent (e.g. a spoken request "Where is Elm Street?" can be
 * handled with a display of the map.) 
 */

public class HtpServer extends Server implements Runnable {
	
	public static int defaultPort = 1080;
	
	/**
	 * Create an HtpServer using the default port (1080)
	 */
	
	public HtpServer () {
		super ();
		setListenPort (defaultPort);
		super.setIdCount (0);
		try {
			ServerSocket use = new ServerSocket (defaultPort);
			setServer (use);
			// setDaemon (true);
			start ();
			setServerId (use.getInetAddress ().getHostName ());
			Log.info ("started htpServer");
			setClients (new Vector <Session> ());
			setValid (true);
		}
		catch (Exception e) {
			Log.severe ("htpServer:htpServer" + e.toString ());
			// Valid will be false
		}
		
	}
	
	/**
	 * Create an HtpServer using the designated port
	 * 
	@param port numerical port to be used for HTTP requests
	 */
	
	public HtpServer (int port) {
		super ();
		setListenPort (port);
		super.setIdCount (0);
		try {
			ServerSocket use = new ServerSocket (defaultPort);
			setServer (use);
			// setDaemon (true);
			start ();
			setServerId (use.getInetAddress ().getHostName ());
			Log.info ("started htpServer");
			setClients (new Vector <Session> ());
			setValid (true);
		}
		catch (Exception e) {
			Log.severe ("htpServer:htpServer" + e.toString ());
			// Valid will be false
		}
		
	}
	
	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				int count = getIdCount ();
				String id = "htps:"+ count;
				setIdCount (count+1);
				HtpResponder r = new HtpResponder ();
				HtpSession ias = new HtpSession (id, this, link, r);
				addSession (ias);
				Log.info ("Added htp client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			Log.severe ("htpServer:run "+e.toString ());
		}
	}

	public static void main (String args []) {
		new HtpServer ();
	}
}
