
package com.jaivox.recognizer.sphinx;

import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import com.jaivox.agent.*;

public class SphinxServer extends Server implements Runnable {
	int waitTime = 5000;

	public SphinxServer (int port) {
		super (port);
	}
	
	public SphinxServer (String name, int port) {
		super (name, port);
	}
	
	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				int count = getIdCount ();
				String id = Name+"_"+getIdCount ();
				setIdCount (count+1);
				SphinxResponder r = new SphinxResponder ();
				SpeechSession ias = new SpeechSession (id, this, link, r);
				addSession (ias);
				Debug ("Added client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			Debug (Name+e.toString ());
		}
	}

	public void Debug (String s) {
		System.out.println ("[SphinxServer]" + s);
	}
	
	public void execute (String command) {
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
				String id = Name+"_"+count;
				SphinxResponder rtest = new SphinxResponder ();
				SpeechSession ias = new SpeechSession (id, this, link, rtest);
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
				JviaFileServer FS = new JviaFileServer (path, 0);
				
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

/**
 * Execute a request with a reply
@param command
@return
 */
	
	public String executeReply (String command) {
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
					return "Error: invalid syntax";
				}
				String host = tokens.elementAt (1);
				int port = Integer.parseInt (tokens.elementAt (2));
				Socket link = new Socket (host, port);
				int count = sessionCount ();
				String id = Name+"_"+count;
				SphinxResponder rtest = new SphinxResponder ();
				SpeechSession ias = new SpeechSession (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				return ("OK: Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					return ("Error: Sytax: disconnect sessionid");
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					return ("Error: No session with id "+id);
				}
				// send a terminate message to the session
				Debug ("disconnecting from "+id);
				String req = "{action: JviaTerminate, from: "+getId ()+", to: "+ias.getSid ();
				req += ", message: JviaTerminate}";
				ias.outbuffer = req;
				sleep (waitTime);
				ias.terminate ();
				// then terminate it
				sleep (waitTime);
				return ("OK: Terminate requested.");
			}
			else if (command.startsWith ("send")) {
				// write it to the outstream of the session?
				if (ntok < 3) {
					Debug ("Error: Syntax: send sessionid message");
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					return ("Error: No session with id "+id);
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
					sleep (waitTime);
					return ("OK: Terminate requested.");
				}
				else return ("OK: Message sent:"+message);
			}
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
				return ("OK: terminated.");
			}
			else {
				return ("Error: illegal command: "+command);
			}
		}
		catch (Exception e) {
			return ("Error: "+e.toString ());
		}
	}

	public static void main (String args []) {
		String name = args [0];
		int port = Integer.parseInt (args [1]);
		SphinxServer iat = new SphinxServer (name, port);
	}

}
