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

import java.net.*;
import java.util.*;

import com.jaivox.util.Log;

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
			Log.info ("started server "+serverId+" name "+Name);
			clients = new Vector <Session> ();
			Valid = true;
		}
		catch (Exception e) {
			Log.severe (Name + e.toString ());
			// Valid will be false
		}
	}

	public Server () {

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
				Log.info (Name+": Added client "+ias.sid);
			}
		}
		catch (Exception e) {
			Log.severe (Name+ ": run "+e.toString ());
		}
	}

	public void addSession (Session ias) {
		clients.add (ias);
	}
	
	public void removeSession (Session ias) {
		clients.remove (ias);
		Log.info (Name + ": Removed client "+ias.sid);
	}
	
	public int sessionCount () {
		return clients.size ();
	}
	
	public Session findSession (String id) {
		int n = clients.size ();
		Log.fine (Name+": looking for client with id "+id);
		for (int i=0; i<n; i++) {
			Session ias = clients.elementAt (i);
			Log.fine (Name+": client "+i+" id is "+ias.sid);
			if (ias.sid.equals (id)) return ias;
		}
		return null;
	}
	
	public Session findSessionTo (String to) {
		int n = clients.size ();
		Log.fine (Name+ ": looking for client to "+to);
		for (int i=0; i<n; i++) {
			Session ias = clients.elementAt (i);
			Log.fine (Name+": client "+i+" id is "+ias.sid);
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

