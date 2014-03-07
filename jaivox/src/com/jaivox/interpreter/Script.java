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

import com.jaivox.tools.QaList;
import com.jaivox.tools.QaNode;
import com.jaivox.util.Log;
import com.jaivox.util.Recorder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Script handles the interpreter's behavior. A script (which may include
 * other scripts) is interpreted based on various conditions. Conditions are
 * described by states. Two states are defined in the code below "initial"
 * is meant to be an initial state and "def" is the default state.
 * @author jaivox
 */

public class Script {

	Interact I;
	Properties kv;
	Command F;
	Control control;
	Vector <HistNode> history;
	boolean Store = false;
	static int quad = 4;
	int nfsm;
	String fsm [][];

	boolean isMatch [];

	TreeMap <String, String> questspecs;
	
/**
 * When an interpreter encounters an error, it tries to produce an error
 * message, generally this is spoken. Since the language of the application is
 * unknown, the string Error is just "?". A synthesizer will say something like
 * "question mark" if this is not replaced, see @errorResult. The Error string
 * is spoken only if there is no value for errorTag in the script.
 */

	public static String Error = "???";
	
/**
 * If the system is not able to handle some situation, it looks for a node
 * with tag errorTag. The system uses this node to produce an error message
 * (in the appropriate language of the script) saying something like "I cannot
 * process your request".
 */
	public static String errorTag = "errortag";
	
/**
 * The initial state is used to create something in the history. This way if
 * an action is supposed to happen at first, it can be defined as having the
 * precondition "initial". If some other state has to be the initial state, then
 * it can be created in the grammar by providing a finite state node with
 * tag "initial" and ending state whatever the user wishes to use as the real
 * starting state.
 */
	public static String initialState = "initial";
	
/**
 * The default state may be used to do actions in most situations not involving
 * the state of the conversation. For example, it may be necessary to evaluate
 * some user-defined function that does not consider the current state, then the
 * state machine can go into the default state and match a state transition
 * that is based on the default state.
 */
	public static String defaultState = "def";
	
/**
 * When a handleExec is called, we record that in history with an ending state
 * that shows something was executed. To prevent runaway recursions of the exec
 * we make a rule that if the last state is an exec, we cannot execute something
 * else immediately afterwards.
 */
	
	public static String execState = "jaivoxexec";
	
/**
 * anystate is to be matched only if nothing else matches. This is used to
 * handle bad matches and desperation statements, where the recognizer is not
 * producing anything useful.
 */
	
	public static String anyState = "zzzzstate";
	
	static String emptyTokens [] = {""};

	QaList List;
	TreeMap <String,QaNode> lookup;

/**
 * Create a new Script handler. Data for this handler is obtained from
 * the Interact class.
 * @param dataholder
 */
	
	public Script (Interact dataholder) {
		I = dataholder;
		F = I.command;
		kv = I.kv;
		control = new Control (this);
		if (kv.getProperty ("store_history") != null) {
			String yes = kv.getProperty ("store_history");
			if (yes.equalsIgnoreCase ("yes")) Store = true;
			else Store = false;
		}
		else Store = false;
		history = new Vector <HistNode> ();
		List = new QaList (I.grammarfile);
		lookup = List.getLookup ();
		createFsm ();
		createDefaultHistoryNode ();
		String questions_file = I.basedir + kv.getProperty ("questions_file");
		loadQuestions (questions_file);
	}

/**
 * Create the finite state machine based on the "grammarfile" variable
 * in the interact class. The finite state machine is stored in an array
 * with four columns and as many rows as there are state transitions.
 */
	void createFsm () {
		Vector <String []> hold = new Vector <String []> ();
		Set <String> keys = lookup.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			if (isState (key)) {
				QaNode node = lookup.get (key);
				String tail [] = node.getTail ();
				// check that size is right
				if (tail.length != quad) {
					Log.severe ("State node "+key+" should have exactly four elements");
					continue;
				}
				hold.add (tail);
			}
		}
		nfsm = hold.size ();
		// fsm = hold.toArray (new String [n][quad]);
		fsm = new String [nfsm][quad];
		for (int i=0; i<nfsm; i++) {
			String f [] = hold.elementAt (i);
			for (int j=0; j<quad; j++) {
				fsm [i][j] = f [j];
			}
		}

		markMatch ();
	}

/**
 * mark the isMatch for fsm's that contain a match predicate
 */
	
	void markMatch () {
		isMatch = new boolean [nfsm];
		for (int i=0; i<nfsm; i++) {
			String input = fsm [i][1];
			String expanded = expandText (input);
			if (hasMatch (expanded)) isMatch [i] = true;
		}
	}

/**
 * create an initial history node. Several methods look at the history to
 * determine details of some questions, it helps to have a non-empty history.
 */
	
	void createDefaultHistoryNode () {
		QaNode initNode = lookup.get (initialState);
		if (initNode == null) {
			createSimpleDefaultNode ();
		}
		else {
			String tail [] = initNode.getTail ();
			// check that size is right
			if (tail.length != quad) {
				Log.severe ("initialSttae node should have exactly four elements");
				createSimpleDefaultNode ();
				return;
			}
			HistNode node = new HistNode (tail, initialState, tail [2]);
			history.add (node);
			if (Store) node.store ();
		}
	}
	
	void createSimpleDefaultNode () {
		String f [] = new String [quad];
		f [0] = f [3] = defaultState;
		f [1] = f [2] = initialState;
		HistNode node = new HistNode (f, initialState, initialState);
		history.add (node);
		if (Store) node.store ();
	}

/**
 * Expand the text below a certain node in the script. This is used for
 * example to see which finite state nodes correspond to states that
 * determine whether we have a good or bad match of the user input with
 * a query recognized by the script. This can also be used to study other
 * properties of the script, such as the location of a particular built
 * in function.
 * @param spec
 * @return
 */
	
	String expandText (String spec) {
		String tokens [] = getTokens (spec);
		if (tokens == null) return "";
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<tokens.length; i++) {
			String token = tokens [i];
			if (isHead (token)) {
				QaNode node = lookup.get (token);
				String tailtext = node.getTail ()[0];
				String result = expandText (tailtext);
				sb.append (" "+result);
				continue;
			}
			else {
				sb.append (" "+token);
				continue;
			}
		}
		String text = new String (sb).trim ();
		return text;
	}

/**
 * Load the questions that are to be understood by the interpreter. Here
 * "question" is just any input by the user, including responses by the
 * user to system queries.
 * @param filename
 */
	
	void loadQuestions (String filename) {
		try {
			questspecs = new TreeMap <String, String> ();
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			int count = 0;
			while ((line = in.readLine ()) != null) {
				int pos = line.indexOf ("\t");
				if (pos == -1) continue;
				StringTokenizer st = new StringTokenizer (line, "\t");
				if (st.countTokens () < 2) continue;
				String q0 = st.nextToken ().trim ().toLowerCase ();
				// remove punctuation from text form of question
				String q1 [] = Utils.splitTokens (q0);
				String q = Utils.makeString (q1);
				String s = st.nextToken ().trim ();
				// there may be additional semantic information in another token
				// specs will have upper case things in them
				if (q.length () == 0 || s.length () == 0) continue;
				questspecs.put (q, s);
				count++;
			}
			in.close ();
			Log.info ("Loaded "+count+" question spec pairs");
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * Get the grammar specification corresponding to a question. The question is
 * something in natural language while the grammar specification has place holders
 * for nouns, adjectives and other parts of speech. The finite state machine and
 * script generally work with the grammar specification.
 * @param question
 * @return
 */
	
	String getSpec (String question) {
		String lower = question.toLowerCase ();
		String spec = questspecs.get (lower);
		if (spec != null) return spec;
		else return Error;
	}

/**
 * When a user input is obtained by Interact, it looks through a list of
 * possible inputs to locate those that are close to the given input.
 * This information is put into a "map" which is sorted with the best
 * matches first. The handleInputValue is then called with this list to
 * obtain a response to the user input.
 * This calls makeAnswer to get the response. makeAnswer returns a string
 * array, which may optionally contain a new state to be set. This generally
 * happens only with a user-defined function, which may decide to set the
 * finite state machine to a state that is different from the state that
 * is given as the output state in the finite state machine. For example,
 * a function may decide that the current line of conversation should not
 * be continued and decide to go to a different location for a different
 * kind of conversation. 
 * @param map
 * @return
 */
	
	public String handleInputValue (TreeMap <Integer, String> map) {
		Log.fine (showLastStates ("handleInputValue"));
		
		if (!control.approves (history)) {
			return errorResult (control.getReason (), map);
		}
		if (!control.addTrack ("handleInputValue/"+map.toString ())) {
			return errorResult (control.getTrackReason (), map);
		}
		Integer firstKey = map.firstKey ();
		if (firstKey == null) {
			Log.severe ("No input in call to handleInput");
			return errorResult (Error, map);
		}
		int firstval = -(firstKey.intValue ());
		String input = map.get (firstKey);
		String spec = getSpec (input);
		if (spec.equals (Error)) {
			Log.severe ("Cannot find spec for matched query "+input);
			return errorResult (input, map);
		}
		if (firstval == 100) {
			String result [] = handleInput (input, map);
			// history already has the new state
			return result [0];
		}
		for (int i=0; i<nfsm; i++) {
			if (!isMatch [i]) continue;
			String statenow = fsm [i][0];
			// initially skip the anystate clauses
			if (statenow.equals (anyState)) continue;
			String tomatch = fsm [i][1];
			String matchtokens [] = getTokens (tomatch);
			String stokens [] = getTokens (spec);
			if (historyMatches (statenow)) {
				Log.fine ("matched: "+i+" "+statenow+ " "+tomatch);
				if (matchNodeValue (firstval, matchtokens, stokens, 0, 0)) {
					Log.fine (tomatch+" at "+i+" matched "+spec);
					String answer [] = makeAnswer (input, fsm [i], map);
					HistNode node = new HistNode (fsm [i], input, answer [0], map);
					if (answer.length > 1) {
						String fnew [] = fsm [i].clone ();
						fnew [3] = answer [1];
						node = new HistNode (fnew, input, answer [0]);
					}
					history.add (node);
					if (Store) node.store ();
					return answer [0];
				}
			}
		}
		// try again by matching anyState
		Log.info ("handleInputValue: Nothing worked, going to anyState match");
		for (int i=0; i<nfsm; i++) {
			String statenow = fsm [i][0];
			// initially skip the anystate clauses
			if (!statenow.equals (anyState)) continue;
			String lastState = defaultState;
			int hn = history.size ();
			if (hn > 0) {
				HistNode lastnode = history.elementAt (hn-1);
				lastState = lastnode.fsmNode [3];
			}
			String tomatch = fsm [i][1];
			String stokens [] = getTokens (spec);
			String matchtokens [] = getTokens (tomatch);
			// doesn't matter if history matches
			if (matchNodeValue (firstval, matchtokens, stokens, 0, 0)) {
				Log.fine (tomatch+" at "+i+" matched "+spec+" with anyState");
				String answer [] = makeAnswer (input, fsm [i], map);
				String fnew [] = fsm [i].clone ();
				fnew [0] = lastState;
				HistNode node = new HistNode (fnew, input, answer [0], map);
				if (answer.length > 1) {
					fnew [3] = answer [1];
					node = new HistNode (fnew, input, answer [0], map);
				}
				history.add (node);
				if (Store) node.store ();
				return answer [0];
			}
		}
		return errorResult (input, map);
	}

/**
 * Once we have decided on the user input, handleInput can deal with
 * creating the response.
 * @param input
 * @param map
 * @return
 */

	public String [] handleInput (String input, TreeMap <Integer, String> map) {
		Log.fine (showLastStates ("handleInput"));
		String spec = getSpec (input);
		String result [] = new String [1];
		if (spec.equals (Error)) {
			Log.severe ("Cannot find spec for matched query "+input);
			result [0] = errorResult (input, null);
			return result;
		}
		/*
		if (!control.approves (history)) {
			result [0] = errorResult (input, null);
			return result;
		}*/
		if (!control.addTrack ("handleInput/"+input+"/"+map.toString ())) {
			return errorResultArray (control.getTrackReason (), map);
		}
		TreeMap <Integer, String> matches = new TreeMap <Integer, String> ();
		matches.put (new Integer (-100), input);
		for (int i=0; i<nfsm; i++) {
			String statenow = fsm [i][0];
			String tomatch = fsm [i][1];
			String stokens [] = getTokens (spec);
			String matchtokens [] = getTokens (tomatch);
			// if (statenow.equals ("om1")) Log.info ("om1/"+tomatch+"/"+fsm[i][2]+"/"+fsm[i][3]);
			if (historyMatches (statenow)) {
				// if (statenow.equals ("om1")) Log.info ("matched: "+statenow+ " "+tomatch);
				if (matchNode (matchtokens, stokens, 0, 0)) {
					Log.fine (tomatch+" at "+i+" matched "+spec);
					String answer [] = makeAnswer (input, fsm [i], matches);
					HistNode node = new HistNode (fsm [i], input, answer [0], map);
					if (answer.length > 1) {
						String fnew [] = fsm [i].clone ();
						fnew [3] = answer [1];
						node = new HistNode (fnew, input, answer [0], map);
					}
					history.add (node);
					if (Store) node.store ();
					return answer;
				}
			}
		}
		// try again by setting statenow to default
		Log.info ("handleInput: Nothing worked, going to default state");
		for (int i=0; i<nfsm; i++) {
			String tomatch = fsm [i][1];
			String stokens [] = getTokens (spec);
			String matchtokens [] = getTokens (tomatch);
			if (historyMatches (defaultState)) {
				Log.fine ("matched: defaultState "+tomatch);
				if (matchNode (matchtokens, stokens, 0, 0)) {
					Log.fine (tomatch+" at "+i+" matched "+spec);
					String answer [] = makeAnswer (input, fsm [i], matches);
					HistNode node = new HistNode (fsm [i], input, answer [0], map);
					if (answer.length > 1) {
						String fnew [] = fsm [i].clone ();
						fnew [3] = answer [1];
						node = new HistNode (fnew, input, answer [0], map);
					}
					history.add (node);
					if (Store) node.store ();
					return answer;
				}
			}
		}
		result [0] = errorResult (input, null);
		return result;
	}

/**
 * produce a response to an input along with an assumed incoming state.
 * This is used to get a response within some larger finite state machine
 * operation, while ignoring the current state of that larger operation.
 * This function is usually called from handling the built=in "exec".
 * @param input
 * @param state
 * @return
 */
	
	String [] handleInputDirect (String input, String state) {
		Log.fine (showLastStates ("handleInputDirect"));
		String spec = getSpec (input);
		String result [] = new String [1];
		if (spec.equals (Error)) {
			Log.severe ("Cannot find spec for matched query "+input);
			result [0] = errorResult (input, null);
			return result;
		}
		/*
		if (!control.approves (history)) {
			result [0] = errorResult (input, null);
			return result;
		}*/
		if (!control.addTrack ("handleInputDirect/"+input+"/"+state)) {
			return errorResultArray (control.getTrackReason (), null);
		}
		TreeMap <Integer, String> matches = new TreeMap <Integer, String> ();
		matches.put (new Integer (-99), input);
		for (int i=0; i<nfsm; i++) {
			String statenow = fsm [i][0];
			String tomatch = fsm [i][1];
			if (!statenow.equals (state)) continue;
			String stokens [] = getTokens (spec);
			String matchtokens [] = getTokens (tomatch);
			if (state != null) {
			// if (historyMatches (state)) {
				// Debug ("handleInputDirect: matched state "+statenow);
				if (matchNode (matchtokens, stokens, 0, 0)) {
					Log.fine ("direct: state "+state+" "+tomatch+" at "+i+" matched "+spec);
					String answer [] = makeAnswer (input, fsm [i], matches);
					// HistNode node = new HistNode (fsm [i], input, answer);
					// history.add (node);
					// if (Store) node.store ();
					return answer;
				}
			}
		}
		result [0] = errorResult (input, null);
		return result;
	}

/**
 * The errorTag is "errortag". A finite state machine should contain a node
 * that provides the system response for an error. This way, the error message
 * sent to the user can be customized to the application and to the language
 * of the conversational dialog.
 * @param input
 * @param map
 * @return
 */
	
	public String errorResult (String input, TreeMap <Integer, String> map) {
		QaNode errorNode = lookup.get (errorTag);
		String errorAnswer = Error;
		if (errorNode != null) errorAnswer = errorNode.pickRandomTail ();
		else Log.fine ("No "+errorTag+" value defined in dialog.");
		String nm [] = new String [quad];
		nm [0] = lastState ();
		nm [1] = input;
		nm [2] = errorAnswer;
		nm [3] = defaultState;
		HistNode hist = new HistNode (nm, "", errorAnswer, map);
		history.add (hist);
		if (Store) hist.store ();
		return errorAnswer;
	}
	
	public String [] errorResultArray (String input, TreeMap <Integer, String> map) {
		QaNode errorNode = lookup.get (errorTag);
		String errorAnswer = Error;
		if (errorNode != null) errorAnswer = errorNode.pickRandomTail ();
		else Log.fine ("No "+errorTag+" value defined in dialog.");
		String nm [] = new String [quad];
		nm [0] = lastState ();
		nm [1] = input;
		nm [2] = errorAnswer;
		nm [3] = defaultState;
		HistNode hist = new HistNode (nm, "", errorAnswer, map);
		history.add (hist);
		if (Store) hist.store ();
		String result [] = new String [1];
		result [0] = errorResult (input, null);
		return result;
	}

/**
 * Any user input has to be matched with a node in the grammar. This is done
 * recursively.
 * @param value
 * @param spec
 * @param tomatch
 * @param kspec
 * @param kmatch
 * @return
 */

	boolean matchNodeValue (int value, String spec [], String tomatch [], int kspec, int kmatch) {
		// Log.finest ("s "+kspec+" "+display(spec)+" m "+kmatch+" "+display(tomatch));
		// Log.finest ("specs:" + display (spec));
		// Log.finest ("match:" + display (tomatch));
		// Log.finest ("\tkspec="+kspec+" kmatch="+kmatch);
		if (kspec >= spec.length) return true;
		String stoken = spec [kspec];
		if (isExpression (stoken)) {
			boolean test = evaluate (stoken, value);
			if (test) return (matchNodeValue (value, spec, tomatch, kspec+1, kmatch));
			else return false;
		}
		else if (isHead (stoken)) {
			QaNode node = lookup.get (stoken);
			String tail [] = node.getTail ();
			int n = spec.length;
			int m = n - (kspec + 1);
			for (int i=0; i<tail.length; i++) {
				String tokens [] = getTokens (tail [i]);
				if (tokens == null) continue;
				// String tokens [] = tail [i].split (" ");
				// Log.finest ("\t\ttail["+i+"] "+display (tokens));
				int l = tokens.length;
				String newtok [] = new String [l+m];
				for (int a=0; a<l; a++) newtok [a] = tokens [a];
				for (int b=0; b<m; b++) newtok [l+b] = spec [kmatch+1+b];
				if (matchNodeValue (value, newtok, tomatch, 0, kmatch)) return true;
			}
			// nothing worked
			return false;
		}
		else {
			if (!stoken.equals (tomatch [kmatch])) return false;
			else return matchNodeValue (value, spec, tomatch, kspec+1, kmatch+1);
		}
	}

/**
 * Another form of matching nodes, this time without considering the value,
 * i.e. the degree of match of the input with questions.
 * @param spec
 * @param tomatch
 * @param kspec
 * @param kmatch
 * @return
 */
	
	boolean matchNode (String spec [], String tomatch [], int kspec, int kmatch) {
		// Log.finest ("s "+kspec+" "+display(spec)+" m "+kmatch+" "+display(tomatch));
		// Log.finest ("specs:" + display (spec));
		// Log.finest ("match: "+display (tomatch));
		// Log.finest ("\tkspec="+kspec+" kmatch="+kmatch);
		if (kmatch >= tomatch.length) return true;
		else if (kspec >= spec.length) return false;
		String stoken = spec [kspec];
		String ttoken = tomatch [kmatch];
		if (isExpression (stoken)) {
			return false;	// no evaluation in this version
		}
		else if (isHead (stoken)) {
			QaNode node = lookup.get (stoken);
			String tail [] = node.getTail ();
			int n = spec.length;
			int m = n - (kspec + 1);
			outer: for (int i=0; i<tail.length; i++) {
				String tokens [] = getTokens (tail [i]);
				if (tokens == null) continue;
				// String tokens [] = tail [i].split (" ");
				// Log.finest ("\t\ttail["+i+"] "+display (tokens));
				int l = tokens.length;
				String newtok [] = new String [l+m];
				for (int a=0; a<l; a++) newtok [a] = tokens [a];
				for (int b=0; b<m; b++) {
					if (kmatch+1+b >= n) continue outer;
					newtok [l+b] = spec [kmatch+1+b];
				}
				if (matchNode (newtok, tomatch, 0, kmatch)) return true;
			}
			// nothing worked
			return false;
		}
		else {
			if (!stoken.equals (tomatch [kmatch])) return false;
			else return matchNode (spec, tomatch, kspec+1, kmatch+1);
		}
	}

/**
 * Get the tokens in a string, keeping strings within a pair of parentheses
 * as single tokens.
 * @param text
 * @return
 */
	String [] getTokens (String text) {
		// split then merge anything inside ( )
		Vector <String> hold = new Vector <String> ();
		if (text == null) return emptyTokens;
		StringTokenizer st = new StringTokenizer (text);
		while (st.hasMoreTokens ()) {
			hold.add (st.nextToken ());
		}
		// merge
		Vector <String> merged = new Vector <String> ();
		int n = hold.size ();
		outer: for (int i=0; i<n; i++) {
			String token = hold.elementAt (i);
			if (token.startsWith ("(")) {
				// find the token ending with )
				int j=i;
				for (; j<n; j++) {
					String test = hold.elementAt (j);
					if (test.endsWith (")")) break;
				}
				if (j == n) {
					Log.severe (token+" +unclosed ()");
					return null; // will be bad anyway
				}
				// combine things from i to j
				StringBuffer sb = new StringBuffer ();
				for (int k=i; k<=j; k++) {
					sb.append (" " + hold.elementAt (k));
				}
				String combined = new String (sb).trim (); //get rid of leading space
				merged.add (combined);
				i = j;
				continue;
			}
			else {
				merged.add (token);
			}
		}
		int m = merged.size ();
		String values [] = merged.toArray (new String [m]);
		return values;
	}

/**
 * This version of historyMatches assumes that we match only the last state
 * in history.
 * @param state
 * @return
 */
	
	boolean historyMatches (String state) {
		// state actually is space separated sequence of states
		String states [] = state.split (" ");
		int m = states.length;
		// if (m == 1 && states [0].equals (defaultState)) return true;
		int n = history.size ();
		/*
		if (m > n) return false;
		int start = n - m;
		for (int i=start, j=0; i<n; i++, j++) {
			HistNode record = history.elementAt (i);
			if (!record.fsmNode [3].equals (states [j])) return false;
		}
		return true;
		*/
		// simpler version matchin only last state
		String last = states [m-1];
		HistNode hist = history.lastElement ();
		if (last.equals (hist.fsmNode [3])) return true;
		else return false;
	}
	
/**
 * Create the response to a particular user input. Here "question" means
 * user input and "answer" means the response to that answer. In a conversation,
 * the user may actually be answering questions from the system.
 * @param question
 * @param fsm
 * @param map
 * @return
 */

	String [] makeAnswer (String question, String fsm [], TreeMap<Integer, String> map) {
		Log.fine ("makeAnswer: "+fsm[0]+" "+fsm[1]+" "+fsm[2]+" "+fsm[3]);
		String rspec = fsm [2];
		Log.fine ("makeAnswer "+question+" / "+rspec);
		if (!control.addTrack ("makeAnswer/"+question+"/"+rspec+"/"+map.toString ())) {
			return errorResultArray (control.getTrackReason (), map);
		}
		String answer [] = generateText (question, rspec, fsm [3], map);
		return answer;
	}
	
/**
 * The actual work of creating responses is done in generateText, called from makeAnswer.
 * @param input
 * @param spec
 * @param state
 * @param map
 * @return
 */

	String [] generateText (String input, String spec, String state, TreeMap<Integer, String> map) {
		String tokens [] = getTokens (spec);
		StringBuffer sb = new StringBuffer ();
		String outstate = state;
		for (int i=0; i<tokens.length; i++) {
			String token = tokens [i];
			if (isExpression (token)) {
				String result [] = handleFunction (input, token, state, map);
				if (result.length > 1) outstate = result [1];
				Log.fine ("generateText:isExpression: "+display (result));
				String gen [] = generateText (input, result [0], state, map);
				sb.append (" "+gen [0]);
				continue;
			}
			else if (isHead (token)) {
				QaNode node = lookup.get (token);
				String tailtext = node.pickRandomTail ();
				String result [] = generateText (input, tailtext, state, map);
				sb.append (" "+result [0]);
				continue;
			}
			else {
				sb.append (" "+token);
				continue;
			}
		}
		String text = new String (sb).trim ();
		Log.fine ("generateText "+input+" / "+spec+" ---> "+text);
		String generated [] = new String [2];
		generated [0] = text;
		generated [1] = outstate;
		if (!outstate.equals (state)) {
			Log.fine ("generateText: Setting state back from "+state+" to "+outstate);
		}
		return generated;
	}

/**
 * Create response from a built-in or user defined function.
 * @param input
 * @param spec
 * @param instate
 * @param map
 * @return
 */
	
	String [] handleFunction (String input, String spec, String instate,
		TreeMap<Integer, String> map) {
		Log.fine ("handleFunction "+input+" / "+spec);
		if (!control.addTrack ("handleFunction/"+input+"/"+spec+"/"+instate+"/"+map.toString ())) {
			return errorResultArray (control.getTrackReason (), map);
		}
		String inside = spec.substring (1, spec.length () -1).trim ();
		// built in functions
		StringTokenizer st = new StringTokenizer (inside);
		String function = st.nextToken ();
		if (function.equals ("exec")) {
			String arg = st.nextToken ().trim ();
			String result [] = handleExec (input, arg, map);
			Log.fine ("handleFunction: input="+input+" result: "+display (result));
			return result;
		}
		else if (function.equals ("quote")) {
			String result [] = handleLastquery (spec, map);
			return result;
		}
		else {
			String result [] = F.handle (function, input, spec, instate, history);
			Log.fine ("handleFunction: function="+function+" input="+input+" result: "+display (result));
			return result;
		}
	}

	// built in functions

/**
 * handle the exec built in function.
 * @param input
 * @param which
 * @param state
 * @param map
 * @return
 */
	
	String [] handleExec (String input, String which, TreeMap<Integer, String> map) {
		Log.fine ("handleExec "+input+" / "+which);
		if (!control.addTrack ("handleExec/"+input+"/"+which+"/"+map.toString ())) {
			return errorResultArray (control.getTrackReason (), map);
			
		}
		/*
		// check the last state, if it is an execState then return error result
		if (history.size () > 0) {
			HistNode hnode = history.lastElement ();
			String lastState = hnode.fsmNode [3];
			if (lastState.equals (execState)) {
				Log.severe ("Recursive exec call "+which);
				String result [] = new String [1];
				result [0] = errorResult (input, null);
				return result;
			}
		}*/
		if (which.equals ("this")) {
			if (map != null) {
				Integer firstkey = map.firstKey ();
				if (firstkey != null) {
					String query = map.get (firstkey);
					if (query != null) {
						// get the state from history, otherwise default
						String state = defaultState;
						if (history.size () > 0) {
							HistNode hnode = history.lastElement ();
							state = hnode.fsmNode [3];
						}
						Log.fine ("executing handleInputDirect/"+state+"/"+query);
						String result [] = handleInputDirect (query, state);
						Log.fine ("result handleInputDirect "+display (result));
						/*
						String fnode [] = new String [4];
						fnode [0] = state;
						fnode [1] = "exec";
						fnode [2] = "this";
						fnode [3] = execState;
						HistNode node = new HistNode (fnode, input, result [0], map);
						history.add (node);
						if (Store) node.store ();
						*/
						return result;
					}
				}
			}
		}
		else if (which.equals ("last")) {
			// pull the last query from the history
			// add this to the history, then handleInput with the
			// pulled query
			int n = history.size ();
			for (int i=n-1; i>=0; i--) {
				HistNode hist = history.elementAt (i);
				// check the map entry in hist
				TreeMap <Integer, String> matches = hist.matches;
				if (matches == null) continue;
				if (matches.size () == 0) continue;
				Integer firstkey = matches.firstKey ();
				if (firstkey == null) continue;
				String query = matches.get (firstkey);
				if (query == null) continue;
				if (query.length () == 0) continue;
				// set state from the history node
				String state = hist.fsmNode [0];
				// remove the particular item from history
				Log.fine ("handleExec calling handleInputDirect "+query);
				String result [] = handleInputDirect (query, state);
				/*
				String fnode [] = new String [4];
				fnode [0] = state;
				fnode [1] = "exec";
				fnode [2] = "last";
				fnode [3] = execState;
				HistNode node = new HistNode (fnode, input, result [0], map);
				history.add (node);
				if (Store) node.store ();
				*/
				return result;
			}
		}
		Log.severe ("exec unimplemented option "+which);
		String errored [] = new String [1];
		errored [0] = errorResult (input, null);
		return errored;
	}
	
/**
 * Built in function to get the last query (useful when handling some mismatches.)
 * @param input
 * @param map
 * @return
 */

	String [] handleLastquery (String input, TreeMap<Integer, String> map) {
		Log.fine ("handleLastQuery "+input);
		if (!control.addTrack ("handleLastQuery/"+input+"/"+map.toString ())) {
			return errorResultArray (control.getTrackReason (), map);
		}
		int n = history.size ();
		// pull the first item in the map
		if (map != null) {
			Integer firstkey = map.firstKey ();
			if (firstkey != null) {
				String query = map.get (firstkey);
				if (query != null) {
					String result [] = new String [1];
					result [0] = query;
					Log.fine ("handleLastQuery returning with current map "+result);
					return result;
				}
			}
		}
		// otherwise go through history?
		for (int i=n-1; i>=0; i--) {
			HistNode hist = history.elementAt (i);
			// check the map entry in hist
			TreeMap <Integer, String> matches = hist.matches;
			if (matches == null) continue;
			if (matches.size () == 0) continue;
			Integer firstkey = matches.firstKey ();
			if (firstkey == null) continue;
			String query = matches.get (firstkey);
			if (query == null) continue;
			if (query.length () == 0) continue;
			String result [] = new String [1];
			result [0] = query;
			Log.fine ("handleLastQuery using history node "+i+" with "+result);
			return result;
		}
		String errored [] = new String [1];
		errored [0] = errorResult (input, null);
		return errored;
	}

/**
 * Select a random phrase from a list of possible phrases; this is useful
 * to introduce some natural variation into system responses.
 * @param phrases
 * @return
 */
	
	String selectPhrase (String [] phrases) {
		int n = phrases.length;
		int selected = (int)(Math.random ()*(double)n);
		if (selected >= n) selected = n-1;
		return phrases [selected];
	}

/**
 * Evaluate an arithmetic expression using the Expression class.
 * @param expression
 * @param value
 * @return
 */
	boolean evaluate (String expression, int value) {
		String sval = ""+value;
		if (expression.indexOf ("match") != -1) {
			String replaced = expression.replaceAll ("match", sval);
			String t = Expression.tokenizeFormula (replaced);
			// Log.finest ("Tokenized "+t);
			// check the tokenization
			if (!evaluatable (t)) return false;
			String p = Expression.postFix (t);
			// Log.finest ("Postfix "+p);
			int v = Expression.evaluate (p);
			// Log.finest ("evaluating "+expression+" value = "+v);
			if (v == 1) return true;
			else return false;
		}
		else {
			Log.info ("Evaluate: only match is implemented as a test");
			return false;
		}
	}

/**
 * Test whether an expression is evaluatable. This is done to make sure
 * there are no undefined variables in the experession.
 * @param t
 * @return
 */
	boolean evaluatable (String t) {
		StringTokenizer st = new StringTokenizer (t);
		while (st.hasMoreTokens ()) {
			String token = st.nextToken ();
			char c = token.charAt (0);
			if (Character.isLetterOrDigit (c)) {
				if (!isNumber (token)) return false;
			}
		}
		return true;
	}
	
/**
 * Test whether the given string is an expression.
 * @param token
 * @return
 */

	public static boolean isExpression (String token) {
		String inside = token.trim ();
		if (inside.startsWith ("(") && inside.endsWith (")")) return true;
		else return false;
	}
	
/**
 * Test to see if an expression contains the word "match" used to see if the
 * matching value fits with the conditions of the expression. This is used for
 * example to determine what constitutes an excellent, good or bad match.
 * @param expression
 * @return
 */

	boolean hasMatch (String expression) {
		StringTokenizer st = new StringTokenizer (expression, "(=<>)+-*&/ \t", true);
		while (st.hasMoreTokens ()) {
			String token = st.nextToken ();
			if (token.equals ("(")) {
				String next = "";
				do {
					next = st.nextToken ();
				} while (next.trim ().length () == 0);
				if (next.equals ("match")) return true;
			}
		}
		return false;
	}
	
/**
 * See if the given string is the head of a grammar node.
 * @param token
 * @return
 */

	boolean isHead (String token) {
		QaNode node = lookup.get (token);
		return (node != null);
	}

/**
 * Function to see if the given string is a number.
 * @param s
 * @return
 */
	public static boolean isNumber (String s) {
		try {
			double x = new Double (s).doubleValue ();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
/**
 * Test whether the given string is a state.
 * @param s
 * @return
 */
	public static boolean isState (String s) {
		String t = s.trim ();
		if (t.startsWith ("[") && t.endsWith ("]"))
			return true;
		else
			return false;
	}

/**
 * Display the contents of an array as a string, used mainly for
 * debugging.
 * @param a
 * @return
 */
	
	String display (String a []) {
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<a.length; i++) {
			sb.append ("-"+a[i]);
		}
		return new String (sb);
	}

/**
 * Show a few last states of the history.
 * @param orig
 * @return
 */
	
	String showLastStates (String orig) {
		StringBuffer sb = new StringBuffer ();
		int start = Math.max (history.size () - 5, 0);
		sb.append (orig);
		for (int i=start; i<history.size (); i++) {
			HistNode h = history.elementAt (i);
			sb.append (" ("+h.fsmNode [0]+" "+h.fsmNode [3]+")");
		}
		String s = new String (sb);
		return s;
	}

/**
 * Show the entire history by printing it to the screen.
 */
	
	public void showHistory () {
		for (int i=0; i<history.size (); i++) {
			HistNode h = history.elementAt (i);
			System.out.println ("History Element "+i+"\n"+h.toString ());
		}
	}

// getters and setters
	
/**
 * Get the Command class that is associated with this script. This Command
 * class will contain user-defined functions.
 * @return
 */
	public Command getCommandClass () {
		return F;
	}

/**
 * Set the Command class containing user-defined functions.
 * @param F
 */
	
	public void setCommandClass (Command F) {
		this.F = F;
	}

/**
 * Get the Interact associated with this Script.
 * @return
 */
	
	public Interact getInteract () {
		return I;
	}

/**
 * Get the grammar stored in a QaList class. Generally we use a
 * TreeMap list of user inputs and corresponding grammar nodes that
 * is stored in this QaList.
 * @return
 */
	
	public QaList getList () {
		return List;
	}

/**
 * Get the string that represents the default state.
 * @return
 */
	public static String getDefaultState () {
		return defaultState;
	}

/**
 * Set the string to be used for default state.
 * @param defaultState
 */
	
	public static void setDefaultState (String defaultState) {
		Script.defaultState = defaultState;
	}

/**
 * Get the tag to be used for a node describing an overall error message.
 * @return
 */
	public static String getErrorTag () {
		return errorTag;
	}

/**
 * Set the tag used in the grammar to specify an overall error message
 * @param errorTag
 */
	public static void setErrorTag (String errorTag) {
		Script.errorTag = errorTag;
	}

/**
 * Get the finite state machine used by this script.
 * @return
 */
	public String[][] getFsm () {
		return fsm;
	}

/**
 * Set the finite state machine used by this script.
 * @param fsm
 */
	public void setFsm (String[][] fsm) {
		this.fsm = fsm;
	}

/**
 * Get the history of execution up to this point.
 * @return
 */
	public Vector<HistNode> getHistory () {
		return history;
	}
	
/**
 * Get the last state in the history
 */
	public String lastState () {
		if (history == null) return defaultState;
		if (history.size () == 0) return defaultState;
		HistNode hist = history.lastElement ();
		String last = hist.fsmNode [3];
		return last;
	}

/**
 * Get the key value pairs of properties used by this script. This identifies
 * locations of various files that may be used by this script.
 * @return
 */
	public Properties getKv () {
		return kv;
	}
	
/**
 * Set the key value properties used here to be a particular Properties
 * instance.
 * @param kv
 */

	public void setKv (Properties kv) {
		this.kv = kv;
	}

/**
 * Get the lookup table in the QaList associated with this script.
 * @return
 */
	public TreeMap<String, QaNode> getLookup () {
		return lookup;
	}

/**
 * Get the table that associates user inputs with their corresponding
 * grammar forms
 * @return
 */
	public TreeMap<String, String> getQuestspecs () {
		return questspecs;
	}

}


