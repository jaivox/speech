
import com.jaivox.synthesizer.freetts.*;
import com.jaivox.util.*;

/**
 * This test replaces the festival package (in C) with a java-based text
 * to speech package Free Tts. To run this program, please note that you
 * need to include the path to freetts.jar explicitly in the classpath
 * since this location is used to find other required files. See comments
 * in com.jaivox.synthesizer.freetts.Synthesizer for details.
 * 
 * In this application we call the SynthServer agent "Festival" for compatibliy
 * with other agents in this apps/recorded demo. Note that you should use
 * this synthesizer and NOT invoke festival.
 */

public class synthesizerTest extends Thread {

	static int port = 4000;
	static int waitTime = 1; // one second

	static String connect1 = "connect localhost 3000";
	static String who1 = "send festival_0 {action: JviaWho, from: festival, to: inter, message: Jviawho}";

	public static void main (String args []) {
		SynthServer Festival;
		Log log = new Log ();
		log.setLevelByName ("warning");
		try {
			Festival = new SynthServer ("festival", port);

			Log.info ("Waiting to connect to inter");
			boolean connected = false;
			int waiting = 0;
			while (!connected && waiting < 1000) {
				sleep (waitTime);
				String result = Festival.executeReply (connect1);
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
				String result = Festival.executeReply (who1);
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
				Log.info ("Processing recognition results");
			}

			sleep (waitTime * 4);
			while (Festival.isAlive ()) {
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
