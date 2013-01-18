/**
 * The Adjective class determines superlative and comparative forms of
 * adjectives. For example, "fast" is an attribute that may be applied
 * to a noun "car", but questions involving car and fast may ask, for
 * instance "is this car faster than that car?". The speech recognizer
 * as well as the interpreter needs to know that the user may ask about
 * "faster" or "fastest" instead of "fast." Adjective uses some rules
 * about forming those variants from the original adjective.
 */

package com.jaivox.interpreter;

// using rules from
// http://www.eflnet.com/tutorials/adjcompsup.php

import java.io.*;
import java.util.*;

public class Adjective {

	String vowels = "aeiouy";

	public Adjective () {
	}

	void Debug (String s) {
		System.out.println ("[Adjective]" + s);
	}

/**
 * Analyze a word, assumed to be an adjective to determine its normal,
 * comparative and superlative forms using some rules about English. The
 * analysis does not work in all cases due to special cases in English.	
@param word
@return a string containing three forms of the adjective
 */
	
	public String analyze (String word) {
		String in = word;
		int k = countsyllables (in);
		// System.out.println (in+" "+k+" syllables");
		String s = analyzespecial (in);
		if (s == null) {
			if (k == 1) s = analyzeone (in);
			else if (k == 2) s = analyzetwo (in);
			else s = analyzemore (in);
		}
		return s;
	}

	String analyzespecial (String word) {
		if (word.equals ("good")) return "good, better, best";
		else if (word.equals ("bad")) return "bad, worse, worst";
		else if (word.equals ("far")) return "far, farther, farthest";
		else if (word.equals ("little")) return "little, less, least";
		else if (word.equals ("many")) return "many, more, most";
		else if (word.equals ("slow")) return "slow, slower, slowest";
		else return null;
	}

	String analyzeone (String word) {
		StringBuffer sb = new StringBuffer ();
		sb.append (word);
		// if it ends with e, just add r for comparative st for superlative
		if (word.endsWith ("e")) {
			sb.append (", "+word+"r");
			sb.append (", "+word+"st");
			String s = new String (sb);
			return s;
		}
		int n = word.length ();
		// if it ends with y change the ending to i and add er or est
		if (word.endsWith ("y")) {
			String stub = word.substring (0, n-1);
			sb.append (", "+stub+"ier");
			sb.append (", "+stub+"iest");
			String s = new String (sb);
			return s;
		}
		char c = word.charAt (n-2);
		char d = word.charAt (n-1);
		// if it ends with VC double the C and add er or est
		if (isvowel (c) && iscons (d)) {
			sb.append (", "+word);
			sb.append (d);
			sb.append ("er");
			sb.append (", "+word);
			sb.append (d);
			sb.append ("est");
			String s = new String (sb);
			return s;
		}
		// otherwise just add er or est
		sb.append (", "+word+"er");
		sb.append (", "+word+"est");
		String s = new String (sb);
		return s;
	}

	String analyzetwo (String word) {
		StringBuffer sb = new StringBuffer ();
		sb.append (word);
		// if it ends with y change the ending to i and add er or est
		int n = word.length ();
		if (word.endsWith ("y")) {
			String stub = word.substring (0, n-1);
			sb.append (", "+stub+"ier");
			sb.append (", "+stub+"iest");
			String s = new String (sb);
			return s;
		}
		// if it ends with er, le or ow, just add er or est
		else if (word.endsWith ("–er") || word.endsWith ("le") || word.endsWith ("ow")) {
			sb.append (", "+word+"er");
			sb.append (", "+word+"est");
			String s = new String (sb);
			return s;
		}
		else {
			return analyzemore (word);
		}
	}

	String analyzemore (String word) {
		StringBuffer sb = new StringBuffer ();
		sb.append (word);
		sb.append (", more "+word);
		sb.append (", most "+word);
		String s = new String (sb);
		return s;
	}

	int getinteger (String word) {
		try {
			int v = Integer.parseInt (word);
			return v;
		}
		catch (Exception e) {
			return -1;
		}
	}

	int countsyllables (String word) {
		int count = 0;
		// count vowel groups  between consonants
		int n = word.length ();
		boolean seencons = true;
		// just ignore last letter like e in nice, but allow multiple vowels at end
		for (int i=0; i<n-1; i++) {
			char c = word.charAt (i);
			if (iscons (c)) {
				seencons = true;
				continue;
			}
			else {
				if (isvowel (c)) {
					if (seencons) count++;
					seencons = false;
					continue;
				}
			}
		}
		return count;
	}

	boolean isletter (char c) {
		if (c >= 'a' && c <= 'z') return true;
		else return false;
	}

	boolean isvowel (char c) {
		int pos = vowels.indexOf (c);
		if (pos == -1) return false;
		else return true;
	}

	boolean iscons (char c) {
		if (isletter (c) && !isvowel (c)) return true;
		else return false;
	}
};

