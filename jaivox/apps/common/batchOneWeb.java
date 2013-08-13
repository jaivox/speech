
import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import com.jaivox.recognizer.web.SpeechInput;
import com.jaivox.synthesizer.PATsynthesizer.Synthesizer;
import java.util.Properties;

public class PATbatchOneWeb {

	String audiodir = "PATaudiodir";
	String lang = "PATlang";
	String basedir = "./";
	Synthesizer speaker;


	static String recorded [] = {
		// add your spoken recordings here. For example if there
		// a recording "spoken10.wav", put "spoken10" here.
		// Note that the code here assumes that it will be a
		// .wav file, you can change it below to handle any
		// other format compatible with sphinx
	};

	public PATbatchOneWeb () {
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
		kv.setProperty ("lang", "PATlang");
		kv.setProperty ("ttslang", "PATttslang");
		Command cmd = new Command ();
		inter = new Interact (basedir, kv, cmd);
		speaker = new Synthesizer (basedir, kv);
	}

	void processSpeech () {
		try {
			// you can read in a file containing the actual
			// text of each recording in recorded [] and print
			// out the original recordings along with what is
			// recognized.
			if (recorded.length == 0) {
				System.out.println ("Please edit PATbatchWeb.java to include information");
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
					// can't always play flac
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
		new PATbatchOneWeb ();
	}
}

