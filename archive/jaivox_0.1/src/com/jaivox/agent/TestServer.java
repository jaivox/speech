/**
 * A TestServer is a simple Server (works with TestResponder.
 * it is used to set and and test server connections and a few
 * simple commands.
 */

package com.jaivox.agent;

import com.jaivox.protocol.FileServer;
import java.net.*;
import java.util.*;

public class TestServer extends Server implements Runnable {
	
	int waitTime = 5000;
	FileServer FS = null;

	public TestServer (int port) {
		super (port);
	}
	
	public TestServer (String name, int port) {
		super (name, port);
	}
	
	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				String id = Name+"_"+getIdCount ();
				TestResponder r = new TestResponder ();
				Session ias = new Session (id, this, link, r);
				addSession (ias);
				Debug ("Added client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			Debug (Name+e.toString ());
		}
	}

	public void Debug (String s) {
		System.out.println ("[testServer]" + s);
	}
	
	void execute (String command) {
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
					Debug ("Syntax: connect host port");
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
				Debug ("Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					Debug ("Sytax: disconnect sessionid");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Debug ("No session with id "+id);
					return;
				}
				// send a terminate message to the session
				Debug ("disconnecting from "+id);
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
					Debug ("Sytax: send sessionid message");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Debug ("No session with id "+id);
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
				Debug ("sending \""+message+"\" to "+id);
				if (message.equals (TestResponder.terminateRequest)) {
					sleep (waitTime);
					ias.terminate ();
				}
			}
			/*
			else if (command.startsWith ("transfer")) {
				// write it to the outstream of the session?
				if (ntok < 3) {
					Debug ("Sytax: transfer sessionid pathtofile");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Debug ("No session with id "+id);
					return;
				}
				String path = tokens.elementAt (2);
				FileServer FS = new FileServer (path, 0);
				
				StringBuffer sb = new StringBuffer ();
				sb.append ("iaTest:fetch ");
				sb.append (FS.getServerHost ());
				sb.append (' ');
				sb.append (FS.getListenPort ());
				sb.append (" "+path);
				String message = new String (sb);
				ias.outbuffer = message;
				Debug ("sending \""+message+"\" to "+id);
			}
			*/
			else if (command.equals ("terminate")) {
				// terminate all clients, then stop
				Vector <Session> clients = getClients ();
				for (int i=0; i<clients.size (); i++) {
					Session ias = clients.elementAt (i);
					String req = "{action: JviaTerminate, from: "+getId ()+", to: "+ias.getSid ();
					req += ", message: JviaTerminate}";
					ias.outbuffer = req;
					sleep (waitTime);
					Debug ("Terminating "+ias.getSid ());
					ias.terminate ();
				}
				server.close ();
				interrupt ();
				return;
			}
			else {
				Debug ("illegal command: "+command);
			}
		}
		catch (Exception e) {
			Debug (e.toString ());
		}
	}

	public static void main (String args []) {
		String name = args [0];
		int port = Integer.parseInt (args [1]);
		TestServer iat = new TestServer (name, port);
	}
}
