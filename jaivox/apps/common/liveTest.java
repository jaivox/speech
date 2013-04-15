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
 * PATliveTest is a single file speech recognizer that connects
 * sphinx for recognition, an interpreter for creating answers
 * and freetts to speak the results.
 */


import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;


import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.util.Properties;


public class PATliveTest extends Thread {

	static String config = "PATlive.xml";
	static String basedir = "./";
	Interact inter;
	VoiceManager tts;
	Voice speaker;

	public PATliveTest () {
		Log log = new Log ();
		log.setLevelByName ("warning");
		initializeInterpreter ();
		initializeTts ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "PATdata_file");
		kv.setProperty ("common_words", "PATcommon_words");
		kv.setProperty ("specs_file", "PATspecs_file");
		kv.setProperty ("questions_file", "PATquestions_file");
		kv.setProperty ("grammar_file", "PATgrammar_file");
		Command cmd = new Command ();
		inter = new Interact (basedir, kv, cmd);
	}

	void initializeTts () {
		tts = VoiceManager.getInstance ();
		Voice [] voices = tts.getVoices ();
		if (voices == null || voices.length == 0) {
			System.out.println ("No voices available for freetts");
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
		ConfigurationManager cm = new ConfigurationManager (
				PATliveTest.class.getResource (config));
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

		System.out.println ("Sample questions are in lm_training_file");

		try {
			// loop the recognition until the programm exits.
			while (true) {
				System.out.println ("Start speaking. Press Ctrl-C to quit.\n");

				Result result = recognizer.recognize ();
				String recognized = null;
				String response = null;

				if (result != null) {
					recognized = result.getBestResultNoFiller ();
					System.out.println ("You said: " + recognized + '\n');
				} else {
					System.out.println ("I can't hear what you said.");
					continue;
				}
				if (recognized != null) {
					response = inter.execute (recognized);
					System.out.println ("Reply: " + response);
					Thread.sleep (4000);
					speaker.speak (response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public static void main (String args []) {
		new PATliveTest ();
	}

}
