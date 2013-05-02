/*
   Jaivox version 0.4 April 2013
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

import com.jaivox.interpreter.HistNode;
import com.jaivox.interpreter.Info;
import com.jaivox.interpreter.Datanode;
import com.jaivox.interpreter.Utils;
import com.jaivox.util.Log;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

/**
 * This class is useful for handling questions about data. The data here
 * is one that involves some nouns and their attributes. In many applications,
 * questions involving nouns and adjectives (or adverbs) are directed towards
 * getting the most important information.
 *
 * This is somewhat related to optimization in that the questions generally
 * ask for the most important information. This class does not do optimization
 * but instead provides the connection from the qualitified question to a
 * simpler data selection function. This simple function can be replaced with
 * optimization functions in specific applications.
 * @author jaivox
 */

public class cmdProc {

	String data [][];
	String fields [];
	int nr, nf;

	roadCommand rcmd;
	Properties kv;
	Info info;
	Answer ans;
	Datanode Dnode;

	TreeMap <String, Point> quants;
	TreeMap <String, String> questspecs;
	TreeMap <String, String []> details;

	static int
		dType = 0,
		dField = 1,
		dAttribute = 2,
		dQuant = 3,
		dNnp = 4,
		dElse = 5,
		dAdverb = 6,
		nDetail = 7;

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

	String dontknow [];


/**
The interpreter's Script processing component calls roadCommand to run
 * user defined functions. The actual work of formulating answers is
 * done in this class. The constructor below organized data needed to
 * formulate answers.
@param inter
@param inf
 */
	public cmdProc (roadCommand cmd, Properties pp) {
		rcmd = cmd;
		kv = pp;
		String specfile = kv.getProperty ("specs_file");
		String basedir = kv.getProperty ("base_dir");
		info = new Info (basedir, specfile);
		info.showspecs ();
		TreeMap <String, Datanode> dnodes = info.getData ();
		String datafile = kv.getProperty ("data_file");
		Dnode = dnodes.get (datafile);
		Dnode.showData ();
		initializeQuants ();
		String filename = basedir + kv.getProperty ("questions_file");
		loadQuestions (filename);
		String answers = kv.getProperty ("answer_file");
		ans = new Answer (answers);
		intro = ans.intro;
		yesanswers = ans.yesanswers;
		noanswers = ans.noanswers;
		confused = ans.confused;
		followup = ans.followup;
		topics = ans.topics;
		oneitem = ans.oneitem;
		twoitems = ans.twoitems;
		manyitems = ans.manyitems;
		forinstance = ans.forinstance;
		askanother = ans.askanother;
		dontknow = ans.dontknow;
		refreshData ();
	}

/**
 * Determine the range of values that correspond to various adjectives.
 */
	void initializeQuants () {
		quants = new TreeMap <String, Point> ();
		// ranges extended since a superlative is a comparative and
		// both superlative and comparative apply to the adjective
		quants.put ("jjm-ns", new Point (0, 10));
		quants.put ("jjf-ns", new Point (0, 10));
		quants.put ("jjm-nr", new Point (0, 30));
		quants.put ("jjf-nr", new Point (0, 30));
		quants.put ("jjm-n",  new Point (0, 50));
		quants.put ("jjf-n",  new Point (0, 50));
		quants.put ("jjm-p",  new Point (50, 99));
		quants.put ("jjf-p",  new Point (50, 99));
		quants.put ("jjm-pr", new Point (70, 99));
		quants.put ("jjf-pr", new Point (70, 99));
		quants.put ("jjm-ps", new Point (90, 99));
		quants.put ("jjf-ps", new Point (0, 10));
	}

/**
 * Load the questions that can be answered by this program
 * @param filename
 */

	void loadQuestions (String filename) {
		try {
			questspecs = new TreeMap <String, String> ();
			details = new TreeMap <String, String []> ();
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			int count = 0;
			while ((line = in.readLine ()) != null) {
				StringTokenizer st = new StringTokenizer (line, "\t");
				int ntok = st.countTokens ();
				if (ntok < 3) {
					Log.info ("Not enough tokens in "+line);
					continue;
				}
				String q = st.nextToken ().toLowerCase ();
				String s = st.nextToken ();
				String sem = st.nextToken ();
				String detail [] = getSemDetails (sem);
				questspecs.put (q, s);
				// String detail [] = getDetails (q, s);
				details.put (q, detail);
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
 * When specifications are processed, there is some semantic information that
 * is also generated. This function extracts those details into an array called
 * detail, one for each question. All such arrays are stored in a tree map called
 * details.
 * @param sem
 * @return
 */

	String [] getSemDetails (String sem) {
		StringTokenizer st = new StringTokenizer (sem, "(),");
		int ntok = st.countTokens ();
		if (ntok != nDetail) {
			Log.info ("Not enough tokens in "+sem);
			return null;
		}
		String detail [] = new String [nDetail];
		for (int i=0; i<nDetail; i++) {
			detail [i] = st.nextToken ().trim ().toLowerCase ();
		}
		return detail;
	}

	void showDetail (String detail []) {
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<detail.length; i++) {
			sb.append (' ');
			sb.append (i);
			sb.append (':');
			sb.append (detail [i]);
		}
		String s = new String (sb);
		Log.info (s);
	}

	void refreshData () {
		data = Dnode.getData ();
		fields = Dnode.getColumnNames ();
		nr = Dnode.getNrow ();
		nf = Dnode.getNcol ();
	}

/**
 * Create an answer to a question.Called from the user-defined
 * Command class..
@param question
@return
 */

	String [] makeAnswer (String question, Vector<HistNode> history) {
		/*
		String spec = questspecs.get (question);
		if (spec == null) {
			return ("Unknown question "+question);
		}
		String detail [] = getDetails (question, spec);
		*/
		String detail [] = newcopy (details.get (question));
		Log.info ("Question: "+question);
		showDetail (detail);
		showHistory (history);

		String state = currentState (detail);
		Log.info ("currentState = "+state);

		String answer = null;

		if (state.equals ("specified")) {
			answer = generateSimple (question, detail);
		}

		else if (state.equals ("nnpSpecified")) {
			answer = generateSpecific (question, detail);
		}

		else if (state.equals ("elseSpecified")) {
			answer = makeWithHistory (state, question, history);
		}

		else if (state.equals ("elseNnp")) {
			answer = generateSpecificElse (question, detail);
		}

		// situations where specifications have to be inferred from history
		else {
			answer = makeWithHistory (state, question, history);

		}

		String result [] = new String [1];
		result [0] = answer;
		return result;

	}

/**
 * This can be replaced with Script.showHistory, but in this implementation,
 * the cmdProc class does not have a pointer to the Script.
 * @param history
 */
	void showHistory (Vector<HistNode> history) {
		for (int i=0; i<history.size (); i++) {
			HistNode h = history.elementAt (i);
			System.out.println ("History Element "+i+"\n"+h.toString ());
		}
	}


/**
 * Determine the type of question we have, using information from the
 * history of the conversation so far.
@param state
@param p
@return
 */
	String makeWithHistory (String state, String question, Vector<HistNode> history) {
		String answer = null;

		// since the main fields are not specified we have to go back to the history
		// find the last fully specified
		int lastfull = getLastSpecified (history);
		// if no such thing, this is a query with wildcards that does not have any
		// data available to fill the wildcards
		if (lastfull == -1) {
			Log.info ("Should have some some nouns specified after stage "+lastfull);
			state = "notSpecified";
			answer = ans.selectPhrase (confused) + " " + ans.selectPhrase (askanother);
			// answer =  "I can't figure out the answer, can you ask a new question?";
			return answer;
		}

		HistNode hist = history.elementAt (lastfull);
		String userinput = hist.getUserInput ();
		String userspec = questspecs.get (userinput);
		if (userspec == null) {
			return ("Unknown question "+userinput);
		}
		Log.info ("Last full question at "+lastfull+": "+userinput);
		// String detail [] = getDetails (userinput, userspec);
		String detail [] = newcopy (details.get (userinput));

		// resolve the nnp's mentioned since lastfull
		Vector <String> nnps = getUsedNouns (lastfull, history);
		if (nnps == null) {
			// can't figure out
			Log.info ("Can't figure out anything: "+question);
			state = "internalError";
			answer = ans.selectPhrase (confused) + " " + ans.selectPhrase (askanother);
			// answer = "I am confused, could you ask the question a different way?";
			return answer;
		}

		// since FAQ are updated, we can answer some questions
		int nNouns = nnps.size ();
		String snouns [] = nnps.toArray (new String [nNouns]);
		Log.info ("used nouns: "+Utils.makeString (snouns));
		if (nNouns == 0) {
			if (detail [dElse].equals ("_")) {
				answer = generateSimple (question, detail);
			}
			else {
				String temp = generateSimple (question, detail);
				answer = ans.selectPhrase (topics) + " " + temp;
				// answer = "Do not know what else. However, " + temp;
			}
		}
		else if (nNouns == 1) {
			String nnp = nnps.elementAt (0);
			detail [dNnp] = nnp;
			// details.put (question, detail);
			if (detail [dElse].equals ("_")) {
				answer = generateSpecific (question, detail);
			}
			else {
				answer = generateSpecificElse (question, detail);
			}
		}
		else { // more than one noun mentioned
			answer = generateElse (question, detail, nnps);
		}

		return answer;
	}

/**
 * Make a new copy of an array. Could be replaced with clone.
 * @param arr
 * @return
 */

	String [] newcopy (String arr []) {
		int n = arr.length;
		String another [] = new String [n];
		for (int i=0; i<n; i++) {
			another [i] = arr [i];
		}
		return another;
	}

/**
 * Get a state for the purpose of processing questions. These states are
 * not the same as states in the finite state machine defined in the
 * application script.
 * @param detail
 * @return
 */

	String currentState (String detail []) {
		if (!detail [dField].equals ("_") &&
			!detail [dAttribute].equals ("_") &&
			!detail [dQuant].equals ("_")) {
			if (detail [dElse].equals ("_")) {
				if (detail [dNnp].equals ("_")) {
					return "specified";
				}
				else {
					return "nnpSpecified";
				}
			}
			else {
				if (detail [dNnp].equals ("_")) {
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

/**
 * Get the last fully specified question, i.e. where the field, attribute
 * and quantifier in a query are specified. This is used to answer questions
 * where some of these are not specified, for example a question like
 * "what else?"
 * @param history
 * @return
 */

	int getLastSpecified (Vector<HistNode> history) {
		int n = history.size ();
		int lastfull = -1;
		for (int i=n-1; i>=0; i--) {
			HistNode hist = history.elementAt (i);
			String userInput = hist.getUserInput ();
			String olds = questspecs.get (userInput);
			if (olds == null) continue;
			// String detail [] = getDetails (hist.userInput, olds);
			String detail [] = details.get (userInput);
			if (detail [dField].equals ("_") || detail [dAttribute].equals ("_") ||
				detail [dQuant].equals ("_")) {
				continue;
			}
			else {
				Log.info ("Lastfull at "+i);
				showDetail (detail);
				lastfull = i;
				break;
			}
		}
		return lastfull;
	}

/**
 * Get the nouns that have been used in the last few instances. This is used
 * to answer followup questions with new nouns.
 * @param lastfull
 * @param history
 * @return
 */
	Vector <String> getUsedNouns (int lastfull, Vector<HistNode> history) {
		int n = history.size ();
		if (lastfull >= n) {
			Log.warning ("No full specification available to determine unknown variables.");
			return null;
		}
		Vector <String> nnps = new Vector <String> ();
		for (int i=lastfull; i<n; i++) {
			HistNode hist = history.elementAt (i);
			/*
			String olds = questspecs.get (hist.userInput);
			if (olds == null) continue;
			String detail [] = getDetails (hist.userInput, olds);
			*/
			String userInput = hist.getUserInput ();
			String detail [] = details.get (userInput);
			String nnp = detail [dNnp];
			if (!nnp.equals ("") && !nnp.equals ("_")) {
				Log.fine ("At step "+n+" adding nnp from question: "+nnp);
				nnps.add (nnp);
			}
			// get all nnp's in answers
			String oneans = hist.getSystemResponse ();
			for (int j=0; j<nr; j++) {
				if (oneans.indexOf (data [j][0]) != -1) {
					nnps.add (data [j][0]);
					Log.fine ("At step "+n+" adding nnp from answer: "+data [j][0]);
				}
			}
		}
		return nnps;
	}

/**
 * Generate an answer to a direct fully specified query
 * @param query
 * @param detail
 * @return
 */

	String generateSimple (String query, String detail []) {
		try {
			/*
			String olds = questspecs.get (query);
			if (olds == null) return ("Unkown question "+query);
			String detail [] = getDetails (query, olds);
			*/
			// String detail [] = details.get (query);
			String type = detail [dType];
			String field = detail [dField];
			String attribute = detail [dAttribute];
			String quant = detail [dQuant];
			Log.fine ("generateSimple: "+ field+" "+attribute+" "+quant);
			Point pt = quants.get (quant);
			int plow = pt.x;
			int phigh = pt.y;
			int f = findColumn (attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+attribute);
				return "error";
			}
			// String stop = ".";
			String stop = " ";

			// select the relevant data
			boolean negative = false;
			if (quant.toLowerCase ().endsWith ("-n")) negative = true;
			String selection [] = selectOrdering (f, plow, phigh, negative);
			if (selection == null) {
				String problem = ans.selectPhrase (intro) + ans.selectPhrase (noanswers) + stop;
				return problem;
			}

			int count = selection.length;
			String result = "";

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			if (type.equals ("ask")) {
				if (count == 0) {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
				}
				else {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			boolean superlative = false;
			if (quant.toLowerCase ().indexOf ("s-") != 1) superlative = true;
			if (count == 0) {
				result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
			}
			else if (count == 1 || superlative) {
				result = ans.selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = ans.selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] +stop;
			}
			else {
				result = ans.selectPhrase (manyitems) + " " + ans.selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

/**
 * Answer a question involving a specific noun such as what is better than
 * a specific item.
 * @param query
 * @param detail
 * @return
 */

	String generateSpecific (String query, String detail []) {
		try {
			/*
			String olds = questspecs.get (query);
			if (olds == null) return ("Unkown question "+query);
			String detail [] = getDetails (query, olds);
			*/
			// String detail [] = details.get (query);
			String type = detail [dType];
			String field = detail [dField];
			String attribute = detail [dAttribute];
			String quant = detail [dQuant];
			String nnp = detail [dNnp];
			Log.fine ("generateSimple: "+ field+" "+attribute+" "+quant);
			boolean up = true;
			if (quant.toLowerCase ().endsWith ("-n")) up = false;
			int f = findColumn (attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			boolean negative = false;
			if (quant.toLowerCase ().endsWith ("-n")) negative = true;
			String selection [] = selectComparative (f, up, nnp, negative);
			if (selection == null) {
				String problem = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
				return problem;
			}

			int count = selection.length;
			String result = "";

			if (type.equals ("ask")) {
				if (count == 0) {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
				}
				else {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			boolean superlative = false;
			if (quant.toLowerCase ().indexOf ("s-") != 1) superlative = true;
			if (count == 0) {
				result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
			}
			else if (count == 1 || superlative) {
				result = ans.selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = ans.selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] +stop;
			}
			else {
				result = ans.selectPhrase (manyitems) + " " + ans.selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

/**
 * Generate an answer to a question like what else is best besides
 * a specific item.
 * @param query
 * @param detail
 * @return
 */
	String generateSpecificElse (String query, String detail []) {
		try {
			/*
			String olds = questspecs.get (query);
			if (olds == null) return ("Unkown question "+query);
			String detail [] = getDetails (query, olds);
			*/
			// String detail [] = details.get (query);
			String type = detail [dType];
			String field = detail [dField];
			String attribute = detail [dAttribute];
			String quant = detail [dQuant];
			String nnp = detail [dNnp];
			String els = detail [dElse];
			Log.fine ("generateSpecificElse: "+ field+" "+attribute+" "+quant+" "+nnp+" "+els);
			boolean up = true;
			if (quant.toLowerCase ().endsWith ("-n")) up = false;
			int f = findColumn (attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			// select the relevant data
			// String orig [] = selectOrdering (f, plow, phigh);
			boolean negative = false;
			if (quant.toLowerCase ().endsWith ("-n")) negative = true;
			String orig [] = selectComparative (f, up, nnp, negative);
			if (orig == null) {
				String problem = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
				return problem;
			}

			// form selection from the rest
			int total = orig.length;
			Vector <String> hold = new Vector <String> ();
			for (int i=0; i<total; i++) {
				if (orig [i].equals (nnp)) continue;
				hold.add (orig [i]);
			}

			int count = hold.size ();
			String selection [] = new String [count];
			for (int i=0; i<count; i++) {
				selection [i] = hold.elementAt (i);
			}
			String result = "";

			if (type.equals ("ask")) {
				if (count == 0) {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
				}
				else {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			boolean superlative = false;
			if (quant.toLowerCase ().indexOf ("s-") != 1) superlative = true;
			if (count == 0) {
				result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
			}
			else if (count == 1 || superlative) {
				result = ans.selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = ans.selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] + stop;
			}
			else {
				result = ans.selectPhrase (manyitems) + " " + ans.selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

/**
 * Answer a question like "what else"
 * @param query
 * @param detail
 * @param nnps
 * @return
 */

	String generateElse (String query, String detail [], Vector <String> nnps) {
		try {
			/*
			String olds = questspecs.get (query);
			if (olds == null) return ("Unkown question "+query);
			String detail [] = getDetails (query, olds);
			*/
			// String detail [] = details.get (query);
			String type = detail [dType];
			String field = detail [dField];
			String attribute = detail [dAttribute];
			String quant = detail [dQuant];
			Log.fine ("generateElse: "+field+" "+attribute+" "+quant);
			boolean up = true;
			if (quant.toLowerCase ().endsWith ("-n")) up = false;
			int f = findColumn (attribute);
			if (f == -1) {
				Log.warning ("Could not find atrribute "+attribute);
				return "error";
			}

			String stop = " ";

			// select the relevant data
			// String orig [] = selectOrdering (f, plow, phigh);
			String nnp = nnps.elementAt (0);
			boolean negative = false;
			if (quant.toLowerCase ().endsWith ("-n")) negative = true;
			String orig [] = selectComparative (f, up, nnp, negative);
			if (orig == null) {
				String problem = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
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

			if (type.equals ("ask")) {
				if (count == 0) {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
				}
				else {
					result = ans.selectPhrase (intro) + " " + ans.selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count > 0) {
				for (int i=0; i<count; i++) {
					Log.finest ("selection:"+i+" "+selection [i]);
				}
			}

			boolean superlative = false;
			if (quant.toLowerCase ().indexOf ("s-") != 1) superlative = true;

			if (count == 0) {
				result = ans.selectPhrase (intro) + " " + ans.selectPhrase (noanswers) + stop;
			}
			else if (count == 1 || superlative) {
				result = ans.selectPhrase (oneitem) + " " + selection [0] +stop;
			}
			else if (count == 2) {
				result = ans.selectPhrase (twoitems) + " "  + selection [0] + " and "+ selection [1] + stop;
			}
			else {
				result = ans.selectPhrase (manyitems) + " " + ans.selectPhrase (forinstance)
				+ " " + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

/**
 * Find the data column corresponding to a particular column name
 * @param field
 * @return
 */
	int findColumn (String field) {
		for (int i=0; i<nf; i++) {
			if (fields [i].equals (field)) return i;
		}
		return -1;
	}

/**
 * we assume for these qualitative data tables that the first column is the
 * identification and the remaining columns are attributes
 *
 * plow and phigh are between 0 and 99
 * @param f
 * @param plow
 * @param phgih
 * @return
 */

	String [] selectOrdering (int f, int plow, int phigh, boolean negative) {
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
		int n = selection.length;
		if (negative) {
			String rev [] = new String [n];
			for (int i=0; i<n; i++) {
				rev [i] = selection [n-1-i];
			}
			selection = rev;
		}
		showSelection ("selectOrdering", selection);
		return selection;
	}

/**
 * Select answers, but involving a comparison
 * @param f
 * @param up
 * @param nnp
 * @return
 */
	String [] selectComparative (int f, boolean up, String nnp, boolean negative) {
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
		int n = selection.length;
		if (negative) {
			String rev [] = new String [n];
			for (int i=0; i<n; i++) {
				rev [i] = selection [n-1-i];
			}
			selection = rev;
		}
		showSelection ("selectComparative", selection);
		return selection;
	}

	void showSelection (String heading, String selection []) {
		System.out.println ("Selection results:"+heading);
		for (int i=0; i<selection.length; i++) {
			System.out.println (""+i+":\t"+selection [i]);
		}
	}

}

