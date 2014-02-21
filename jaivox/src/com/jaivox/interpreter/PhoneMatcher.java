package com.jaivox.interpreter;

import com.jaivox.util.Log;
import com.jaivox.util.Pair;
import java.util.StringTokenizer;

public class PhoneMatcher {
	TextToPhoneme t2p;
	static String terms = " .,;:/?-()[]{}$#@!%&*\'\"<>\t\r\n";
	
	String questions [];
	int N;
	String qphones [];
	
	public PhoneMatcher (String rules, String q []) {
		t2p = new TextToPhoneme (rules);
		questions = q;
		N = q.length;
		qphones = new String [N];
		for (int i=0; i<N; i++) {
			String question = questions [i];
			String cleaned = clean (question);
			String raw = t2p.l2p (cleaned);
			qphones [i] = cleanPhones (raw);
		}
	}
		
	String clean (String s) {
		StringTokenizer st = new StringTokenizer (s, terms);
		StringBuffer sb = new StringBuffer ();
		while (st.hasMoreTokens ()) {
			sb.append (st.nextToken ());
			if (st.hasMoreTokens ()) sb.append (' ');
		}
		String result = new String (sb);
		return result;
	}

	String cleanPhones (String phones) {
		StringTokenizer st = new StringTokenizer (phones);
		StringBuilder sb = new StringBuilder ();
		while (st.hasMoreTokens ()) {
			String token = st.nextToken ();
			if (token.equals ("_")) continue;
			sb.append (token);
			sb.append (' ');
		}
		String result = new String (sb).trim ();
		return result;
	}
	
	public Pair [] findBestMatchingSentences (String question) {
		String cleaned = clean (question);
		String raw = t2p.l2p (cleaned);
		String test = cleanPhones (raw);
		int bestdist = Integer.MAX_VALUE;
		int bestq = -1;
		Pair pp [] = new Pair [N];
		for (int i=0; i<N; i++) {
			int d = Utils.editDistance (qphones [i], test);
			if (d < bestdist) {
				bestdist = d;
				bestq = i;
			}
			pp [i] = new Pair (i, d);
		}
		if (bestq >= 0) {
			Log.info ("Best match question "+questions [bestq]+" distance "+bestdist);
		}
		else {
			Log.info ("No matches found for "+question);
		}
		Utils.quicksortpointy (pp, 0, N-1);
		return pp;
	}			
			
		

// check edit distance for comparison
// http://en.wikibooks.org/wiki/Algorithm_implementation/Strings/Levenshtein_distance#Java
	int minimum(int a, int b, int c) {
		return Math.min (Math.min(a, b), c);
	}

	int editDistance (String a, String b) {
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
}
