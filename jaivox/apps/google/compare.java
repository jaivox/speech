
import java.io.*;

public class compare {

	static int n = 33;

	static String recorded [];
	static String google [];
	static String sphinx [];

	// http://en.wikibooks.org/wiki/Algorithm_implementation/Strings/Levenshtein_distance#Java
	static int minimum(int a, int b, int c) {
		return Math.min (Math.min(a, b), c);
	}

    static int approxMatch (String a, String b) {
		String one [] = a.split (" ");
		String two [] = b.split (" ");
		int n = one.length;
		int m = two.length;
        int [][] distance = new int [n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            distance [i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            distance [0][j] = j;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                distance[i][j] = minimum (
                    distance[i-1][j] + 1,
                    distance[i][j-1] + 1,
                    distance[i-1][j-1] + (one[i-1].equals (two[j-1]) ? 0 : 1));
            }
        }
        return distance [n][m];
    }

	public static void main (String args []) {
		try {
			BufferedReader in;
			String line;

			recorded = new String [n];
			in = new BufferedReader (new FileReader ("recorded.txt"));

			for (int i=0; i<n; i++) {
				line = in.readLine ();
				String part = line.substring (3).toLowerCase ();
				recorded [i] = part;
			}
			in.close ();

			google = new String [n];
			in = new BufferedReader (new FileReader ("google.txt"));

			for (int i=0; i<n; i++) {
				line = in.readLine ();
				String part = line.substring (3).toLowerCase ();
				google [i] = part;
			}
			in.close ();

			sphinx = new String [n];
			in = new BufferedReader (new FileReader ("sphinx.txt"));

			for (int i=0; i<n; i++) {
				line = in.readLine ();
				String part = line.substring (3).toLowerCase ();
				sphinx [i] = part;
			}
			in.close ();

			int googletotal = 0;
			int sphinxtotal = 0;

			for (int i=0; i<n; i++) {
				StringBuffer sb = new StringBuffer ();
				sb.append ('0');
				int j = i+1;
				if (j<10) sb.append ('0');
				sb.append (j);
				sb.append ('\t');
				int d1 = approxMatch (recorded [i], google [i]);
				sb.append (d1);
				googletotal += d1;
				sb.append ('\t');
				int d2 = approxMatch (recorded [i], sphinx [i]);
				sb.append (d2);
				sphinxtotal += d2;
				String s = new String (sb);
				System.out.println (s);
			}
			System.out.println ("Google: "+googletotal+" Sphinx: "+sphinxtotal);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
}
