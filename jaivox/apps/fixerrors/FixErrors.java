
import com.jaivox.interpreter.PhoneMatcher;
import com.jaivox.util.Pair;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class FixErrors {
    static String file_correct = "correct.txt";
    static String file_recognized = "recognized.txt";
	static String correct [];
	static String recognized [];
	static int N;
	static String rules = "../common/t2prules_en.tree";

	static String [] loadStrings (String filename, boolean upper) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			ArrayList <String> list = new ArrayList <String> ();
			String line;
			while ((line = in.readLine ()) != null) {
				list.add (line);
			}
			in.close ();
			int n = list.size ();
			String result [] = list.toArray (new String [n]);
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}

	public static void main (String args []) {
		correct = loadStrings (file_correct, false);
		recognized = loadStrings (file_recognized, false);
		N = correct.length;
		PhoneMatcher pm = new PhoneMatcher (rules, correct);
		int errors = 0;
		for (int i=0; i<N; i++) {
			Pair pp [] = pm.findBestMatchingSentences (recognized [i]);
			Pair p = pp [0];
			String intented = correct [i];
			String selected = correct [p.x];
			if (!selected.equals (intented)) {
				errors++;
				System.out.println (""+i+" selected "+p.x+" distance "+p.y);
				System.out.println ("intended  : "+intented);
				System.out.println ("recognized: "+recognized [i]);
				System.out.println ("selected  : "+selected);
			}
		}
		System.out.println ("Total errors: "+errors);
	}
}

