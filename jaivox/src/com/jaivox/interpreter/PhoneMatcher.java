/*
   Jaivox version 0.7 March 2014
   Copyright 2010-2014 by Bits and Pixels, Inc.

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
package com.jaivox.interpreter;

import com.jaivox.util.Log;
import com.jaivox.util.Pair;
import java.util.StringTokenizer;
import java.util.TreeMap;

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
			String raw = t2p.convertToPhonemes (cleaned);
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
		StringBuffer sb = new StringBuffer ();
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
 * a map where the index is a Double and the value is an Integer indicating the
 * position of the matching question in the list of questions.
 * @param question
 * @return 
 */
	public TreeMap <Double, Integer> findBestMatchingSentences (String recognized,
			int start) {
		String cleaned = clean (recognized);
		String raw = t2p.convertToPhonemes (cleaned);
		String test = cleanPhones (raw);
		int bestdist = Integer.MAX_VALUE;
		int bestq = -1;
		TreeMap <Double, Integer> map = new TreeMap <Double, Integer> ();
		for (int i=0; i<N; i++) {
			int j = (i + start)%N;
			int d = Utils.approxMatch (qphones [j], test);
			if (d < bestdist) {
				bestdist = d;
				bestq = j;
			}
			double delta = (double)i/100.0;
			Double D = new Double (d + delta);
			map.put (D, new Integer (j));
		}
		if (bestq >= 0) {
			Log.info ("Best match question "+questions [bestq]+" distance "+bestdist);
		}
		else {
			Log.info ("No matches found for "+recognized);
		}
		return map;
	}		
	
	public double findPhoneCount (String recognized) {
		String cleaned = clean (recognized);
		String raw = t2p.convertToPhonemes (cleaned);
		String test = cleanPhones (raw);
		String phones [] = Utils.splitTokens (test);
		double n = (double)(phones.length);
		return n;
	}

}
