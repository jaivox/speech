/*
   Jaivox version 0.5 August 2013
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

import com.jaivox.synthesizer.PATsynthesizer.SynthServer;
import com.jaivox.util.Log;
import java.util.Properties;

/**
 * This test replaces the Festival package (in C) with a java-based text
 * to speech package. Depending on the synthesizer used, some additional
 * libraries may be needed.
 */

public class PATsynthesizerTest extends Thread {

	static int port = PATport_synthesizer;
	static int waitTime = 1; // one second

	static String connect1 = "connect localhost PATport_interpreter";
	static String who1 = "send PATnamesynthesizer_0 {action: JviaWho, from: PATnamesynthesizer, to: PATnameinterpreter, message: Jviawho}";

	public static void main (String args []) {
		SynthServer Synth;
		Log log = new Log ();
		log.setLevelByName ("PATlog_level");
		Properties kv = new Properties ();
		kv.setProperty ("ttslang", "PATttslang");

		try {
			Synth = new SynthServer ("PATnamesynthesizer", port, "./", kv);

			Log.info ("Waiting to connect to inter");
			boolean connected = false;
			int waiting = 0;
			while (!connected && waiting < 1000) {
				sleep (waitTime);
				String result = Synth.executeReply (connect1);
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
				String result = Synth.executeReply (who1);
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
				Log.info ("Established credentials with PATnameinterpreter");
				Log.info ("Processing synthesis requests");
			}

			sleep (waitTime * 4);
			while (Synth.isAlive ()) {
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
