
package com.jaivox.tools;

import java.io.*;
import java.util.*;
import com.jaivox.interpreter.Adjective;
import com.jaivox.util.Log;

/**
 * Generates questions along with their semantic specifications. We generate
 * questions along with specifications that tell us what to do with the
 * question. When the user speaks, we try to match what is said with the
 * questions in our system, and then we can look up these specifications to
 * decide how to answer the question.
 */

public class Questgen {

	Properties kv;

	String grammarfile;
	String redirectfile;
	String tagsfile;
	String infosfile;
	String resultfile;
	String srcdir;
	String datadir;

	Adjective Adj;

	TreeMap <String, Infonode> infos;
	String fields [];
	Vector <String []> patterns;
	Vector <String> patorig;
	Vector <String []> redirpats;
	Vector <String> redirorig;
	Hashtable <String, String> redirspecs;
	TreeMap <String, String []> gtags;
	Vector <String> questions;

	// static String yesno [] = {"is", "are", "was", "were", "do", "does", "how",
		// "would", "will", "could", "can"};

	// Vector <String> yesnospecs;

/**
 * Generate questions from a set of properties in the configuration file.
@param keyval
 */
	public Questgen (Properties keyval) {
		kv = keyval;
		String specdir = kv.getProperty ("source");
		srcdir = kv.getProperty ("common");
		String useonedir = kv.getProperty ("onedirectory");
		String dest = kv.getProperty ("destination");
		if (useonedir.equals ("true")) {
			datadir = dest;
		}
		else {
			datadir = dest + kv.getProperty ("dir_interpreter") + "/";
		}
		String gram = kv.getProperty ("grammar_file");
		grammarfile = specdir + gram;
		Grammar g = new Grammar (grammarfile);
		patterns = g.patterns;
		patorig = g.patorig;
		String redir = kv.getProperty ("redirects_file");
		redirectfile = srcdir + redir;
		Grammar r = new Grammar (redirectfile);
		redirpats = r.patterns;
		redirorig = r.patorig;
		redirspecs = r.specs;
		String penntags = kv.getProperty ("penn_tags");
		tagsfile = srcdir + penntags;
		Tags t = new Tags (tagsfile);
		gtags = t.gtags;
		// yesnospecs = new Vector <String> ();
		// for (int i=0; i<yesno.length; i++) {
		// 	yesnospecs.add (yesno [i]);
		// }
		Adj = new Adjective ();
		infos = new TreeMap <String, Infonode> ();
		String specs = kv.getProperty ("specs_file");
		infosfile = specdir + specs;
		loadinfos (specdir, infosfile);
		Check c = new Check (this);
		c.checkAll ();
		questions = new Vector <String> ();
		String qq = kv.getProperty ("questions_file");
		resultfile = datadir + qq;
	}

	void loadinfos (String specdir, String filename) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			Vector <String> hold;
			int nfield = 0;
			outer:
			while ((line = in.readLine ()) != null) {
				String rest = line.trim ();
				if (rest.startsWith ("//")) continue;
				if (rest.startsWith ("{")) {
					hold = new Vector <String> ();
					while ((line = in.readLine ()) != null) {
						rest = line.trim ();
						if (rest.startsWith ("//")) continue;
						hold.add (rest);
						if (rest.endsWith ("}")) {
							Infonode node = new Infonode (specdir, hold);
							node.buildAdjectives (Adj);
							infos.put (node.name, node);
							String [] types = node.tagval.get ("type");
							if (types == null) {
								Log.severe ("No type for node "+node.name);
							}
							else {
								if (types [0].equals ("field")) nfield++;
							}
							continue outer;
						}
					}
				}
			}
			in.close ();
			Log.info ("Created "+infos.size ()+" infos");
			fields = new String [nfield];
			Set <String> keys = infos.keySet ();
			int i = 0;
			for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				Infonode sp = infos.get (key);
				String [] types = sp.tagval.get ("type");
				if (types != null && types [0].equals ("field")) {
					fields [i++] = key;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public void generate () {
		questions = new Vector <String> ();
		for (int i=0; i<fields.length; i++) {
			String field = fields [i];
			generatefield (field);
		}
	}

	void generatefield (String field) {
		Infonode finfo = infos.get (field);
		String [] attrs = finfo.tagval.get ("attributes");
		for (int i=0; i<attrs.length; i++) {
			String attribute = attrs [i];
			generatefieldattribute (field, attribute);
		}
	}

	void generatefieldattribute (String field, String attribute) {
		Infonode finfo = infos.get (field);
		Infonode ainfo = infos.get (attribute);
		for (int i=0; i<patterns.size (); i++) {
			String pat [] = patterns.elementAt (i);
			String pato = patorig.elementAt (i);
			Log.fine (finfo.name+"."+ainfo.name+" "+pato);
			generatepattern (finfo, ainfo, pat);
		}
		for (int i=0; i<redirpats.size (); i++) {
			String pat [] = redirpats.elementAt (i);
			String pato = redirorig.elementAt (i);
			Log.fine (finfo.name+"."+ainfo.name+" "+pato);
			generateredirect (finfo, ainfo, pat, pato);
		}
	}

	void generatepattern (Infonode finfo, Infonode ainfo, String pat []) {
		int n = pat.length;
		String q [] = new String [n];
		gt (finfo, ainfo, 0, pat, q);
	}

	void gt (Infonode finfo, Infonode ainfo, int stage, String pat [], String q []) {
		int n = pat.length;
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<stage; i++) {
			sb.append (q [i]);
			// blanks are also tokens
			// if (i < n-1) sb.append (' ');
		}
		String quest = new String (sb);
		Log.finest ("gt:"+stage+" "+quest);
		if (stage >= n) {	// done
			String selection = getselection (finfo, ainfo, pat, q);
			String out = quest + "\t" + selection;
			if (questions.indexOf (out) == -1) questions.add (out);
			return;
		}
		// consider the current pat

		String gtag = pat [stage];
		// any lower case pattern is passed through
		if (gtag.equals (gtag.toLowerCase ())) {
			q [stage] = gtag;
			gt (finfo, ainfo, stage+1, pat, q);
		}
		/*
		else if (gtag.startsWith ("W")) {
			String whquestions [] = finfo.tagval.get ("wh");
			if (whquestions != null) {
				for (int i=0; i<whquestions.length; i++) {
					String whquestion = whquestions [i];
					q [stage] = whquestion;
					gt (finfo, ainfo, stage+1, pat, q);
				}
			}
			else return;
		}*/
		else if (gtag.equals ("VBZ")) {
			q [stage] = "is";
			gt (finfo, ainfo, stage+1, pat, q);
		}
		else if (gtag.equals ("VBP")) {
			q [stage] = "are";
			gt (finfo, ainfo, stage+1, pat, q);
		}
		else if (finfo.tagval.get (gtag) != null || ainfo.tagval.get (gtag) != null) {
		// else if (gtag.startsWith ("N") || gtag.startsWith ("J") || gtag.startsWith ("R")) {
			String [] words = finfo.tagval.get (gtag);
			if (words == null) words = ainfo.tagval.get (gtag);
			if (words != null) {
				for (int i=0; i<words.length; i++) {
					String word = words [i];
					q [stage] = word;
					gt (finfo, ainfo, stage+1, pat, q);
				}
			}
			else return;
		}
		else {
			String options [] = gtags.get (gtag);
			if (options == null) {
				Log.severe ("No options for Grammar tag "+gtag);
				return;
			}
			int m = options.length;
			for (int j=0; j<m; j++) {
				q [stage] = options [j];
				gt (finfo, ainfo, stage+1, pat, q);
			}
		}
	}

	String getselection (Infonode finfo, Infonode ainfo, String pat [], String q []) {
		String field = finfo.name;
		String attr = ainfo.name;
		// to see if it is a followup see if field or attribute is unspecified
		String quant = "_";
		// just find the adjective
		int n = pat.length;
		String action = "(find, ";
		if (!pat [0].startsWith ("W")) {
			boolean foundelse = false;
			for (int i=0; i<n; i++) {
				String p = pat [i];
				if (p.startsWith ("ELS")) {
					foundelse = true;
					break;
				}
			}
			if (!foundelse) action = "(ask, ";
		}
		// if (yesnospecs.indexOf (pat [0]) != -1)
		// 	action = "(ask, ";
		boolean foundField = false;
		for (int i=0; i<n; i++) {
			String p = pat [i];
			if (p.startsWith ("NN")) {
				foundField = true;
				break;
			}
			String word = q [i];
			if (word.startsWith (field)) {
				foundField = true;
			}
		}
		if (!foundField) field = "_";
		for (int i=0; i<n; i++) {
			String p = pat [i];
			if (p.startsWith ("JJ")) quant = p;
		}
		// adverbial terms
		String adverb = "";
		for (int i=0; i<n; i++) {
			String p = pat [i];
			if (p.startsWith ("RB")) adverb = adverb+", "+p;
		}
		// proper names
		String nnp = "";
		for (int i=0; i<n; i++) {
			String p = pat [i];
			if (p.startsWith ("NNP")) nnp = nnp+", NNP: "+q [i];
		}
		// else
		String els = "";
		for (int i=0; i<n; i++) {
			String p = pat [i];
			if (p.startsWith ("ELS")) els = els+", ELS: "+q[i];
		}
		if (quant.equals ("_")) attr = "_";
		// note nnp will contain a space if it contains anything
		String s = action+field+", "+attr+", "+quant+adverb+nnp+els+")";
		return s;
	}

	void generateredirect (Infonode finfo, Infonode ainfo, String pat [], String orig) {
		int n = pat.length;
		String q [] = new String [n];
		gtr (finfo, ainfo, 0, pat, q, orig);
	}

	void gtr (Infonode finfo, Infonode ainfo, int stage, String pat [], String q [], String orig) {
		int n = pat.length;
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<stage; i++) {
			sb.append (q [i]);
			if (i < n-1) sb.append (' ');
		}
		String quest = new String (sb);
		Log.finest ("gtr:"+stage+" "+quest);
		if (stage >= n) {	// done
			String selection = redirspecs.get (orig);
			if (selection != null) {
				String out = quest + "\t" + selection;
				if (questions.indexOf (out) == -1) questions.add (out);
			}
			return;
		}
		// consider the current pat

		String gtag = pat [stage];
		// any lower case pattern is passed through
		if (gtag.equals (gtag.toLowerCase ())) {
			q [stage] = gtag;
			gtr (finfo, ainfo, stage+1, pat, q, orig);
		}
		else if (gtag.equals ("VBZ")) {
			q [stage] = "is";
			gtr (finfo, ainfo, stage+1, pat, q, orig);
		}
		else if (gtag.equals ("VBP")) {
			q [stage] = "are";
			gtr (finfo, ainfo, stage+1, pat, q, orig);
		}
		else if (finfo.tagval.get (gtag) != null || ainfo.tagval.get (gtag) != null) {
			String [] words = finfo.tagval.get (gtag);
			if (words == null) words = ainfo.tagval.get (gtag);
			if (words != null) {
				for (int i=0; i<words.length; i++) {
					String word = words [i];
					q [stage] = word;
					gtr (finfo, ainfo, stage+1, pat, q, orig);
				}
			}
			else return;
		}
		else {
			String options [] = gtags.get (gtag);
			if (options == null) {
				Log.severe ("No options for Grammar tag "+gtag);
				return;
			}
			int m = options.length;
			for (int j=0; j<m; j++) {
				q [stage] = options [j];
				gtr (finfo, ainfo, stage+1, pat, q, orig);
			}
		}
	}

	public void saveQuestions () {
		try {
			
			PrintWriter out = new PrintWriter (new FileWriter (resultfile));

			for (int i=0; i<questions.size (); i++) {
				out.println (questions.elementAt (i));
			}
			Log.info (""+questions.size ()+" questions saved in "+resultfile);
			out.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

};





