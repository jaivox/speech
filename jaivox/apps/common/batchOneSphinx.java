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
import com.jaivox.synthesizer.PATsynthesizer.Synthesizer;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

import java.util.Properties;

public class PATbatchOneSphinx extends Thread {

	static String audiodir = "PATaudiodir";
	static String config = "PATbatch.xml";
	static String basedir = "./";
	Interact inter;
	Synthesizer speaker;

	static String recorded [] = {
		// add your spoken recordings here. For example if there
		// a recording "spoken10.wav", put "spoken10" here.
		// Note that the code here assumes that it will be a
		// .wav file, you can change it below to handle any
		// other format compatible with sphinx
	};

	public PATbatchOneSphinx () {
		Log log = new Log ();
		log.setLevelByName ("PATlog_level");
		initializeInterpreter ();
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
		kv.setProperty ("ttslang", "PATttslang");
		inter = new Interact (basedir, kv, cmd);
		speaker = new Synthesizer (kv);
	}

	void processSpeech () {
		try {
			// you can read in a file containing the actual
			// text of each recording in recorded [] and print
			// out the original recordings along with what is
			// recognized.
			if (recorded.length == 0) {
				System.out.println ("Please edit PATbatchTest.java to include information");
				System.out.println ("on recordings to be used by the recognizer.");
				return;
			}
			ConfigurationManager cm = new ConfigurationManager (
					PATbatchOneSphinx.class.getResource (config));

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
					// For testing, you can print out the original
					// text of each recording along with the recognized
					// version.
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

	public static void main (String args []) {
		new PATbatchOneSphinx ();
	}

}
