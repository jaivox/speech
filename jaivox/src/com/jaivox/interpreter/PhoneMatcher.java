package com.jaivox.interpreter;

import com.jaivox.util.Log;
import com.jaivox.util.Pair;
import java.util.StringTokenizer;

/**
 * Use the standard edit distance to match a sequence of phonemes to a stored
 * set of sequences of phonemes.
 * @author dev
 */

public class PhoneMatcher {
	TextToPhoneme t2p;
	static String terms = " .,;:/?-()[]{}$#@!%&*\'\"<>\t\r\n";
	
	String questions [];
	int N;
	String qphones [];
	
/**
 * Create a phoneme matcher using rules for text to phoneme conversion (for
 * English see apps/common/t2prules_en.tree) and a set of questions. In a typical
 * speech recognition application, a user's question is recognized as some words.
 * These are converted to phonemes and matched against phonemes corresponding
 * to stored questions. This is used to guess the actual question asked by the
 * user, assuming it is one of the questions used when creating this class.
 * @param rules
 * @param q 
 */
	
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
	
/**
 * Create a sorted list of stored questions that may match a recognized string
 * produced by a speech recognizer. The results are returned in the form of
 * Pairs, i.e. x and y values. Here x is the index of a question in the original
 * list of questions used when creating this class, and the y is the edit distance
 * of the sequence of recognized phonemes from the phonemes belonging to the
 * selected x-th question.
 * @param question
 * @return 
 */
	public Pair [] findBestMatchingSentences (String recognized) {
		String cleaned = clean (recognized);
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
			Log.info ("No matches found for "+recognized);
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
