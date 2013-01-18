/**
 * HtpServer implements a very simple HTTP server. This server can be
 * used as a free-standing HTTP server for URL requests. It can also
 * be used in a more complex application to serve files. For example,
 * this can be used to serve files in response to a request made through
 * a speech agent (e.g. a spoken request "Where is Elm Street?" can be
 * handled with a display of the map.) 
 */

package com.jaivox.protocol;

import com.jaivox.agent.*;

import java.net.*;
import java.util.*;


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
			Debug ("started htpServer");
			setClients (new Vector <Session> ());
			setValid (true);
		}
		catch (Exception e) {
			Debug ("htpServer:htpServer" + e.toString ());
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
			Debug ("started htpServer");
			setClients (new Vector <Session> ());
			setValid (true);
		}
		catch (Exception e) {
			Debug ("htpServer:htpServer" + e.toString ());
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
				Debug ("Added htp client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			Debug ("htpServer:run "+e.toString ());
		}
	}

	public static void main (String args []) {
		HtpServer htps = new HtpServer ();
	}
}
