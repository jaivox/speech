/*
   Jaivox version 0.4 April 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/**
 * Utils primarily consists of some edit distance functions.
 */

package com.jaivox.interpreter;

import java.awt.Point;
import java.util.StringTokenizer;

public class Utils {

	public static String terms = " \t\r\n!@$#$%^&*()_-~`+={}[]|\\:;\"\'<>,.?/";

	public Utils () {
	}

/**
 * Split tokens using the terminators given in Utils.terms
 * @param line
 * @return
 */

	public static String [] splitTokens (String line) {
		StringTokenizer st = new StringTokenizer (line, terms);
		int n = st.countTokens ();
		String qq [] = new String [n];
		for (int i=0; i<n; i++) {
			qq [i] = st.nextToken ();
		}
		return qq;
	}

/**
 * Assemble a string from an array of strings, mostly for printing out
 * @param tokens
 * @return
 */
	
	public static String makeString (String tokens []) {
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


/**
 * Used for edit distance
 * @param a
 * @param b
 * @param c
 * @return
 */
	// http://en.wikibooks.org/wiki/Algorithm_implementation/Strings/Levenshtein_distance#Java
	static int minimum(int a, int b, int c) {
		return Math.min (Math.min(a, b), c);
	}

/**
 * Determine the Levenshtein edit distance between two strings of characters
 * @param one
 * @param two
 * @return
 */	
	
	public static int editDistance (String one, String two) {
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

/**
 * Get the edit distance between two strings of words
 * @param a
 * @param b
 * @return
 */	
	
	public static int approxMatch (String a, String b) {
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

	
/**
 * Sort an array of points in increasing order of the y coordinate.
 * @param p
 * @param low
 * @param high
 */
	
    public static void quicksortpointy (Point p[], int low, int high) {
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
