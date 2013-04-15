/*
   Jaivox version 0.4 April 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

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

import com.jaivox.synthesizer.freetts.*;
import com.jaivox.util.*;

/**
 * This test replaces the Fttsynth package (in C) with a java-based text
 * to speech package Free Tts. To run this program, please note that you
 * need to include the path to freetts.jar explicitly in the classpath
 * since this location is used to find other required files. See comments
 * in com.jaivox.synthesizer.freetts.Synthesizer for details.
 *
 * In this application we call the SynthServer agent "Fttsynth" for compatibliy
 * with other agents in this apps/recorded demo. Note that you should use
 * this synthesizer and NOT invoke Fttsynth.
 */

public class PATsynthesizerTest extends Thread {

	static int port = PATport_synthesizer;
	static int waitTime = 1; // one second

	static String connect1 = "connect localhost PATport_interpreter";
	static String who1 = "send Fttsynth_0 {action: JviaWho, from: PATsynthesizer, to: PATinterpreter, message: Jviawho}";

	public static void main (String args []) {
		SynthServer Fttsynth;
		Log log = new Log ();
		log.setLevelByName ("warning");
		try {
			Fttsynth = new SynthServer ("Fttsynth", port);

			Log.info ("Waiting to connect to inter");
			boolean connected = false;
			int waiting = 0;
			while (!connected && waiting < 1000) {
				sleep (waitTime);
				String result = Fttsynth.executeReply (connect1);
				if (result.startsWith ("OK:")) {
					connected = true;
					break;
				}
				else {
					waiting++;
					System.out.println ("Waiting ... "+waiting);
					continue;
				}
			}
			if (!connected) {
				Log.severe ("Waiting for too long, terminating ...");
				return;
			}
			else {
				Log.info ("Connected to inter");
				Log.info ("Waiting to establish who credentials");
			}
			sleep (waitTime * 4);
			boolean established = false;
			waiting = 0;
			while (!established && waiting < 100) {
				sleep (waitTime);
				String result = Fttsynth.executeReply (who1);
				if (result.startsWith ("OK:")) {
					established = true;
					break;
				}
				else {
					waiting++;
					Log.info ("Establishing credentials ... "+waiting);
					continue;
				}
			}
			if (!established) {
				Log.severe ("Establishing did not work, terminating ...");
				return;
			}
			else {
				Log.info ("Established credentials with PATinterpreter");
				Log.info ("Processing synthesis requests");
			}

			sleep (waitTime * 4);
			while (Fttsynth.isAlive ()) {
				Thread.sleep (waitTime);
			}
			System.out.println ("Terminating ...");
			System.exit (1);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
