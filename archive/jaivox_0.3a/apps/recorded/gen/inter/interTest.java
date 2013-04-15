
import com.jaivox.interpreter.*;
import com.jaivox.util.*;
import java.io.*;
import java.util.Properties;

public class interTest extends Thread {

	static int port = 3000;
	static int waitTime = 5000; // milliseconds

	static String
	connect1 = "connect localhost 2000",
	connect2 = "connect localhost 4000",
	who1 = "{action: JviaWho, from: inter, to: sphinx, message: Jviawho}",
	who2 = "{action: JviaWho, from: inter, to: festival, message: Jviawho}";

	public static void main (String args []) {
		InterServer jviatest;

		Properties kv = new Properties ();
		kv.setProperty ("data_file", "road.txt");
		kv.setProperty ("common_words", "common.txt");
		kv.setProperty ("specs_file", "road.spec");
		kv.setProperty ("questions_file", "road.quest");
		kv.setProperty ("recognizer", "sphinx");
		kv.setProperty ("interpreter", "inter");
		kv.setProperty ("synthesizer", "festival");
		
		Log log = new Log ();
		log.setLevelByName ("FINEST");
		
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
				log.info ("Executed: "+line);
				if (line.equals ("terminate")) {
					Log.info ("Ending test program");
					System.exit (1);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
