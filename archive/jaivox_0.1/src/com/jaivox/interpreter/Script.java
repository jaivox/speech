/**
 * The Script class is used to generate responses to questions. The
 * interpretation of the questions is done in Interact, while Script is
 * mainly for generating the responses in natural language.
 */

package com.jaivox.interpreter;

import java.util.*;
import java.awt.Point;

public class Script {

	Interact Act;
	Info Info;

	String data [][];
	String fields [];
	String categories [];
	int nr, nf;

	TreeMap <String, String> qspecs;
	Vector <String> queries;
	TreeMap <String, String> qa;

	static String intro [] = {
		"I guess ",
		"It could be ",
		"It looks like ",
		"It seems like ",
		"Apparently ",
		"This may be "
	};

	String yesanswers [] = {"that is the case", "the answer is yes", "that is true"};
	String noanswers [] = {"that is not the case", "the answer is no", "that is false"};

	String confused [] = {"I cannot understand your question ",
		"I am unable to figure it out ",
		"Sorry about being so dense ",
		"Hmm, may be I am not figuring it out right, ",
		"Really sorry about this, "
	};

	String followup [] = {"can you ask a different question ",
		"can you ask this another way ",
		"is there another way to ask what you need ",
		"perhaps you can reformulate the question "
	};

	String topics [] = {"I recall we were talking about ",
		"may be there is something related to ",
		"is your question about ",
		"could it be that you are asking about "
	};

	String oneitem [] = {"there is only one such, ", "there is exactly one answer, ",
		"Only one answer fits, ", "one solution, ", "there is one match, " };

	String twoitems [] = {"there are two such, ", "there are exactly two answers, ",
		"two answers may fit, ", "two solutions, ", "there are two matches, " };

	String manyitems [] = {"there are several solutions, ", "there are many answers, ",
		"many ways to answer this, ", "there are quite a few solutions, ",
		"there are several matches, "};

	String forinstance [] = {"for example ", "for instance ", "such as ", "as an example ",
		"one of them is ", "one such is ", "one such example is "};


	TreeMap <String, Point> quants;
	TreeMap <String, String []> qwords;


	static int nSpecials = 2;		// top values to show

	public Script (Interact inter, Info inf) {
		Act = inter;
		Info = inf;
		initializeQuants ();
	}

	void Debug (String s) {
		System.out.println ("[Script]" + s);
	}

	void initializeQuants () {
		quants = new TreeMap <String, Point> ();

		/*
		quants.put ("JJS-N", new Point ( 0, 10));
		quants.put ("JJR-N", new Point (10, 30));
		quants.put ("JJ-N",  new Point (30, 50));
		quants.put ("JJ-P",  new Point (50, 70));
		quants.put ("JJR-P", new Point (70, 90));
		quants.put ("JJS-P", new Point (90, 99));
		*/

		quants.put ("jjs-n", new Point ( 0, 10));
		quants.put ("jjr-n", new Point (10, 30));
		quants.put ("jj-n",  new Point (30, 50));
		quants.put ("jj-p",  new Point (50, 70));
		quants.put ("jjr-p", new Point (70, 90));
		quants.put ("jjs-p", new Point (90, 99));

		refreshData ();
	}

	void refreshData () {
		data = Info.data;
		fields = Info.fields;
		categories = Info.categories;
		nr = Info.nr;
		nf = Info.nf;
	}

	void loadhistory () {
		qspecs = Act.qspecs;
		queries = Act.queries;
		qa = Act.qa;
	}

/**
 * Create an answer in natural language in response to a specific question
@param question
@return
 */
	
	public String makeAnswer (String question) {
		loadhistory ();

		String spec = qspecs.get (question).toLowerCase ();
		// Debug ("Getting answer from: "+spec+" for question:"+question);

		// take the spec apart
		String command = "";
		String arg = "";
		String field = "";
		String attribute = "";
		String quant = "";
		String nnp = "";
		String els = "";
		StringTokenizer st = new StringTokenizer (spec, "(),\r\n");

		command = st.nextToken ().trim ();
		if (command.equals ("command")) {
			arg = st.nextToken ().trim ();
			String result = handleCommand (arg);
			return result;
		}

		if (st.hasMoreTokens ()) field = st.nextToken ().trim ();
		if (st.hasMoreTokens ()) attribute = st.nextToken ().trim ();
		if (st.hasMoreTokens ()) quant = st.nextToken ().trim ();
		if (st.hasMoreTokens ()) nnp = st.nextToken ().trim ();
		if (st.hasMoreTokens ()) els = st.nextToken ().trim ();

		if (!nnp.equals ("")) {
			int pos = nnp.indexOf (":");
			if (pos != -1) {
				nnp = nnp.substring (pos+1).trim ();
			}
		}

		// handle various situations

		// redirects
		// if (command.equals ("command")) {
		// }

		// simple selection
		if (FAQspecified (field, attribute, quant) &&	nnp.equals ("") && els.equals ("")) {
			// Debug ("Simple question: "+spec);
			// clear the history?
			Act.clearhistory ();
			loadhistory ();
			String answer = generateSimple (command, field, attribute, quant);
			qa.put (question, answer);
			queries.add (question);
			return answer;
		}

		// selection with a proper name specified
		if (FAQspecified (field, attribute, quant) &&	!nnp.equals ("") && els.equals ("")) {
			// Debug ("Question with comparison "+spec);
			// clear the history?
			Act.clearhistory ();
			loadhistory ();
			String answer = generateSpecific (command, field, attribute, quant, nnp);
			qa.put (question, answer);
			queries.add (question);
			return answer;
		}

		// use history to fill in field, attribute, quant and nnp if they are
		// blank

		int n = queries.size ();
		int m = Math.max (0, n-3);	// don't go back further
		Vector <String> nnps = new Vector <String> ();
		for (int i=n-1; i>=m; i--) {
			String oldq = queries.elementAt (i);
			String oldspec = qspecs.get (oldq);
			if (oldspec.startsWith ("(dir")) continue;
			String oldans = qa.get (oldq);
			st = new StringTokenizer (oldspec, "(),\r\n");
			String oldcmd = st.nextToken ();
			if (oldcmd.equals ("command")) continue;
			String oldfield = "";
			String oldattr = "";
			String oldquant = "";
			String oldnnp = "";
			String oldels = "";
			if (st.hasMoreTokens ()) oldfield = st.nextToken ().trim ();
			if (st.hasMoreTokens ()) oldattr = st.nextToken ().trim ();
			if (st.hasMoreTokens ()) oldquant = st.nextToken ().trim ();
			if (st.hasMoreTokens ()) oldnnp = st.nextToken ().trim ();
			if (st.hasMoreTokens ()) oldels = st.nextToken ().trim ();

			// patch field, attr etc if they are blank
			if (field.equals ("_") && !oldfield.equals ("")) field = oldfield;
			if (attribute.equals ("_") && !oldattr.equals ("")) attribute = oldattr;
			if (quant.equals ("_") && !oldquant.equals ("")) quant = oldquant;
			if (nnp.equals ("_") && !oldnnp.equals ("")) {
				nnp = oldnnp;
				// Debug ("Adding nnp from question: "+nnp);
				nnps.add (nnp);
			}
			// if (els.equals ("_") && !oldels.equals ("")) els = oldels;

			// check the answers to see if any of the data fields were in there
			String ans = qa.get (oldq);
			if (ans == null) continue;
			// Debug ("old question: "+oldq);
			// Debug ("old answer: "+ans);
			// nnps should contain all question and answer specifics
			for (int j=0; j<nr; j++) {
				if (ans.indexOf (data [j][0]) != -1) {
					// nnp = data [j][0];
					nnps.add (data [j][0]);
					// Debug ("Adding nnp from answer: "+data [j][0]);
				}
			}
		}

		if (nnps.size () == 1) nnp = nnps.elementAt (0);
		if (!nnp.equals ("")) {
			int pos = nnp.indexOf (":");
			if (pos != -1) {
				nnp = nnp.substring (pos+1).trim ();
			}
		}

		String newspec = "("+command+", "+field+", "+attribute+", "+quant;
		if (!nnp.equals ("")) newspec = newspec +", "+nnp;
		if (!els.equals ("")) newspec = newspec +", "+els;
		newspec = newspec + ")";

		// simple selection after using history
		if (FAQspecified (field, attribute, quant) && nnp.equals ("") && els.equals ("")) {
			// Debug ("Using history, no nnp: "+newspec);
			// qspecs.put (question, newspec);
			String answer = generateSimple (command, field, attribute, quant);
			qa.put (question, answer);
			queries.add (question);
			return answer;
		}

		// selection with a proper name specified after using history
		if (FAQNspecified (field, attribute, quant, nnp) && els.equals ("")) {
			// Debug ("Using history with nnp: "+newspec);
			// qspecs.put (question, newspec);
			String answer = generateSpecific (command, field, attribute, quant, nnp);
			qa.put (question, answer);
			queries.add (question);
			return answer;
		}

		// if there is an els clause
		// even if the what of the else is not mentioned, use history to figure out
		// the what, then select the else
		if (FAQNspecified (field, attribute, quant, nnp) && !els.equals ("")) {
			// Debug ("Figuring else from: "+newspec);
			// qspecs.put (question, newspec);
			String answer = "";
			if (nnps.size () == 1) {
				answer = generateSpecificElse (command, field, attribute, quant, nnp);
			}
			else {
				answer = generateElse (command, field, attribute, quant, nnps);
			}
			qa.put (question, answer);
			queries.add (question);
			return answer;
		}

		// can't figure out
		// Debug ("Can't figure out anything: "+question+" "+newspec);
		String result =  "I can't figure out the answer, can you ask a new question?";
		return result;
	}

	boolean FAQspecified (String field, String attribute, String quant) {
		if (field.equals ("")) return false;
		if (field.equals ("_")) return false;
		if (attribute.equals ("")) return false;
		if (attribute.equals ("_")) return false;
		if (quant.equals ("")) return false;
		if (quant.equals ("_")) return false;
		return true;
	}

	boolean FAQNspecified (String field, String attribute, String quant, String nnp) {
		if (!FAQspecified (field, attribute, quant)) return false;
		if (nnp.equals ("")) return false;
		if (nnp.equals ("_")) return false;
		return true;
	}

	String generateSimple (String cmd, String field, String attribute, String quant) {
		try {
			String dbname = field+"_"+attribute+"_"+quant;
			Point p = quants.get (quant);
			int plow = p.x;
			int phigh = p.y;
			int f = findColumn (attribute);
			if (f == -1) {
				Debug ("Could not find atrribute "+attribute);
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
			
			/*
			if (count > 0) {
				for (int i=0; i<count; i++) {
					System.out.println ("selection:"+i+" "+selection [i]);
				}
			}
			 */
			
			if (cmd.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			if (count == 0) {
				result ="something hard to determine";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems)  + selection [0] + " and "+ selection [1] +stop;
			}
			else {
				result = selectPhrase (manyitems) + selectPhrase (forinstance) + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

	String generateSpecific (String cmd, String field, String attribute, String quant, String nnp) {
		try {
			String dbname = field+"_"+attribute+"_"+quant+"_nnp";
			boolean up = true;
			if (quant.endsWith ("-N")) up = false;
			int f = findColumn (attribute);
			if (f == -1) {
				Debug ("Could not find atrribute "+attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			String selection [] = selectComparative (f, up, nnp);
			if (selection == null) {
				String problem = selectPhrase (intro) + selectPhrase (noanswers) + stop;
				return problem;
			}

			int count = selection.length;
			String result = "";

			if (cmd.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			/*
			if (count > 0) {
				for (int i=0; i<count; i++) {
					System.out.println ("selection:"+i+" "+selection [i]);
				}
			}
			*/

			if (count == 0) {
				result ="something hard to determine";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems)  + selection [0] + " and "+ selection [1] +stop;
			}
			else {
				result = selectPhrase (manyitems) + selectPhrase (forinstance) + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}

	String generateSpecificElse (String cmd, String field, String attribute, String quant, String nnp) {
		try {
			String dbname = field+"_"+attribute+"_"+quant;
			boolean up = true;
			if (quant.endsWith ("-N")) up = false;
			int f = findColumn (attribute);
			if (f == -1) {
				Debug ("Could not find atrribute "+attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			// select the relevant data
			// String orig [] = selectOrdering (f, plow, phigh);
			String orig [] = selectComparative (f, up, nnp);
			if (orig == null) {
				String problem = selectPhrase (intro) + selectPhrase (noanswers) + stop;
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

			if (cmd.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			/*
			if (count > 0) {
				for (int i=0; i<count; i++) {
					System.out.println ("selection:"+i+" "+selection [i]);
				}
			}
			*/
			
			if (count == 0) {
				result ="there is no other solution";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems)  + selection [0] + " and "+ selection [1] + stop;
			}
			else {
				result = selectPhrase (manyitems) + selectPhrase (forinstance) + selection [0] +stop;
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}


	String generateElse (String cmd, String field, String attribute, String quant, Vector<String> nnps) {
		try {
			String dbname = field+"_"+attribute+"_"+quant;
			boolean up = true;
			if (quant.endsWith ("-N")) up = false;
			int f = findColumn (attribute);
			if (f == -1) {
				Debug ("Could not find atrribute "+attribute);
				return "error";
			}

			// String stop = ".";
			String stop = " ";

			// select the relevant data
			// String orig [] = selectOrdering (f, plow, phigh);
			String nnp = nnps.elementAt (0);
			String orig [] = selectComparative (f, up, nnp);
			if (orig == null) {
				String problem = selectPhrase (intro) + selectPhrase (noanswers) + stop;
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

			if (cmd.equals ("ask")) {
				if (count == 0) {
					result = selectPhrase (intro) + selectPhrase (noanswers) + stop;
				}
				else {
					result = selectPhrase (intro) + selectPhrase (yesanswers) + stop;
				}
				return result;
			}

			/*
			if (count > 0) {
				for (int i=0; i<count; i++) {
					System.out.println ("selection:"+i+" "+selection [i]);
				}
			}
			*/

			if (count == 0) {
				result ="there is no other solution";
			}
			else if (count == 1) {
				result = selectPhrase (oneitem) + selection [0] +stop;
			}
			else if (count == 2) {
				result = selectPhrase (twoitems)  + selection [0] + " and "+ selection [1] + stop;
			}
			else {
				result = selectPhrase (manyitems) + selectPhrase (forinstance) + selection [0] +stop;
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
		// Debug ("Select ordering from "+plow+" to "+phigh);
		if (f == 0 || f >=nf) {
			Debug ("selectOrdering based on a column > 0 and < nf");
			return null;
		}
		// order the rows
		Point pp [] = new Point [nr];
		for (int i=0; i<nr; i++) {
			int val = (int)(new Double (data [i][f]).doubleValue ()*100.0);
			pp [i] = new Point (i, val);
		}
		Utils.quicksortpointy (pp, 0, nr-1); // increasing order of data [i][f];
		/*
		Debug ("After sorting rows");
		for (int i=0; i<nr; i++) {
			int j = pp [i].x;
			System.out.print (""+i+":"+data [j][0]+" ");
		}
		System.out.println ();
		*/

		double xnr = (double)nr;
		double low = (double)plow;
		double high = (double)phigh;
		double per = xnr/100.0;
		// Debug ("nr="+nr+" xnr="+xnr+" per="+per+" low="+low+" high="+high);
		int start = (int)(per*low);
		int end = (int)(per*high);
		// Debug ("start at row "+start+" end at row "+end);
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
			Debug ("selectOrdering based on a column > 0 and < nf");
			return null;
		}
		if (nnp.indexOf ("nnp:") != -1) {
			int pos = nnp.indexOf ("nnp:");
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
			Debug ("could not find anything to match "+nnp);
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
		String result = start + follow + topic + suggest;
		return result;
	}

	// command handler
	String handleCommand (String arg) {
		/*
		if (arg.equals ("yes")) {
			Act.qstack.push ("yes");
			String result = Act.handleConfirmation ();
			return result;
		}
		else if (arg.equals ("no")) {
			Act.qstack.push ("no");
			String result = Act.handleConfirmation ();
			return result;
		}
		else */ if (arg.equals ("back") || arg.equals ("clear") || arg.equals ("reset")) {
			Act.clearhistory ();
			return "Memory cleared, please ask another question.";
		}
		else if (arg.equals ("end")) {
			Act.qstack.push ("terminate");
			return "terminate";
		}
		else {
			String	result = "Cannot handle the command "+arg+" Please ask something else.";
			Act.qstack.push (result);
			return result;
		}
	}

}


