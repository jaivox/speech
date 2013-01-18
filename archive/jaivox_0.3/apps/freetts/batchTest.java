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

/*
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

/**
 * batchTest uses sphinx, jaivox and freetts as libraries to recognize spoken
 * questions. It uses precorded audio instead of requiring the user to speak
 * into a microphone.
 */

import java.io.*;
import java.net.URL;
import java.util.TreeMap;
import java.util.Properties;

import javax.sound.sampled.*;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;

import com.jaivox.interpreter.*;
import com.jaivox.util.*;

public class batchTest extends Thread {

	static String audiodir = "../audio/";
	static String config = "batch.xml";
	static String basedir = "../recorded/gen/inter/";
	Interact inter;
	VoiceManager tts;
	Voice speaker;


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

	public batchTest () {
		Log log = new Log ();
		log.setLevelByName ("warning");
		initializeInterpreter ();
		initializeTts ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common.txt");
		kv.setProperty ("specs_file", "road.spec");
		kv.setProperty ("questions_file", "road.quest");
		inter = new Interact (basedir, kv);
	}

	void initializeTts () {
		tts = VoiceManager.getInstance ();
        Voice [] voices = tts.getVoices ();
		if (voices == null || voices.length == 0) {
			System.out.println ("No voices available for freetts");
			System.out.println ("Please see README.txt for details");
			System.exit (0);
		}
		System.out.println ("Available voices:");
        for (int i = 0; i < voices.length; i++) {
			String voice = voices [i].getName () + " " + voices [i].getDomain ();
			System.out.println (voice);
 		}

		speaker = tts.getVoice ("kevin16");
		speaker.allocate ();
	}

	void processSpeech () {
		try {
			BufferedReader in = new BufferedReader (
					new FileReader ("road.questions"));
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
					batchTest.class.getResource (config));

			Recognizer recognizer = (Recognizer) cm.lookup ("recognizer");
			recognizer.allocate ();
			for (int i=0; i<questions.length; i++) {
				StringBuffer sb = new StringBuffer ();
				sb.append (audiodir);
				sb.append ("road");
				sb.append (questions [i]);
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
					String orig = original.get (questions [i]);
					System.out.println ("Original  : "+orig);
					System.out.println ("Recognized: " + resultText);
					recognized = resultText;
					break;
				}
				if (recognized != null) {
					response = inter.execute (recognized);
					System.out.println ("Reply: " + response);
				}
				try {
					Thread.sleep (4000);
					play (filename);
					// speaker.allocate ();
					speaker.speak (response);
					// speaker.deallocate ();
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
		new batchTest ();
	}

}
