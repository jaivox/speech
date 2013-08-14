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

/**
 * PATrecognizerTest creates a SphinxServer. It also uses Sphinx as a library
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

public class PATrecognizerBatchTest extends Thread {

	static int port = PATport_recognizer;
	static int waitTime = 1000; // one second

	static String work1 =
	"send PATnamerecognizer_0 {action: interpret, from: PATnamerecognizer, to: PATnameinterpreter, message: ";
	static String connect1 = "connect localhost PATport_interpreter";
	static String who1 = "send PATnamerecognizer_0 {action: JviaWho, from: PATnamerecognizer, to: PATnameinterpreter, message: Jviawho}";
	static String config = "PATrecognizer_config_file";

	static String audiodir = "PATaudiodir";

	static String recorded [] = {
		// add your spoken recordings here. For example if there
		// a recording "spoken10.wav", put "spoken10" here.
		// Note that the code here assumes that it will be a
		// .wav file, you can change it below to handle any
		// other format compatible with sphinx
	};

	static void processSpeech (SphinxServer server) {
		try {
			// you can read in a file containing the actual
			// text of each recording in recorded [] and print
			// out the original recordings along with what is
			// recognized.
			if (recorded.length == 0) {
				System.out.println ("Please edit PATrecognizerBatchTest.java to include information");
				System.out.println ("on recordings to be used by the recognizer.");
				return;
			}
			ConfigurationManager cm = new ConfigurationManager (
					PATrecognizerBatchTest.class.getResource (config));

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

				String recognized = null;
				String response = null;
				while ((result = recognizer.recognize ()) != null) {
					String resultText = result.getBestResultNoFiller ();
					String message = work1 + "\"" + resultText + "\"}";
					server.execute (message);
				}
				try {
					Thread.sleep (4000);
					play (filename);
					// Sometimes java-based programs and festival do not work
					// together, in these cases, you can change the C++ programs
					// for festival to write a wave file instead of playing the
					// response correctly, and have this program play this wave file.
					// for example:
					// play ("../festival/wave.wav");
				} catch (Exception e) {
					e.printStackTrace ();
					break;
				}
			}
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
		log.setLevelByName ("PATlog_level");
		try {
			Recognizer = new SphinxServer ("PATnamerecognizer", PATport_recognizer);
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));

			Log.info ("Waiting to connect to PATnameinterpreter");
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
				Log.info ("Connected to PATnameinterpreter");
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
				Log.info ("Established credentials with PATnameinterpreter");
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
