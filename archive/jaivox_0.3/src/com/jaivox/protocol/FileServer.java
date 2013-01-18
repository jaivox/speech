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

import java.io.*;
import java.net.*;

import com.jaivox.util.Log;

/**
 * The file server implements a simple protocol to handle requiests for
 * files. Agents may require entire files to be transferred from the
 * control of one agent to another. The FileServer is initialized with
 * the name of such a file. After that a request to transforer the
 * file is handled in the run () function. 
 */


public class FileServer extends Thread {
	String fileName;
	String serverHost;
	int listenPort;
	ServerSocket server;
	
	static final int packetSize = 1024;
	// later add time out to socket so that we have chance to turn off
	// boolean turnOff = false;

	/**
	 * Creates the FileServer. The port to use for requests and the
	 * specific file to be served are specified when the server is
	 * created. The run method of this thread returns after the file
	 * is served.
	 @param file to be served
	 @param port for socket to receive request
	 */
	
	public FileServer (String file, int port) {
		fileName = file;
		listenPort = port;
		serverHost = "localhost";
		try {
			// normally create the server with port 0, it
			// will be changed to an available port
			server = new ServerSocket (port);
			listenPort = server.getLocalPort ();
			serverHost = server.getInetAddress ().getHostName ();
			Log.info ("fileServer:"+ serverHost+" listening on "+listenPort);
		}
		catch (Exception e) {
			Log.severe ("fileServer:fileServer" + e.toString ());
			e.printStackTrace ();
		}
		start ();
	}

	/**
	 * The run is terminated after a file is served. The file to be
	 * served is determined when this class is created. Request for the
	 * file is received through the socket connection of the server.
	 */
	
	public void run () {
		// blocks until interrupted
		try {
			Socket dataLink = server.accept ();
			// open the file and sends it through this port
			FileInputStream in = new FileInputStream (fileName);
			OutputStream out = dataLink.getOutputStream ();
			byte b[] = new byte [packetSize];
			while (true) {
				int bytesRead = in.read(b);
				if (bytesRead < 0) break;
				out.write (b, 0, bytesRead);
			}
			in.close ();
			out.close ();
			dataLink.close ();
			server.close ();
			return;
		}
		catch (Exception e) {
			Log.severe ("fileServer:run" + e.toString ());
			e.printStackTrace ();
		}
	}

	public String getFileName () {
		return fileName;
	}

	public void setFileName (String fileName) {
		this.fileName = fileName;
	}

	public String getServerHost () {
		return serverHost;
	}

	public void setServerHost (String serverHost) {
		this.serverHost = serverHost;
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

}
