
package com.jaivox.interpreter;

import java.net.Socket;
import java.util.*;

import com.jaivox.agent.*;
import com.jaivox.util.Log;

/**
 * InterServer is an interpreter server. In a typical application, this
 * server communicates with a speech recognizer as well as a speech
 * synthesizer.
 */

public class InterServer extends Server implements Runnable {
	int waitTime = 5000;

	String basedir;
	Properties kv;
	Interact inter;
	
	
/**
 * The interpreter often needs information from some files.
 * Here the server sets some variables that can be accessed by
 * the InterResponder when creating an Interact class
@param name
@param port
@param base
@param kv	Properties for the interpreter
 */
	
	public InterServer (String name, int port, String base, Properties pp) {
		super (name, port);
		basedir = base;
		kv = pp;
		inter = new Interact (base, kv);
	}

/**
 * The server sometimes need to pass a custom command handler to the
 * interpreter. This form of the InterServer constructor allows for
 * a Command class to be passed to the interpreter
@param name
@param port
@param base
@param pp
@param cmd
 */
	
	public InterServer (String name, int port, String base, 
			Properties pp, Command cmd) {
		super (name, port);
		basedir = base;
		kv = pp;
		inter = new Interact (base, kv, cmd);
	}

/**
 * Creates an InterServer that listens on the designated port
@param port	socket listens on this port
 */
	public InterServer (int port) {
		super (port);
	}
	
/**
 * Crates an InterServer with a specific name, that listens at
 * the specified port
@param name	Name of the server (used in messages)
@param port socket listens on this port
 */
	
	public InterServer (String name, int port) {
		super (name, port);
	}
	
	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				int count = getIdCount ();
				String id = Name+"_"+getIdCount ();
				setIdCount (count+1);
				InterResponder r = new InterResponder (basedir, kv, inter);
				InterSession ias = new InterSession (id, this, link, r);
				addSession (ias);
				Log.info ("Added client "+ias.getSid ());
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe (Name+" "+e.toString ());
		}
	}

/**
 * Execute agent-based commands to connect, disconnect and perform
 * other actions
@param command
 */
	
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
					Log.warning ("Syntax: connect host port");
					return;
				}
				String host = tokens.elementAt (1);
				int port = Integer.parseInt (tokens.elementAt (2));
				Socket link = new Socket (host, port);
				int count = sessionCount ();
				String id = Name+"_"+count;
				InterResponder rtest = new InterResponder (basedir, kv, inter);
				InterSession ias = new InterSession (id, this, link, rtest);
				rtest.setOwner (ias);
				addSession (ias);
				Log.info ("Made connection client id "+id);
			}
			else if (command.startsWith ("disconnect")) {
				if (ntok != 2) {
					Log.warning ("Sytax: disconnect sessionid");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Log.warning ("No session with id "+id);
					return;
				}
				// send a terminate message to the session
				Log.info ("disconnecting from "+id);
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
					Log.warning ("Sytax: send sessionid message");
					return;
				}
				String id = tokens.elementAt (1);
				Session ias = findSession (id);
				if (ias == null) {
					Log.warning ("No session with id "+id);
					return;
				}
				StringBuffer sb = new StringBuffer ();
				sb.append (tokens.elementAt (2));
				for (int i=3; i<ntok; i++) {
					sb.append (' ');
					sb.append (tokens.elementAt (i));
				}
				String message = new String (sb);
				ias.outbuffer = message;
				// Log.fine ("sending \""+message+"\" to "+id);
				if (message.equals (TestResponder.terminateRequest)) {
					sleep (waitTime);
					ias.terminate ();
				}
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
					Log.info ("Terminating "+ias.getSid ());
					ias.terminate ();
				}
				server.close ();
				interrupt ();
				return;
			}
			else {
				Log.warning ("illegal command: "+command);
			}
		}
		catch (Exception e) {
			Log.severe ("Interserver: execute "+e.toString ());
		}
	}
	
	public Interact getInteract () {
		return inter;
	}
	
	public void setInteract (Interact interactor) {
		inter = interactor;
	}
	
}
