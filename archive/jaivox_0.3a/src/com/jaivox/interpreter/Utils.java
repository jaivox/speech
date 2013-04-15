/**
 * Utils primarily consists of some edit distance functions.
 */

package com.jaivox.interpreter;

import java.util.*;
import java.awt.Point;

public class Utils {

	static String terms = " \t\r\n!@$#$%^&*()_-~`+={}[]|\\:;\"\'<>,.?/";

	public Utils () {
	}


	public String [] splitTokens (String line) {
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

	public int editDistance (String one, String two) {
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

	public int approxMatch (String a, String b) {
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
