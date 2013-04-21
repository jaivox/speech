

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


/**
 * sphinxTest creates a SphinxServer. It also uses Sphinx as a library
 * to recognize spoken questions.
 * This version (sphinxBatchTest) uses precorded audio instead
 * of requiring the user to speak into a microphone.
 */

import com.jaivox.recognizer.sphinx.SphinxServer;
import com.jaivox.util.Log;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TreeMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class sphinxBatchTest extends Thread {

	static int port = 2000;
	static int waitTime = 1000; // one second
	static String audiodir = "../../../audio/";

	static String work1 =
	"send sphinx_0 {action: interpret, from: sphinx, to: inter, message: ";
	static String connect1 = "connect localhost 3000";
	static String who1 = "send sphinx_0 {action: JviaWho, from: sphinx, to: inter, message: Jviawho}";
	static String config = "batch.xml";

	static String recorded [] = {
		"road03",	// are roads busy
		"road02",	// do the roads get congested at this time
		"road05",	// do you think elmwood avenue is slow
		"road06",	// do you think old mill road is fast
		"road10",	// is avenue o the quickest highway
		"road14",	// what are the fast roads
		"road16",	// which freeway is quicker than old mill road
		"road31",	// are highways stop and go
		"road32",	// do you think elmwood avenue is stop and go
	};

	static void processSpeech (SphinxServer server) {
		try {
			BufferedReader in = new BufferedReader (
					new FileReader ("recorded.txt"));
			TreeMap <String, String> original =
					new TreeMap <String, String> ();
			String line;
			while ((line = in.readLine ()) != null) {
				int pos = line.indexOf ("\t");
				if (pos == -1) continue;
				String tag = "road" + line.substring (1, pos);
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
			for (int i=0; i<recorded.length; i++) {
				StringBuffer sb = new StringBuffer ();
				sb.append (audiodir);
				sb.append (recorded [i]);
				if (!recorded [i].endsWith (".wav"))
					sb.append (".wav");
				String filename = new String (sb);
				System.out.println (filename);
				URL audioURL = new File (filename).toURI ().toURL ();
				AudioFileDataSource dataSource = (AudioFileDataSource) cm.lookup ("audioFileDataSource");
				dataSource.setAudioFile (audioURL, null);

				Result result;
				try {
					while ((result = recognizer.recognize ()) != null) {
						String resultText = result.getBestResultNoFiller ();
						String orig = original.get (recorded [i]);
						System.out.println ("Original  : "+orig);
						System.out.println ("Recognized: " + resultText);
						play (filename);
						Thread.sleep (2000);
						String message = work1 + "\"" + resultText + "\"}";
						server.execute (message);
						Thread.sleep (4000);
					}
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
