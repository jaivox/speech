
/**
 * sphinxTest creates a SphinxServer. It also uses Sphinx as a library
 * to recognize spoken questions.
 */

import java.io.*;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import com.jaivox.recognizer.sphinx.*;
import com.jaivox.util.*;

public class sphinxTest extends Thread {

	static int port = 2000;
	static int waitTime = 1000; // one second

	static String work1 =
	"send sphinx_0 {action: interpret, from: sphinx, to: inter, message: ";
	static String connect1 = "connect localhost 3000";
	static String who1 = "send sphinx_0 {action: JviaWho, from: sphinx, to: inter, message: Jviawho}";
	static String config = "road.config.xml";

	static void processSpeech (SphinxServer server) {
		ConfigurationManager cm = new ConfigurationManager (
				sphinxTest.class.getResource (config));
		// allocate the recognizer
		Log.info ("Loading...");
		Recognizer recognizer = (Recognizer) cm.lookup ("recognizer");
		recognizer.allocate ();

		// start the microphone or exit if the programm if this is not possible
		Microphone microphone = (Microphone) cm.lookup ("microphone");
		if (!microphone.startRecording ()) {
			Log.severe ("Cannot start microphone.");
			recognizer.deallocate ();
			System.exit (1);
		}

		System.out.println ("Sample questions are in road.sent");

		// loop the recognition until the programm exits.
		while (true) {
			System.out.println ("Start speaking. Press Ctrl-C to quit.\n");

			Result result = recognizer.recognize ();

			if (result != null) {
				String resultText = result.getBestResultNoFiller ();
				System.out.println ("You said: " + resultText + '\n');
				String name = server.getServerId ();
				String message = work1 + "\"" + resultText + "\"}";
				server.execute (message);
				try {
					Thread.sleep (4000);
				} catch (Exception e) {
					System.out.println (e.toString ());
					break;
				}
			} else {
				System.out.println ("I can't hear what you said.");
			}
		}
	}

	public static void main (String args []) {
		SphinxServer Recognizer;
		Log log = new Log ();
		log.setLevelByName ("info");
		try {
			Recognizer = new SphinxServer ("sphinx", 2000);
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));

			Log.info ("Waiting to connect to inter");
			boolean connected = false;
			int waiting = 0;
			while (!connected && waiting < 1000) {
				sleep (waitTime);
				String result = Recognizer.executeReply (connect1);
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
				String result = Recognizer.executeReply (who1);
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
				Log.info ("Established credentials with inter");
				Log.info ("Processing recognition results");
			}

			sleep (waitTime * 4);

			// loop in SpeechInput
			processSpeech (Recognizer);
			System.out.println ("Terminating ...");
			System.exit (1);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
