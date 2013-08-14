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

import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import com.jaivox.recognizer.web.*;


import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;


public class roadWebFreetts {

	String audiodir = "../audio/";
	String lang = "en-US";
	String basedir = "./";
	Interact inter;
	VoiceManager tts;
	Voice speaker;


	static String recorded [] = {
		"road05",	// do you think elmwood avenue is slow
		"road06",	// do you think old mill road is fast
		"road10",	// is avenue o the quickest highway
		"road14",	// what are the fast roads
		"road16",	// which freeway is quicker than old mill road
		"road31",	// are highways stop and go
		"road32",	// do you think elmwood avenue is stop and go
	};

	public roadWebFreetts () {
		Log log = new Log ();
		log.setLevelByName ("warning");
		initializeInterpreter ();
		initializeTts ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common_en.txt");
		kv.setProperty ("specs_file", "road.spec");
		kv.setProperty ("questions_file", "road.quest");
		kv.setProperty ("grammar_file", "road.dlg");
		// Command cmd = new Command ();
		Command cmd = new roadCommand ();
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
		try {
			// you can read in a file containing the actual
			// text of each recording in recorded [] and print
			// out the original recordings along with what is
			// recognized.
			if (recorded.length == 0) {
				System.out.println ("Please edit roadWeb.java to include information");
				System.out.println ("on recordings to be used by the recognizer.");
				return;
			}
			SpeechInput recognizer = new SpeechInput ();

			for (int i=0; i<recorded.length; i++) {
				StringBuffer sb = new StringBuffer ();
				sb.append (audiodir);
				sb.append (recorded [i]);
				if (!recorded [i].endsWith (".flac"))
					sb.append (".flac");
				String filename = new String (sb);
				System.out.println (filename);
				String result = recognizer.recognize (filename, lang);
				System.out.println ("Recognized: " + result);
				String response = "";
				if (result != null) {
					response = inter.execute (result);
					System.out.println ("Reply: " + response);
				}
				try {
					Thread.sleep (4000);
					String wavfile = filename.replaceAll (".flac", ".wav");
					play (wavfile); // can't seem to play flac directly
					speaker.speak (response);
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
		new roadWeb ();
	}
}

