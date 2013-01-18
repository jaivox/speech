
/**
 * Each Interact manages a conversation. In a typical application, Interact is asked
 * to execute sentences (generally questions.) The questions can be queries without
 * any context, or it can be a follow up to earlier questions. Interact therefore
 * manages the state of its conversation and uses the state to determine the next
 * action.
 */

package com.jaivox.interpreter;

import java.io.*;
import java.util.*;
import java.awt.Point;

public class Interact {

	Properties kv;

	Utils U;
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
	Vector <String> queries;	// those that were answered
	TreeMap <String, String> qa;
	
	String basedir = "./";
	String specfile = "spec.txt";
	String common = "common.txt";

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
		specfile = kv.getProperty ("specs_file");
		common = basedir + kv.getProperty ("common_words");
		U = new Utils ();
		data = new Info (basedir, specfile);
		if (data.Valid) gen = new Script (this, data);
		else Debug ("Invalid data for Info");
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
		data = new Info (basedir, specfile);
		if (data.Valid) gen = new Script (this, data);
		else Debug ("Invalid data for Info");
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

	void Debug (String s) {
		System.out.println ("[Interact] " +qstate+": "+ s);
	}
	
	void clearhistory () {
		qstate = "initial";
		qstack = new Stack <String> ();
		queries = new Vector <String> ();
		qa = new TreeMap <String, String> ();
	}

/**
 * Terminate the Inract by exiting the system. To be used
 * only when Interact is used from a console application.
 */
	public void terminate () {
		U.endLog ();
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
				/*// use this if words are numbered one per line
				if (text.length () == 0) continue;
				if (text.startsWith ("//")) continue;
				int pos = text.indexOf (". ");
				if (pos == -1) continue;
				String word = text.substring (pos+2).trim ();
				if (word.length () == 0) continue;
				lookup.put (word, "common");
				*/
				StringTokenizer st = new StringTokenizer (line);
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
			File f = new File (basedir);
			String files [] = f.list ();
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
			// Debug ("Reading "+filename);
			BufferedReader in = new BufferedReader (new FileReader (filename));
			int pos = filename.lastIndexOf (".");
			if (pos == -1) pos = filename.length ();
			String stub = filename.substring (0, pos);
			String line;
			while ((line = in.readLine ()) != null) {
				line = line.toLowerCase ();
				pos = line.indexOf ("(");
				if (pos == -1) continue;
				String head = line.substring (0, pos).trim ();
				String spec = line.substring (pos).trim ();
				String lower = head.toLowerCase ();
				qspecs.put (lower, spec);
				String tokens [] = U.splitTokens (lower);
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
		U.record ("Q: "+query);
		String lq = query.toLowerCase ();
		qstack.push (lq);

		String result = "hmm, I don't know ...";

		// state machine
		U.record ("qstate: "+qstate);
		if (qstate.equals ("initial")) {
			qstate = handleInitialQuery ();
			// Debug ("after handleInitialQuery: "+qstack.toString ());
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
				// Debug ("after handleRecognizedQuery: "+qstack.toString ());
				if (qstate.equals ("terminate")) {
					// Debug ("Terminate requested");
					return ("Please enter quit interactively.");
				}
				result = qstack.pop ();
			}
		}
		else if (qstate.equals ("asking_confirmation")) {
			qstate = handleConfirmation ();
			// Debug ("after handleConfirmation: "+qstack.toString ());
			if (qstate.equals ("notconfirmed")) {
				qstate = "initial";
				result = qstack.pop ();
			}
			else if (qstate.equals ("confirmed")) {
				qstate = handleRecognizedQuery ();
				// Debug ("after handleRecognizedQuery: "+qstack.toString ());
				qstate = "initial";
				result = qstack.pop ();
			}
			else if (qstate.equals ("terminate")) {
				// Debug ("Terminate requested");
				return ("Please enter quit interactively.");
			}
		}
		else if (qstate.equals ("terminate")) {
			// Debug ("Terminate requested");
			return ("Please enter quit interactively.");
		}
		U.record ("qstate: "+qstate);
		U.record ("R: "+result);
		// net.execute (result);
		// netprocess (result);
		return result;
	}


	String handleInitialQuery () {
		// Debug ("handleInitialQuery: "+qstack.toString ());
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
		// Debug ("handleRecognizedQuery: "+qstack.toString ());
		String lq = qstack.pop ();
		// queries.add (lq);
		// add the question when we have an answer
		String answer = gen.makeAnswer (lq);
		if (answer.equals ("terminate")) {
			qstate = answer;
			return answer; // that will be the next state
		}
		qa.put (lq, answer);
		String state = "initial";
		net.execute (answer);
		net.updatepasts ();
		qstack.push (answer);
		return state;
	}

	String handleConfirmation () {
		// Debug ("handleConfirmation: "+qstack.toString ());
		String lq = qstack.pop ();
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
				// System.out.println (word + " " +lexicon [best] + " " + bestval);
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
