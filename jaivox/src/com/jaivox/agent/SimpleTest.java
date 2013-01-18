
package com.jaivox.agent;

import java.io.*;

/**
 * SimpleTest is a small program to test connections and commands
 * in the agent world. You can create a SimpleTest and use it to
 * send messages to other agents.
 * You can see what sort of commands to enter at the command line
 * by checking TestServer.execute ()
 */

public class SimpleTest extends Thread {

	static int port = 2000;
	static int waitTime = 5000; // milliseconds

	public static void main (String args []) {
		TestServer iatest;
		try {
			iatest = new TestServer (port);
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));
			while (true) {
				System.out.print ("> ");
				String line = in.readLine ();
				iatest.execute (line);
				sleep (waitTime);
				System.out.println ("Executed: "+line);
				if (line.equals ("terminate")) {
					System.out.println ("Ending test program");
					System.exit (1);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
