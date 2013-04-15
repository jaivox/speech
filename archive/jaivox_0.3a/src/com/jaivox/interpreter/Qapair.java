package com.jaivox.interpreter;

import java.util.*;

import com.jaivox.util.Log;

/**
 * Qapair holds questions paired with answers. This is primarily used to
 * store historical questions and answers so that we can formulate a reasonable
 * response to follow-up questions.
 */

public class Qapair {

	Interact act;

	String question;
	String answer;
	String spec;

	String qfilled;		// question with wildcards filled out
	int pos;			// position of this pair in the question-answer vector

	String command;
	String arg;
	String field;
	String attribute;
	String quant;
	String nnp;
	String els;

/**
 * Create a Qapair with the given question and answer. There is also pos, the
 * position of this pair in a question-answer history.
@param inter
@param p
@param q
@param a
 */
	public Qapair (Interact inter, int p, String q, String a) {
		act = inter;
		pos = p;
		question = q;
		answer = a;
		qfilled = null;
		getTerms ();
	}

	void getTerms () {
		command = "";
		arg = "";
		field = "";
		attribute = "";
		quant = "";
		nnp = "";
		els = "";

		spec = act.qspecs.get (question);
		if (spec == null) {
			Log.warning ("No specifications for question: "+question);
			return;
		}
		StringTokenizer st = new StringTokenizer (spec, "(),\r\n");

		command = st.nextToken ().trim ();
		if (command.equals ("command")) {
			if (st.hasMoreTokens ()) arg = st.nextToken ().trim ();
			while (st.hasMoreTokens ()) {
				String next = st.nextToken ().trim ();
				arg = arg + "," + next;
			}
			return;
		}

		// not a command
		if (st.hasMoreTokens ()) field = st.nextToken ().trim ();
		if (st.hasMoreTokens ()) attribute = st.nextToken ().trim ();
		if (st.hasMoreTokens ()) quant = st.nextToken ().trim ();
		String tok = "";
		if (st.hasMoreTokens ()) tok = st.nextToken ().trim ();
		if (tok.startsWith ("nnp: ")) {
			nnp = tok.substring ("nnp: ".length ());
		}
		else if (tok.startsWith ("els: ")) {
			els = tok;
		}
		if (!nnp.equals ("") && st.hasMoreTokens ()) {
			tok = st.nextToken ().trim ();
			if (tok.startsWith ("els: ")) els = tok;
		}
		Log.fine (details ());
	}
	
/**
 * Get the first command of the question
@return
 */
	public String getCommand () {
		return command;
	}
	
/**
 * Get the argument that follows a question
@return
 */
	
	public String getArg () {
		return arg;
	}

	boolean FAQspecified () {
		if (field.equals ("")) return false;
		if (field.equals ("_")) return false;
		if (attribute.equals ("")) return false;
		if (attribute.equals ("_")) return false;
		if (quant.equals ("")) return false;
		if (quant.equals ("_")) return false;
		return true;
	}

	boolean updateFAQ (Qapair p) {
		if (!p.FAQspecified ()) return false;
		if (field.equals ("_")) field = p.field;
		if (attribute.equals ("_")) attribute = p.attribute;
		if (quant.equals ("_")) quant = p.quant;
		return true;
	}

	public String toString () {
		return question+":"+spec+" = "+answer;
	}
	
	public String details () {
		String s = command+","+field+","+attribute+","+quant+","+nnp+","+els;
		return s;
	}

}

