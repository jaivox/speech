
package com.jaivox.interpreter;

import java.io.*;
import java.util.*;
import java.awt.Point;

import com.jaivox.util.*;

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

	Utils U;
	Recorder Record;
	TreeMap <String, String> lookup;
	TreeMap <String, Vector<String>> multiword;
	String lexicon [];
	int nl;

	TreeMap <String, String> qspecs;
	String questions [];

	Semnet net;
	Info data;
	Script gen;

	Stack <String> qstack;
	String qstate;
	Vector <Qapair> qa;
	
	String basedir = "./";
	String specfile = "spec.txt";
	String common = "common.txt";
	String answer = "answer.txt";

/**
 * Interact initialize all other classes used in the conversation.
 * This version permits the user to set the basedir and specfile
 * in a location differnet from the default location.
@param base	Directory containing all data required by the applcation
@param spec	specifications file, in the base directory
 */
	public Interact (String base, Properties pp) {
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
		specfile = kv.getProperty ("specs_file");
		common = basedir + kv.getProperty ("common_words");
		U = new Utils ();
		String stub = "Interact";
		String temp = kv.getProperty ("recorder_name");
		if (temp != null) stub = temp;
		Record = new Recorder (stub);
		data = new Info (basedir, specfile);
		answer = basedir + kv.getProperty ("answer_forms");
		if (data.Valid) gen = new Script (this, data, answer);
		else Log.warning ("Invalid data for Info");
		net = new Semnet (basedir, data, kv);
		// net.shownodes ();
		lookup = new TreeMap <String, String> ();
		multiword = new TreeMap <String, Vector<String>> ();
		addcommon ();
		addnetnodes ();
		loadfiles (); // to load any .dat files
		updateLexicon ();
		updatequestions ();
		clearhistory ();
	}

/**
 * Interact initializes all the other classes used in the conversation
 * (Info, Script and Sement.) It also loads all the data located in the
 * basedir location. This version assumes that the basedir
 * and specfile are declared above in the right locations since
 * Info is loaded from there.
 */
	
	public Interact () {
		U = new Utils ();
		Record = new Recorder ("interpreter");
		data = new Info (basedir, specfile);
		answer = basedir + kv.getProperty ("answer_forms");
		if (data.Valid) gen = new Script (this, data, answer);
		else Log.warning ("Invalid data for Info");
		net = new Semnet (basedir, data, kv);
		// net.shownodes ();
		lookup = new TreeMap <String, String> ();
		multiword = new TreeMap <String, Vector<String>> ();
		addcommon ();
		addnetnodes ();
		loadfiles (); // to load any .dat files
		updateLexicon ();
		updatequestions ();
		clearhistory ();
	}

	void clearhistory () {
		qstate = "initial";
		qstack = new Stack <String> ();
		qa = new Vector <Qapair> ();
	}

/**
 * Terminate the Interact by exiting the system. To be used
 * only when Interact is used from a console application.
 */
	public void terminate () {
		Recorder.endRecord ();
		System.exit (1);
	}

	void addnetnodes () {
		TreeMap <String, Snode> nodes = net.nodes;
		Set <String> keys = nodes.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Snode node = nodes.get (key);
			if (node.t.equals ("data")) {
				// see if it has spaces in it
				StringTokenizer st = new StringTokenizer (key);
				if (st.countTokens () > 1) {
					while (st.hasMoreTokens ()) {
						String token = st.nextToken ();
						Vector<String> vals = multiword.get (token);
						if (vals == null) vals = new Vector <String> ();
						vals.add (key);
						multiword.put (token, vals);
						lookup.put (token, "multi");
					}
				}
				lookup.put (key, "data");
			}
			else lookup.put (key, "net");
		}
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

	void loadfiles () {
		try {
			qspecs = new TreeMap <String, String> ();
			String questions = kv.getProperty ("questions_file");
			loadfile (basedir + questions);
			updateLexicon ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void loadfile (String filename) {
		try {
			Log.fine ("Reading "+filename);
			BufferedReader in = new BufferedReader (new FileReader (filename));
			int pos = filename.lastIndexOf (".");
			if (pos == -1) pos = filename.length ();
			String stub = filename.substring (0, pos);
			String line;
			while ((line = in.readLine ()) != null) {
				String lower = line.toLowerCase ();
				pos = line.indexOf ("(");
				if (pos == -1) continue;
				String head = lower.substring (0, pos).trim ();
				String spec = line.substring (pos).trim ();
				StringTokenizer st = new StringTokenizer (spec, "(), \t\r\n");
				String first = st.nextToken ();
				if (!first.equals ("command")) {
					spec = lower.substring (pos).trim ();
				}
				qspecs.put (head, spec);
				String tokens [] = U.splitTokens (head);
				int n = tokens.length;
				if (n > 0) {
					for (int i=0; i<n; i++) {
						lookup.put (tokens [i], stub);
					}
				}
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
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

	void updatequestions () {
		Set <String> keys = qspecs.keySet ();
		int n = qspecs.size ();
		questions = new String [n];
		int i = 0;
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			questions [i++] = key;
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
		qstack.push (lq);

		String result = "hmm, I don't know ...";

		// state machine
		if (qstate.equals ("initial")) {
			qstate = handleInitialQuery ();
			// Log.fine ("after handleInitialQuery: "+qstack.toString ());
			if (qstate.equals ("question_unrecognized")) {
				result = qstack.pop ();
				qstate = "initial";
			}
			else if (qstate.equals ("confirm_question")) {
				result = qstack.pop ();
				qstate = "asking_confirmation";
			}
			else if (qstate.equals ("recognized")) {
				qstate = handleRecognizedQuery ();
				// Log.fine ("after handleRecognizedQuery: "+qstack.toString ());
				if (qstate.equals ("terminate")) {
					// Log.fine ("Terminate requested");
					return ("Please enter quit interactively.");
				}
				result = qstack.pop ();
			}
		}
		else if (qstate.equals ("asking_confirmation")) {
			qstate = handleConfirmation ();
			// Log.fine ("after handleConfirmation: "+qstack.toString ());
			if (qstate.equals ("notconfirmed")) {
				qstate = "initial";
				result = qstack.pop ();
			}
			else if (qstate.equals ("confirmed")) {
				qstate = handleRecognizedQuery ();
				// Log.fine ("after handleRecognizedQuery: "+qstack.toString ());
				qstate = "initial";
				result = qstack.pop ();
			}
			else if (qstate.equals ("terminate")) {
				// Log.fine ("Terminate requested");
				return ("Please enter quit interactively.");
			}
		}
		else if (qstate.equals ("terminate")) {
			// Log.fine ("Terminate requested");
			return ("Please enter quit interactively.");
		}
		Recorder.record ("R: "+result);
		// net.execute (result);
		// netprocess (result);
		return result;
	}


	String handleInitialQuery () {
		Log.fine ("handleInitialQuery: "+qstack.toString ());
		String lq = qstack.pop ();
		String qq [] = U.splitTokens (lq);
		Point match = findBestMatch (qq);
		if (match.x == -1) {
			String state = "question_unrecognized";
			// String response = "I can not understand your question, please try asking another way";
			String response = gen.confusedAnswer (net);
			qstack.push (lq);
			qstack.push (response);
			return state;
		}
		String matchedQuery = questions [match.x];
		int error = match.y;
		if (error < 3) {
			String state = "recognized";
			qstack.push (matchedQuery);
			return state;
		}
		else {
			String state = "confirm_question";
			String response = "Was your question "+matchedQuery;
			qstack.push (matchedQuery);
			qstack.push (response);
			return state;
		}
	}

	String handleRecognizedQuery () {
		Log.fine ("handleRecognizedQuery: "+qstack.toString ());
		String lq = qstack.pop ();
		// queries.add (lq);
		// add the question when we have an answer
		String answer = gen.makeAnswer (lq);
		if (answer.equals ("terminate")) {
			qstate = answer;
			return answer; // that will be the next state
		}
		Qapair qap = new Qapair (this, qa.size (), lq, answer);
		qa.add (qap);
		String state = "initial";
		net.execute (answer);
		net.updatepasts ();
		qstack.push (answer);
		return state;
	}

	String handleConfirmation () {
		Log.fine ("handleConfirmation: "+qstack.toString ());
		String lq = qstack.pop ();
		Qapair qap = new Qapair (this, qa.size (), lq, "");
		if (qap.command.equals ("command")) {
			if (qap.arg.equals ("yes")) {
				String state = "confirmed";
				return state;
			}
			else if (qap.arg.equals ("no")) {
				String state = "notconfirmed";
				String response = gen.confusedAnswer (net);
				qstack.push (lq);
				qstack.push (response);
				return state;
			}
		}
		if (lq.indexOf ("yes") != -1 || lq.indexOf ("yeah") != -1) {
			String state = "confirmed";
			return state;
		}
		else {
			String state = "notconfirmed";
			// String response = "Ok, perhaps you can ask the question another way";
			String response = gen.confusedAnswer (net);
			qstack.push (lq);
			qstack.push (response);
			return state;
		}
	}

	Point findBestMatch (String in []) {
		int n = in.length;
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<n; i++) {
			String word = in [i];
			int best = -1;
			int bestval = Integer.MAX_VALUE;
			for (int j=0; j<nl; j++) {
				String lex = lexicon [j];
				int d = U.editDistance (word, lex);
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
		int bestq = -1;
		int bestqd = Integer.MAX_VALUE;
		for (int i=0; i<questions.length; i++) {
			int d = U.approxMatch (questions [i], result);
			if (d < bestqd) {
				bestq = i;
				bestqd = d;
			}
		}
		Point p = new Point (bestq, bestqd);
		return p;
	}

	boolean checkfit (String a, String b, int d) {
		int n = a.length ();
		int m = b.length ();
		int x = Math.min (n, m);
		if (d > x/2) return false;
		return true;
	}
};
