
import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import com.jaivox.recognizer.web.SpeechInput;
import com.jaivox.recognizer.web.Mike;
import com.jaivox.synthesizer.freetts.Synthesizer;
import java.util.Properties;

public class roadOneWeb {

	static String basedir = "./";
	Interact inter;
	Synthesizer speaker;
	static int wait = 20; // maximum length of input in seconds
	static String type = ".wav";

	public roadOneWeb () {
		Log log = new Log ();
		log.setLevelByName ("warning");
		initializeInterpreter ();
		speaker = new Synthesizer ();
		processSpeech ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common_en.txt");
		kv.setProperty ("specs_file", "road.spec");
		kv.setProperty ("questions_file", "road.quest");
		kv.setProperty ("grammar_file", "road.dlg");
		Command cmd = new Command ();
		inter = new Interact (basedir, kv, cmd);
	}

	void processSpeech () {
		SpeechInput R = new SpeechInput ();
		Mike mike = new Mike ("road", type);
		int empty = 0;
		int maxempty = 5;
		while (true) {
			String flac = mike.nextsample (type, wait);
			mike.showtime ("result is "+flac);
			if (flac != null) {
				String result = R.recognize (flac, "en-US");
				System.out.println ("Recognized: " + result);
				String response = "";
				if (result != null) {
					if (result.trim ().equals ("")) {
						empty++;
						if (empty >= maxempty) return;
					}
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
		}
	}

	public static void main (String args []) {
		new roadOneWeb ();
	}
}

