/**
 * Utils consists of a simple logger and a few helper functions.
 * It also contains some edit distance functions.
 */

package com.jaivox.interpreter;

import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.Point;

public class Utils {

	static String logdir = "./";
	static String terms = " \t\r\n!@$#$%^&*()_-~`+={}[]|\\:;\"\'<>,.?/";

	RandomAccessFile R;

	SimpleDateFormat stamp;

	public Utils () {
		startLog ();
	}

	void startLog () {
		try {
			stamp = new SimpleDateFormat ("HH:mm:ss");
			SimpleDateFormat dateformat = new SimpleDateFormat ("yyMMdd");
			Date d = new Date ();
			String s = dateformat.format (d);
			String name = logdir + "log"+s+".txt";
 			R = new RandomAccessFile (name, "rw");
			System.out.println ("Log file: "+name);
			long N = R.length ();
			R.seek (N);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void record (String s) {
		// save after each write
		try {
            Date now = new Date ();
            String ts = stamp.format (now);
			R.writeBytes (ts);
			R.writeBytes ("\t");
			R.writeBytes (s);
			R.writeBytes ("\n");
			// System.out.println (s);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void endLog () {
		try {
			if (R != null) R.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	String [] splitTokens (String line) {
		StringTokenizer st = new StringTokenizer (line, terms);
		int n = st.countTokens ();
		String qq [] = new String [n];
		for (int i=0; i<n; i++) {
			qq [i] = st.nextToken ();
		}
		return qq;
	}

	String makeString (String tokens []) {
		StringBuffer sb = new StringBuffer ();
		int n = tokens.length;
		if (n == 0) return "";
		sb.append (tokens [0]);
		for (int i=1; i<n; i++) {
			sb.append (" "+tokens [i]);
		}
		String result = new String (sb);
		return result;
	}

	// http://en.wikibooks.org/wiki/Algorithm_implementation/Strings/Levenshtein_distance#Java
	int minimum(int a, int b, int c) {
		return Math.min (Math.min(a, b), c);
	}

	int editDistance (String one, String two) {
		int n = one.length ();
		int m = two.length ();
		int [][] distance = new int [n + 1][m + 1];

		for (int i = 0; i <= n; i++)
			distance [i][0] = i;
		for (int j = 0; j <= m; j++)
			distance [0][j] = j;

		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				distance [i][j] = minimum (
				distance [i - 1][j] + 1,
				distance [i][j - 1] + 1,
				distance [i - 1][j - 1]
					+ ((one.charAt (i - 1) == two.charAt (j - 1)) ? 0 : 1));
			}
		}

		return distance [n][m];
	}

	int approxMatch (String a, String b) {
		String one [] = a.split (" ");
		String two [] = b.split (" ");
		int d = approxMatch (one, two);
		return d;
	}

    int approxMatch (String one [], String two []) {
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

    static void quicksortpointy (Point p[], int low, int high) {
        // Debug("quicksort "+low+" to "+high);
        int lo = low;
        int hi = high;
        if (lo >= hi) {
            return;
        }
        int mid = p[(lo + hi) / 2].y;
        while (lo <= hi) {
            while (lo < high && p[lo].y < mid) {
                lo++;
            }
            while (hi > low && p[hi].y > mid) {
                hi--;
            }
            if (lo <= hi) {
                Point t;
                t = p[lo];
                p[lo] = p[hi];
                p[hi] = t;
                lo++;
                hi--;
            }
        }

        if (low < hi) {
            quicksortpointy (p, low, hi);
        }
        if (lo < high) {
            quicksortpointy (p, lo, high);
        }
    }




};
