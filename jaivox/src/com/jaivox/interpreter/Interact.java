/*
   Jaivox version 0.6 December 2013
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

package com.jaivox.interpreter;

import com.jaivox.util.Log;
import com.jaivox.util.Recorder;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
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
	String lexicon [];
	int nl;
	
	public static int MaxMatch = 5;

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
 * The interpreter maintains a Semnet, i.e. a semantic net, of topics
 * that have been discussed. This is used to suggest topics in case the
 * recognizer is not doing too well.
 */
	void initialize () {
		String stub = "Interact";
		String temp = kv.getProperty ("recorder_name");
		if (temp != null) stub = temp;
		Record = new Recorder (basedir + stub);
		specfile = kv.getProperty ("specs_file");
		grammarfile = basedir + kv.getProperty ("grammar_file");
		lookup = new TreeMap <String, String> ();
		multiword = new TreeMap <String, Vector<String>> ();
		common = basedir + kv.getProperty ("common_words");
		if (specfile != null) data = new Info (basedir, specfile);
		gen = new Script (this);
		addcommon ();
		loadquestions ();
		updateLexicon ();
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
		// old method that does not work very well
		// Point pp [] = findBestMatches (in);
		Point pp [] = findBestMatchingSentences (cleaned);
		// standardize goodness
		TreeMap <Integer, String> matches = new TreeMap <Integer, String> ();
		for (int i=0; i<pp.length && i<MaxMatch; i++) {
			Point p = pp [i];
			double d = (double)(p.y);
			int percentage = (int)((n-d)*100.0/n);
			Integer I = new Integer (-percentage);
			String s = questions [p.x];
			matches.put (I, s);
		}
		Log.info (matches.toString ());
		String result = gen.handleInputValue (matches);
		Recorder.record ("A: "+result);
		// gen.control.showTrack ();
		return result;
	}

	Point [] findBestMatchingSentences (String query) {
		int N = questions.length;
		int bestdist = Integer.MAX_VALUE;
		int bestq = -1;
		Point pp [] = new Point [N];
		for (int i=0; i<N; i++) {
			int d = Utils.approxMatch (questions [i], query);
			if (d < bestdist) {
				bestdist = d;
				bestq = i;
			}
			pp [i] = new Point (i, d);
		}
		if (bestq >= 0) {
			Log.info ("Best match question "+questions [bestq]+" distance "+bestdist);
		}
		else {
			Log.info ("No matches found for "+query);
		}
		Utils.quicksortpointy (pp, 0, N-1);
		return pp;
		
	}
	
	Point [] findBestMatches (String in []) {
		int n = in.length;
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<n; i++) {
			String word = in [i];
			int best = -1;
			int bestval = Integer.MAX_VALUE;
			for (int j=0; j<nl; j++) {
				String lex = lexicon [j];
				int d = Utils.editDistance (word, lex);
				if (d < bestval && checkfit (word, lex, d)) {
					best = j;
					bestval = d;
				}
			}
			if (best != -1) {
				sb.append (" "+lexicon [best]);
				Log.finest (word + " " +lexicon [best] + " " + bestval);
			}
		}
		String result = new String (sb);
		Log.info ("Bestmatch words: "+result);
		int N = questions.length;
		int bestdist = Integer.MAX_VALUE;
		int bestq = -1;
		Point pp [] = new Point [N];
		for (int i=0; i<N; i++) {
			int d = Utils.approxMatch (questions [i], result);
			if (d < bestdist) {
				bestdist = d;
				bestq = i;
			}
			pp [i] = new Point (i, d-1);
		}
		if (bestq >= 0) {
			Log.info ("Best match question "+questions [bestq]+" distance "+bestdist);
		}
		else {
			Log.info ("No matches found for "+result);
		}
		Utils.quicksortpointy (pp, 0, N-1);
		return pp;
	}

	boolean checkfit (String a, String b, int d) {
		int n = a.length ();
		int m = b.length ();
		int x = Math.min (n, m);
		if (d > x/2) return false;
		return true;
	}
	
/**
 * Get the Script associated wit this interpreter
 * @return
 */
	public Script getScript () {
		return gen;
	}

};
