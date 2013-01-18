/**
 * Creates a generic server for asynchronous communication with agent
 * and task handlers.
 * 
 * Each agent is capable of performing specific tasks. A user may request
 * an agent to do something. The user interacts with a user interface to
 * make this request. Specific task handlers perform the actual work. Some
 * handlers coordinate the communication between task handlers and user
 * interfaces.
 * 
 */

package com.jaivox.agent;

import java.net.*;
import java.util.*;

public class Server extends Thread implements Runnable {

	int listenPort;
	protected ServerSocket server;
	String serverId;
	Vector <Session> clients;
	int idCount;
	static String defaultName = "Jvia";
	public String Name;

	boolean Valid = false;

	public Server (int port) {
		Name = defaultName; 
	}
	
/**
 * Create a server with a specific id, which listens at the designated
 * port.
@param name Name of the agent
@param port	Listen to this port for connection requests
 */

	public Server (String name, int port) {
		Name = name;
		listenPort = port;
		idCount = 0;
		try {
			server = new ServerSocket (listenPort);
			start ();
			serverId = server.getInetAddress ().getHostName ()+"_"+port;
			Debug ("started server "+serverId+" name "+Name);
			clients = new Vector <Session> ();
			Valid = true;
		}
		catch (Exception e) {
			Debug (Name + e.toString ());
			// Valid will be false
		}
	}

	public Server () {

	}

	public void Debug (String message) {
		System.out.println ("[Server]"+Name+":"+listenPort+" "+message);
	}

	public void run () {
		try {
			while (true) {
				Socket link = server.accept ();
				String id = Name+"_"+idCount;
				idCount++;
				Responder r = new Responder ();
				Session ias = new Session (id, this, link, r);
				clients.add (ias);
				Debug ("Added client "+ias.sid);
			}
		}
		catch (Exception e) {
			Debug ("Server:run "+e.toString ());
		}
	}

	public void addSession (Session ias) {
		clients.add (ias);
	}
	
	public void removeSession (Session ias) {
		clients.remove (ias);
		Debug ("Removed client "+ias.sid);
	}
	
	public int sessionCount () {
		return clients.size ();
	}
	
	public Session findSession (String id) {
		int n = clients.size ();
		Debug ("looking for client with id "+id);
		for (int i=0; i<n; i++) {
			Session ias = clients.elementAt (i);
			Debug ("client "+i+" id is "+ias.sid);
			if (ias.sid.equals (id)) return ias;
		}
		return null;
	}
	
	public Session findSessionTo (String to) {
		int n = clients.size ();
		Debug ("looking for client to "+to);
		for (int i=0; i<n; i++) {
			Session ias = clients.elementAt (i);
			Debug ("client "+i+" id is "+ias.sid);
			if (ias.sid.startsWith (to)) return ias;
		}
		return null;
	}
	
	public int getListenPort () {
		return listenPort;
	}

	public void setListenPort (int listenPort) {
		this.listenPort = listenPort;
	}

	public ServerSocket getServer () {
		return server;
	}

	public void setServer (ServerSocket server) {
		this.server = server;
	}

	public String getServerId () {
		return serverId;
	}

	public void setServerId (String serverId) {
		this.serverId = serverId;
	}

	public Vector<Session> getClients () {
		return clients;
	}

	public void setClients (Vector<Session> clients) {
		this.clients = clients;
	}

	public int getIdCount () {
		return idCount;
	}

	public void setIdCount (int idCount) {
		this.idCount = idCount;
	}

	public boolean isValid () {
		return Valid;
	}

	public void setValid (boolean valid) {
		Valid = valid;
	}

}

