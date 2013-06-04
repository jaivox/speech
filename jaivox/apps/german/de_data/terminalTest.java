/**
 * terminalTest can be used to test only the interpreter, without involving
 * a speech recognizer or synthesizer.
 */


import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.Interact;
import com.jaivox.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class terminalTest {

	static String basedir = "./";
	Interact inter;

	public terminalTest () {
		Log log = new Log ();
		log.setLevelByName ("fine");
		initializeInterpreter ();
		processQuestions ();
	}

	void initializeInterpreter () {
		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common_de.txt");
		kv.setProperty ("specs_file", "road_de.spec");
		kv.setProperty ("questions_file", "road.quest");
		kv.setProperty ("grammar_file", "road_de.dlg");
		Command cmd = new roadCommand ();
		inter = new Interact (basedir, kv, cmd);
	}

	void processQuestions () {
		try {
			BufferedReader in = new BufferedReader (new InputStreamReader (System.in));
			do {
				System.out.print ("> ");
				String line = in.readLine ();
				String response = inter.execute (line);
				System.out.println (": "+response);
				inter.getScript ().showHistory ();
			} while (true);
		}
		catch (Exception e) {
			e.printStackTrace ();
			return;
		}
	}

	public static void main (String args []) {
		new terminalTest ();
	}
}
