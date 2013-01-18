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
