
/**
 * sphinxTest creates a SphinxServer. It also uses Sphinx as a library
 * to recognize spoken questions.
 * This version (sphinxBatchTest) uses precorded audio instead
 * of requiring the user to speak into a microphone.
 */

import java.io.*;
import java.net.URL;
import java.util.TreeMap;
import javax.sound.sampled.*;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import com.jaivox.recognizer.sphinx.*;
import com.jaivox.util.*;

public class sphinxBatchTest extends Thread {

	static int port = 2000;
	static int waitTime = 1000; // one second
	static String stub = "../../../audio/";

	static String work1 =
	"send sphinx_0 {action: interpret, from: sphinx, to: inter, message: ";
	static String connect1 = "connect localhost 3000";
	static String who1 = "send sphinx_0 {action: JviaWho, from: sphinx, to: inter, message: Jviawho}";
	static String query = "send sphinx_0 {action: interpret, from: sphinx, to: inter, message: \"what is the fastest route\"}";
	static String config = "batch.xml";

	static String questions [] = {
		"03",	// are roads busy
		"02",	// do the roads get congested at this time
		"05",	// do you think elmwood avenue is slow
		"06",	// do you think old mill road is fast
		"10",	// is avenue o the quickest highway
		"14",	// what are the fast roads
		"28",	// any other fast roads
		"31",	// are highways stop and go
		"32",	// do you think elmwood avenue is stop and go
	};

	static void processSpeech (SphinxServer server) {
		try {
			BufferedReader in = new BufferedReader (
					new FileReader ("road.txt"));
			TreeMap <String, String> original =
					new TreeMap <String, String> ();
			String line;
			while ((line = in.readLine ()) != null) {
				int pos = line.indexOf ("\t");
				if (pos == -1) continue;
				String tag = line.substring (1, pos);
				String val = line.substring (pos+1);
				original.put (tag, val);
			}
			in.close ();
			ConfigurationManager cm = new ConfigurationManager (
					sphinxBatchTest.class.getResource (config));
			// allocate the recognizer
			Log.info ("Loading...");
			Recognizer recognizer = (Recognizer) cm.lookup ("recognizer");
			recognizer.allocate ();
			for (int i=0; i<questions.length; i++) {
				StringBuffer sb = new StringBuffer ();
				sb.append (stub);
				sb.append ("road");
				sb.append (questions [i]);
				sb.append (".wav");
				String filename = new String (sb);
				System.out.println (filename);
				// URL audioURL = batchTest.class.getResource (filename);
				URL audioURL = new File (filename).toURI ().toURL ();
				AudioFileDataSource dataSource = (AudioFileDataSource) cm.lookup ("audioFileDataSource");
				dataSource.setAudioFile (audioURL, null);

				Result result;

				while ((result = recognizer.recognize ()) != null) {
					String resultText = result.getBestResultNoFiller ();
					String orig = original.get (questions [i]);
					System.out.println ("Original  : "+orig);
					System.out.println ("Recognized: " + resultText);
					String message = work1 + "\"" + resultText + "\"}";
					server.execute (message);
				}
				try {
					Thread.sleep (4000);
					play (filename);
					play ("../festival/wave.wav");
				} catch (Exception e) {
					e.printStackTrace ();
					break;
				}
			}
			in.close ();
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	static void play (String filename) {
		try {
			File f = new File (filename);
			AudioInputStream as = AudioSystem.getAudioInputStream (f);
			AudioFormat format = as.getFormat ();
			DataLine.Info info = new DataLine.Info (Clip.class, format);
			Clip clip = (Clip)AudioSystem.getLine (info);
			clip.open (as);
			clip.start ();
			while (!clip.isRunning())
				Thread.sleep(10);
			while (clip.isRunning())
				Thread.sleep(10);
			clip.close();
			Thread.sleep (1000);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public static void main (String args []) {
		SphinxServer Recognizer;
		Log log = new Log ();
		log.setLevelByName ("warning");
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
			// test query even if speech recognition is not working
			Recognizer.executeReply (query);
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
