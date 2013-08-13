

import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import com.jaivox.recognizer.web.SpeechInput;
import com.jaivox.synthesizer.web.Synthesizer;
import java.util.Properties;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;


public class batchTest extends Thread {

	static String audiodir = "../audio_es/";
	static String basedir = "./";
	Interact inter;
	String lang = "es-MX";
	String ttslang = "es";
	Synthesizer speaker;

	// the recorded questions are in road.quest

	static String recorded [] = {
		"caminos00",
		"caminos01",
		"caminos02"
	};


	public batchTest () {
		Log log = new Log ();
		log.setLevelByName ("info");
		initializeInterpreter ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common_es.txt");
		kv.setProperty ("specs_file", "road_es.spec");
		kv.setProperty ("questions_file", "road.quest");
		kv.setProperty ("grammar_file", "road_es.dlg");
		kv.setProperty ("lang", lang);
		kv.setProperty ("ttslang", ttslang);
		// Command cmd = new Command ();
		Command cmd = new roadCommand ();
		inter = new Interact (basedir, kv, cmd);
		speaker = new Synthesizer (basedir, kv);
	}

	void processSpeech () {
		try {
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
					play (wavfile);
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

	void play (String filename) {
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
