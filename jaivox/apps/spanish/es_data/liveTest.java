
/**
 * liveTest is a single file speech recognizer that connects
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


public class liveTest extends Thread {

	static String config = "live_es.xml";
	static String basedir = "./";
	Interact inter;
	VoiceManager tts;
	Voice speaker;

	public liveTest () {
		Log log = new Log ();
		log.setLevelByName ("finest");
		initializeInterpreter ();
		initializeTts ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common_es.txt");
		kv.setProperty ("specs_file", "road_es.spec");
		kv.setProperty ("questions_file", "road.quest");
		kv.setProperty ("grammar_file", "road_es.dlg");
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
		ConfigurationManager cm = new ConfigurationManager (
				liveTest.class.getResource (config));
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
		new liveTest ();
	}

}
