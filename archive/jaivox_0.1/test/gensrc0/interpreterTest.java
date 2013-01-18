
import com.jaivox.interpreter.*;
import java.io.*;
import java.util.Properties;

public class PATinterpreterTest extends Thread {

	static int port = PATport_interpreter;
	static int waitTime = 5000; // milliseconds

	static String
	connect1 = "connect localhost PATport_recognizer",
	connect2 = "connect localhost PATport_synthesizer",
	who1 = "{action: JviaWho, from: PATinterpreter, to: PATrecognizer, message: Jviawho}",
	who2 = "{action: JviaWho, from: PATinterpreter, to: PATsynthesizer, message: Jviawho}";

	public static void main (String args []) {
		InterServer jviatest;

		Properties kv = new Properties ();
		kv.setProperty ("data_file", "PATdata_file");
		kv.setProperty ("common_words", "PATcommon_words");
		kv.setProperty ("specs_file", "PATspecs_file");
		kv.setProperty ("questions_file", "PATquestions_file");
		kv.setProperty ("recognizer", "PATrecognizer");
		kv.setProperty ("interpreter", "PATinterpreter");
		kv.setProperty ("synthesizer", "PATsynthesizer");
		
		try {
			File currentDir = new File (".");
			String path = currentDir.getCanonicalPath ();
			String dir = path + "/";
			jviatest = new InterServer ("inter", port, dir, kv);
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));
			while (true) {
				String line = in.readLine ();
				if (line.startsWith ("connect1")) line = connect1;
				else if (line.startsWith ("connect2")) line = connect2;
				else if (line.endsWith ("who1")) line = line.replaceAll ("who1", who1);
				else if (line.endsWith ("who2")) line = line.replaceAll ("who2", who2);
				jviatest.execute (line);
				sleep (waitTime);
				System.out.println ("Executed: "+line);
				if (line.equals ("terminate")) {
					System.out.println ("Ending test program");
					System.exit (1);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
