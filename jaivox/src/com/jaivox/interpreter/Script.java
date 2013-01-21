
package com.jaivox.interpreter;

import java.util.*;
import java.awt.Point;

import com.jaivox.util.Log;

/**
 * The Script class is used to generate responses to questions. The
 * interpretation of the questions is done in Interact, while Script is
 * mainly for generating the responses in natural language.
 */

public class Script {

	Interact Act;
	Info Info;

	String data [][];
	String fields [];
	int nr, nf;

	Vector <Qapair> qa;
	TreeMap <String, String> qspecs;

	Answer adata;

	String intro [];

	String yesanswers [];

	String noanswers [];

	String confused [];

	String followup [];

	String topics [];

	String oneitem [];

	String twoitems [];

	String manyitems [];

	String forinstance [];
	
	String askanother [];

	TreeMap <String, Point> quants;
	TreeMap <String, String []> qwords;


	static int nSpecials = 2;		// top values to show

/**
 * Create a script
@param inter
@param inf
 */
	public Script (Interact inter, Info inf, String answerdata) {
		Act = inter;
		Info = inf;
		initializeQuants ();
		adata = new Answer (answerdata);
		intro = adata.intro;
		yesanswers = adata.yesanswers;
		noanswers = adata.noanswers;
		confused = adata.confused;
		followup = adata.followup;
		topics = adata.topics;
		oneitem = adata.oneitem;
		twoitems = adata.twoitems;
		manyitems = adata.manyitems;
		forinstance = adata.forinstance;
		askanother = adata.askanother;
	}

/**
 * Determine the range of values that correspond to various adjectives.
 */
	void initializeQuants () {
		quants = new TreeMap <String, Point> ();

		/*
		quants.put ("jjs-n", new Point ( 0, 10));
		quants.put ("jjr-n", new Point (10, 30));
		quants.put ("jj-n",  new Point (30, 50));
		quants.put ("jj-p",  new Point (50, 70));
		quants.put ("jjr-p", new Point (70, 90));
		quants.put ("jjs-p", new Point (90, 99));
		*/

		// ranges extended since a superlative is a comparative and
		// both superlative and comparative apply to the adjective

		quants.put ("jjs-n", new Point (0, 10));
		quants.put ("jjr-n", new Point (0, 30));
		quants.put ("jj-n",  new Point (0, 50));
		quants.put ("jj-p",  new Point (50, 99));
		quants.put ("jjr-p", new Point (70, 99));
		quants.put ("jjs-p", new Point (90, 99));

		refreshData ();
	}

	void refreshData () {
		data = Info.data;
		fields = Info.fields;
		nr = Info.nr;
		nf = Info.nf;
	}

	void loadhistory () {
		qspecs = Act.qspecs;
		qa = Act.qa;
	}
	
/**
 * Create an answer to a question. Usually called from Inter.
@param question
@return
 */

	String makeAnswer (String question) {
		Log.fine ("makeAnswer: question = "+question);
		loadhistory ();
		String dummyAnswer = "dummy";
		Qapair p = new Qapair (Act, -1, question, dummyAnswer);

		String state = currentState (p);
		Log.fine ("currentState = "+state);

		String answer = null;

		// situations with fully specified question

		if (state.equals ("command")) {
			answer = handleCommand (p);
		}

		else if (state.equals ("specified")) {
			answer = generateSimple (p);
		}

		else if (state.equals ("nnpSpecified")) {
			answer = generateSpecific (p);
		}

		else if (state.equals ("elseSpecified")) {
			// answer = generateSimpleElse (p);
			answer = makeWithHistory (state, p);
		}

		else if (state.equals ("elseNnp")) {
			answer = generateSpecificElse (p);
		}

		// situations where specifications have to be inferred from history
		else {
			answer = makeWithHistory (state, p);

		}
		return answer;

	}

/**
 * Determine the type of question we have, using information from the
 * history of the conversation so far.
@param state
@param p
@return
 */
	String makeWithHistory (String state, Qapair p) {

		Log.fine ("makeWithHistory: state="+state+", Qapair="+p.details ());
		String answer = null;

		// since the main fields are not specified we have to go back to the history
		// find the last fully specified
		int lastfull = getLastSpecified ();
		// if no such thing, this is a query with wildcards that does not have any
		// data available to fill the wildcards
		if (lastfull == -1) {
			Log.info ("Should have some some nouns specified after stage "+lastfull);
			state = "notSpecified";
			answer = selectPhrase (confused) + " " + selectPhrase (askanother);
			// answer =  "I can't figure out the answer, can you ask a new question?";
			return answer;
		}

		Qapair qap = qa.elementAt (lastfull);
		if (!p.updateFAQ (qap)) {
			Log.warning ("Not able to update FAQ from query at position "+lastfull);
			state = "internalError";
		}

		// resolve the nnp's mentioned since lastfull
		Vector <String> nnps = getUsedNouns (lastfull);
		if (nnps == null) {
			// can't figure out
			Log.info ("Can't figure out anything: "+p.question);
			state = "internalError";
			answer = selectPhrase (confused) + " " + selectPhrase (askanother);
			// answer = "I am confused, could you ask the question a different way?";
			return answer;
		}

		// since FAQ are updated, we can answer some questions
		int nNouns = nnps.size ();

		if (nNouns == 0) {
			if (p.els.equals ("")) {
				answer = generateSimple (p);
			}
			else {
				String temp = generateSimple (p);
				answer = selectPhrase (topics) + " " + temp;
				// answer = "Do not know what else. However, " + temp;
			}
		}
		else if (nNouns == 1) {
			String nnp = nnps.elementAt (0);
			p.nnp = nnp;
			if (p.els.equals ("")) {
				answer = generateSpecific (p);
			}
			else {
				answer = generateSpecificElse (p);
			}
		}
		else { // more than one noun mentioned
			answer = generateElse (p, nnps);
		}

		return answer;
	}

	String currentState (Qapair p) {
		if (p.command.equals ("command"))
			return "command";
		if (p.FAQspecified ()) {
			if (p.els.equals ("")) {
				if (p.nnp.equals ("")) {
					return "specified";
				}
				else {
					return "nnpSpecified";
				}
			}
			else {
				if (p.nnp.equals ("")) {
					return "elseSpecified";
				}
				else {
					return "elseNnp";
				}
			}
		}
		else {
			return "useHistory";
		}
	}

	int getLastSpecified () {
		int n = qa.size ();
		int lastfull = -1;
		for (int i=n-1; i>=0; i--) {
			Qapair qap = qa.elementAt (i);
			Log.fine ("qa:"+i+" "+qap.toString ());
			String olds = qap.spec;
			// ignore commands
			if (qap.command.equals ("command")) continue;
			int loc = olds.indexOf ("_");
			if (loc == -1) {
				lastfull = i;
				break;
			}
		}
		return lastfull;
	}

	Vector <String> getUsedNouns (int lastfull) {
		int n = qa.size ();
		if (lastfull >= n) {
			Log.warning ("No full specification available to determine unknown variables.");
			return null;
		}
		Vector <String> nnps = new Vector <String> ();
		for (int i=lastfull; i<n; i++) {
			Qapair qap = qa.elementAt (i);
			// get any nnp from the question specs
			String nnp = qap.nnp;
			if (!nnp.equals ("") && !nnp.equals ("_")) {
				Log.fine ("At step "+n+" adding nnp from question: "+nnp);
				nnps.add (nnp);
			}
			// get all nnp's in answers
			String ans = qap.answer;
			for (int j=0; j<nr; j++) {
				if (ans.indexOf (data [j][0]) != -1) {
					nnps.add (data [j][0]);
					Log.fine ("At step "+n+" adding nnp from answer: "+data [j][0]);
				}
			}
		}
		return nnps;
	}

	String generateSimple (Qapair p) {
		try {
			Log.fine ("generateSimple: "+ p.field+" "+p.attribute+" "+p.quant);
			Point pt = quants.get (p.quant);
			int plow = pt.x;
			int phigh = pt.y;
			int f = findColumn (p.attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+p.attribute);
				return "error";
			}
			// String stop = ".";
			String stop = " ";

			// select the relevant data
			String selection [] = selectOrdering (f, plow, phigh);
			if (selection == null) {
				String problem = selectPhrase (intro) + selectPhrase (noanswers) + stop;
				return problem;
			}

			int count = selection.length;
			String result = "";

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			if (p.command.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + " " + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count == 0) {
				result ="I cannot determine the answer.";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] +stop;
			}
			else {
				result = selectPhrase (manyitems) + " " + selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

	String generateSpecific (Qapair p) {
		try {
			Log.fine ("generateSpecific: "+ p.field+" "+p.attribute+" "+p.quant+" "+p.nnp);
			boolean up = true;
			if (p.quant.toLowerCase ().endsWith ("-n")) up = false;
			int f = findColumn (p.attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+p.attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			String selection [] = selectComparative (f, up, p.nnp);
			if (selection == null) {
				String problem = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				return problem;
			}

			int count = selection.length;
			String result = "";

			if (p.command.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + " " + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			if (count == 0) {
				result ="I cannot seem to come up with the answer.";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] +stop;
			}
			else {
				result = selectPhrase (manyitems) + " " + selectPhrase (forinstance) 
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

	String generateSpecificElse (Qapair p) {
		try {
			Log.fine ("generateSpecificElse: "+ p.field+" "+p.attribute+" "+p.quant+" "+p.nnp+" "+p.els);
			boolean up = true;
			if (p.quant.toLowerCase ().endsWith ("-n")) up = false;
			int f = findColumn (p.attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+p.attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			// select the relevant data
			// String orig [] = selectOrdering (f, plow, phigh);
			String orig [] = selectComparative (f, up, p.nnp);
			if (orig == null) {
				String problem = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				return problem;
			}

			// form selection from the rest
			int total = orig.length;
			Vector <String> hold = new Vector <String> ();
			for (int i=0; i<total; i++) {
				if (orig [i].equals (p.nnp)) continue;
				hold.add (orig [i]);
			}

			int count = hold.size ();
			String selection [] = new String [count];
			for (int i=0; i<count; i++) {
				selection [i] = hold.elementAt (i);
			}
			String result = "";

			if (p.command.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + " " + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			if (count == 0) {
				result ="Seems like I cannot figure out the answer.";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] + stop;
			}
			else {
				result = selectPhrase (manyitems) + " " + selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

	// need to avoid previous answers here
	String generateElse (Qapair p, Vector <String> nnps) {
		try {
			Log.fine ("generateElse: "+p.field+" "+p.attribute+" "+p.quant);
			boolean up = true;
			if (p.quant.toLowerCase ().endsWith ("-n")) up = false;
			int f = findColumn (p.attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+p.attribute);
				return "error";
			}

			String stop = " ";

			// select the relevant data
			// String orig [] = selectOrdering (f, plow, phigh);
			String nnp = nnps.elementAt (0);
			String orig [] = selectComparative (f, up, nnp);
			if (orig == null) {
				String problem = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				return problem;
			}

			// form selection from the rest. avoiding everything in nnp
			int total = orig.length;
			Vector <String> hold = new Vector <String> ();
			outer: for (int i=0; i<total; i++) {
				for (int j=0; j<nnps.size (); j++) {
					nnp = nnps.elementAt (j);
					if (orig [i].equals (nnp)) continue outer;
				}
				hold.add (orig [i]);
			}

			int count = hold.size ();
			String selection [] = new String [count];
			for (int i=0; i<count; i++) {
				selection [i] = hold.elementAt (i);
			}

			String result = "";

			if (p.command.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + " " + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + " " + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			if (count == 0) {
				result ="The answer is not clear.";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] + stop;
			}
			else {
				result = selectPhrase (manyitems) + " " + selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

	int findColumn (String field) {
		for (int i=0; i<fields.length; i++) {
			if (fields [i].equals (field)) return i;
		}
		return -1;
	}

	// we assume for these qualitative data tables that the first column is the
	// identification and the remaining columns are attributes

	// plow and phigh are between 0 and 99

	String [] selectOrdering (int f, int plow, int phigh) {
		// Log.fine ("Select ordering from "+plow+" to "+phigh);
		if (f == 0 || f >=nf) {
			Log.warning ("selectOrdering based on a column > 0 and < nf");
			return null;
		}
		// order the rows
		Point pp [] = new Point [nr];
		for (int i=0; i<nr; i++) {
			int val = (int)(new Double (data [i][f]).doubleValue ()*100.0);
			pp [i] = new Point (i, val);
		}
		Utils.quicksortpointy (pp, 0, nr-1); // increasing order of data [i][f];

		double xnr = (double)nr;
		double low = (double)plow;
		double high = (double)phigh;
		double per = xnr/100.0;
		// Log.fine ("nr="+nr+" xnr="+xnr+" per="+per+" low="+low+" high="+high);
		int start = (int)(per*low);
		int end = (int)(per*high);
		Log.fine ("start at row "+start+" end at row "+end);
		String selection [] = new String [end - start+1];
		if (end -start < 0) return null;
		for (int i=0; i<=end-start; i++) {
			Point p = pp [start+i];
			int j = p.x;
			selection [i] = data [j][0];
		}

		return selection;
	}

	String [] selectComparative (int f, boolean up, String nnp) {
		if (f == 0 || f >=nf) {
			Log.warning ("selectOrdering based on a column > 0 and < nf");
			return null;
		}
		if (nnp.indexOf ("nnp:") != -1) {
			nnp = nnp.substring (4).trim ();
		}
		// order the rows
		Point pp [] = new Point [nr];
		for (int i=0; i<nr; i++) {
			int val = (int)(new Double (data [i][f]).doubleValue ()*100.0);
			pp [i] = new Point (i, val);
		}
		Utils.quicksortpointy (pp, 0, nr-1); // increasing order of data [i][f];
		// find the one with data [i][0] equaling nnp
		int mark = -1;
		for (int i=0; i<nr; i++) {
			Point p = pp [i];
			int j = p.x;
			if (data [j][0].equals (nnp)) {
				mark = i;
				break;
			}
		}

		if (mark == -1) {
			Log.warning ("could not find anything to match "+nnp);
			return null;
		}

		String selection [];
		if (up) {
			selection = new String [nr - mark - 1];
			for (int i=0; i<nr-mark-1; i++) {
				int j = mark + 1 + i;
				Point p = pp [j];
				int k = p.x;
				selection [i] = data [k][0];
			}
		}
		else {
			selection = new String [mark];
			for (int i=0; i<mark; i++) {
				Point p = pp [i];
				int k = p.x;
				selection [i] = data [k][0];
			}
		}
		return selection;
	}

	String selectPhrase (String [] phrases) {
			int n = phrases.length;
			int selected = (int)(Math.random ()*(double)n);
			if (selected >= n) selected = n-1;
			return phrases [selected];
	}

	String confusedAnswer (Semnet net) {
		String start = selectPhrase (confused);
		String follow = selectPhrase (followup);
		String topic = selectPhrase (topics);
		String suggest = net.picktopic (2);
		String result = start  + " "+ follow + " " + topic + " " + suggest;
		return result;
	}

	// command handler
	// currently it does very little, but this is the place to use both
	// the command and the args to do various actions

	public String handleCommand (Qapair p) {
		String command = p.command;
		if (command.equals ("back") || command.equals ("clear") || command.equals ("reset")) {
			Act.clearhistory ();
			return "Memory cleared, please ask another question.";
		}
		else if (command.equals ("end")) {
			Act.qstack.push ("terminate");
			return "terminate";
		}
		else {
			String result = Act.command.handleCommand (p);
			Act.qstack.push (result);
			return result;
		}
	}

}

