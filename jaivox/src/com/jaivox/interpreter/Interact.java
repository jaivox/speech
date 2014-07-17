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
import com.jaivox.util.Recorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;


/**
 * Each Interact manages a conversation. In a typical application, Interact is asked
 * to execute sentences (generally questions.) The questions can be queries without
 * any context, or it can be a follow up to earlier questions. Interact therefore
 * manages the state of its conversation and uses the state to determine the next
 * action.
 */

public class Interact {

	Properties kv;
	Command command;
	Recorder Record;
	Info data;
	Script gen;
	
	String basedir = "./";
	String specfile = "spec.txt";
	String common = "common.txt";
	String answer = "answer.txt";
	String grammarfile = "gram.dlg";

	TreeMap <String, String> lookup;
	TreeMap <String, Vector<String>> multiword;
	String questions [];

	PhoneMatcher phonematch;
	
	String lexicon [];
	int nl;
	
	public static int MaxMatch = 5;
	public static int triggerSearch = 60;
	int startSearch = 0;

/**
 * Interact initialize all other classes used in the conversation.
 * This version permits the user to set the basedir and specfile
 * in a location different from the default location.
@param base	Directory containing all data required by the application
@param spec	specifications file, in the base directory
 */
	public Interact (String base, Properties pp) {
		if (!base.endsWith (File.separator)) base = base + File.separator;
		basedir = base;
		kv = pp;
		command = new Command ();
		initialize ();
	}
	
/**
 * In this form of the constructor, we can pass a custom command
 * handler to the interpreter
@param pp
@param cmd
 */

	public Interact (String base, Properties pp, Command cmd) {
		if (!base.endsWith (File.separator)) base = base + File.separator;
		basedir = base;
		kv = pp;
		command = cmd;
		initialize ();
	}

/**
 * Initialize data used by the interpreter. Also start a recorder to
 * record the questions recognized by the recognizer in combination with
 * the interpreter. (The recognizer's output is matched against questions
 * that can be recognized to find a close match - if any.)
 * 
 * Starting with version 0.7, you can optionally specify a file "phone_database".
 * For English this is a set of text to phoneme rules learned using a program
 * called t2p created at Carnegie Mellon University which uses the CMU Pronouncing
 * Dictionary. If such a set of rules are provided, then recognized strings are
 * matched to stored questions using a phoneme-level edit distance matching.
 */
	void initialize () {
		String stub = "Interact";
		String temp = kv.getProperty ("recorder_name");
		if (temp != null) stub = temp;
		Record = new Recorder (basedir + stub);
		specfile = kv.getProperty ("specs_file");
		String base = kv.getProperty("Base");
		if(base != null) basedir = base;
		grammarfile = basedir + kv.getProperty ("grammar_file");
		lookup = new TreeMap <String, String> ();
		multiword = new TreeMap <String, Vector<String>> ();
		common = basedir + kv.getProperty ("common_words");
		if (specfile != null) data = new Info (basedir, specfile);
		gen = new Script (this);
		addcommon ();
		loadquestions ();
		updateLexicon ();
		
		// in work/apps/common/t2prules_en.tree
		String phonedb = kv.getProperty ("phone_database");
		if (phonedb != null) {
			phonematch = new PhoneMatcher (phonedb, questions);
		}
		else {
			phonematch = null;
		}
	}

/**
 * If the basedir is defined in a properties record, then we can create the
 * interpreter using just the properties as argument. This form also creates
 * a Recorder to keep track of a session's questions and answers.
 * @param pp 
 */
	
	public Interact (Properties pp) {
		kv = pp;
		basedir = kv.getProperty ("Base");
		command = new Command ();
		if (basedir == null) {
			basedir = "./";
		}
		initialize ();
		Record = new Recorder ("interact");
		gen = new Script (this);
	}
	
/**
 * Interact initializes all the other classes used in the conversation
 * (Info, Script and Sement.) It also loads all the data located in the
 * basedir location. This version assumes that the basedir
 * and specfile are declared above in the right locations since
 * Info is loaded from there.
 * This form does not work because kv does not contain anything. It can
 * be used if initialization is done after Interact is created and kv is updated.
 */
	
	public Interact () {
		Record = new Recorder ("interact");
		kv = new Properties ();
		Log.severe ("No properties specified for Interact.");
		return;
	}

/**
 * Terminate the Interact by exiting the system. To be used
 * only when Interact is used from a console application.
 */
	public void terminate () {
		Recorder.endRecord ();
		System.exit (1);
	}

	void addcommon () {
		try {
			BufferedReader in = new BufferedReader (new FileReader (common));
			String line;
			while ((line = in.readLine ()) != null) {
				String text = line.toLowerCase ().trim ();
				StringTokenizer st = new StringTokenizer (text);
				while (st.hasMoreTokens ()) {
					String word = st.nextToken ();
					lookup.put (word, "common");
				}
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void loadquestions () {
		String indicator = "qspecs";
		Vector <String> hold = new Vector <String> ();
		TreeMap<String, String> qspecs = gen.questspecs;
		Set<String> keys = qspecs.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext ();) {
			String key = it.next ();
			if (Script.isState (key)) {
				continue;
			}
			hold.add (key);
			String tokens[] = Utils.splitTokens (key);
			int n = tokens.length;
			if (n>0) {
				for (int i = 0; i<n; i++) {
					lookup.put (tokens[i], indicator);
				}
			}
		}
		int n = hold.size ();
		questions = hold.toArray (new String [n]);
	}
	
	void updateLexicon () {
		try {
			nl = lookup.size ();
			lexicon = new String [nl];
			Set <String> keys = lookup.keySet ();
			int j=0;
			for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				lexicon [j++] = key;
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * the main way to interpret something. After creating the class and
 * initializing the related classes, the execute function processes a
 * query to manage the conversation state and to produce a response.
 * the response in this case may be a question clarifying the original
 * query.
 * If phone_database was specified, then PhoneMatcher is used to find the
 * best match for the recognized query.
@param query
@return result of executing the query
 */

	public String execute (String query) {
		Recorder.record ("Q: "+query);
		String lq = query.toLowerCase ();
		// get the match list
		String in [] = Utils.splitTokens (lq);
		String cleaned = Utils.joinTokens (in, " ");
		double n = (double)(in.length);
		if (n == 0) {
			Log.info ("No input");
			return gen.errorResult (lq, null);
		}

		TreeMap <Double, Integer> candidates;
		if (phonematch != null) {
			n = phonematch.findPhoneCount (cleaned);
			candidates = phonematch.findBestMatchingSentences (cleaned, startSearch);
		}
		else {
			candidates = findBestMatchingSentences (cleaned, startSearch);
		}
		// standardize goodness
		double best = 0.0;
		double denominator = (double)questions.length;
		TreeMap <Double, String> matches = new TreeMap <Double, String> ();
		Set <Double> keys = candidates.keySet ();
		int m = keys.size ();
		Double dd [] = keys.toArray (new Double [m]);
		for (int i=0; i<m && i<MaxMatch; i++) {
			Double D = dd [i];
			double d = D.doubleValue ();
			int qi = candidates.get (D);
// 			double delta = (double)i/denominator;
			double percentage = (n-d)*100.0/n;
			if (percentage > best) best = percentage;
			Double E = new Double (-percentage);
			String s = questions [qi];
			matches.put (E, s);
		}
		Log.info ("Matches without searches: "+matches.toString ());
		// now try the search approach if percentage is not high enough
		if (best < triggerSearch) {
			LinkedHashMap <String, String> searches = new LinkedHashMap <String, String> ();
			StringTokenizer st = new StringTokenizer (lq);
			int ntok = 0;
			while (st.hasMoreTokens ()) {
				String token = st.nextToken ();
				if (searches.get (token) == null) {
					searches.put (token, "yes");
					ntok++;
				}
			}
			TreeMap <Double, Integer> results;
			results = findBestSearchResults (searches, startSearch);
			double xntok = (double)ntok;
			// judge the seach results
			keys = results.keySet ();
			m = keys.size ();
			dd = keys.toArray (new Double [m]);
			for (int i=0; i<m && i<MaxMatch; i++) {
				Double D = dd [i];
				double d = -D.doubleValue ();
				int qi = results.get (D);
	// 			double delta = (double)i/denominator;
				double percentage = (d/xntok)*100.0;
				if (percentage > best) best = percentage;
				Double E = new Double (-percentage);
				String s = questions [qi];
				matches.put (E, s);
			}
		}
		Log.info ("Matches with searches: "+matches.toString ());
		String result = gen.handleInputValue (matches);
		Recorder.record ("A: "+result);
		// gen.control.showTrack ();
		int found = findMatchingQuestion (matches);
		int N = questions.length;
		startSearch = (found + 1)%N;
		Log.info ("found = "+found+" startSearch = "+startSearch);
		return result;
	}

	TreeMap <Double, Integer>  findBestMatchingSentences (String query, int start) {
		int N = questions.length;
		int bestdist = Integer.MAX_VALUE;
		int bestq = -1;
		TreeMap <Double, Integer> map = new TreeMap <Double, Integer> ();
		for (int i=0; i<N; i++) {
			int j = (i + start)%N;
			int d = Utils.approxMatch (questions [i], query);
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
			Log.info ("No matches found for "+query);
		}
		return map;
	}
	
	boolean checkfit (String a, String b, int d) {
		int n = a.length ();
		int m = b.length ();
		int x = Math.min (n, m);
		if (d > x/2) return false;
		return true;
	}
	
	// should do this with incremental separating search
	
	TreeMap <Double, Integer>  findBestSearchResults (
			LinkedHashMap <String, String> searches, int start) {
		int N = questions.length;
		int bestmatch = 0;
		int bests = -1;
		TreeMap <Double, Integer> map = new TreeMap <Double, Integer> ();
		for (int i=0; i<N; i++) {
			int j = (i + start)%N;
			StringTokenizer st = new StringTokenizer (questions [j]);
			String first = st.nextToken ();	// discard it
			if (!first.equals ("jaivoxsearch")) continue;
			int count = 0;
			while (st.hasMoreTokens ()) {
				String token = st.nextToken ();
				if (searches.get (token) != null) count++;
			}
			if (count > bestmatch) {
				bestmatch = count;
				bests = i;
			}
			double delta = (double)j/100.0;
			Double D = new Double (-count-delta);
			map.put (D, new Integer (j));
		}
		if (bests >= 0) {
			System.out.println ("Best search question "+questions [bests]+" distance "+bestmatch);
		}
		else {
			System.out.println ("No matches found for search "+searches.toString ());
		}
		return map;
	}

	int findMatchingQuestion (TreeMap <Double, String> matches) {
		if (matches.size () == 0) return 0;
		Double D = matches.firstKey ();
		String question = matches.get (D);
		// find question in the list of questions
		int n = questions.length;
		int found = -1;
		for (int i=0; i<n; i++) {
			if (questions [i].equals (question)) {
				found = i;
				break;
			}
		}
		if (found == -1) return 0;
		else return found;
	}
	
/**
 * Get the Script associated wit this interpreter
 * @return
 */
	public Script getScript () {
		return gen;
	}
/**
 * Get the questions that are recognized by this interpreter. This includes
 * search queries.
 * @return 
 */
	public String[] getQuestions () {
		return questions;
	}
};
