/*
   Jaivox version 0.5 August 2013
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

package com.jaivox.tools;

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;


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
	String tagsfile;
	String infosfile;
	String resultfile;
	String srcdir;
	String datadir;

	Grammar Gram;

	TreeMap <String, Infonode> infos;
	String fields [];
	Vector <String []> patterns;
	Vector <String> patorig;
	TreeMap <String, String []> gtags;
	Vector <String> questions;
	
	public static String nosemantics = "(_,_,_,_,_,_,_)";

/**
 * Generate questions from a set of properties in the configuration file.
 * Questions are generated for each field and associated attributes.
 * Generated questions are saved in a specified "questions_file".
@param keyval
 */
	public Questgen (Properties keyval) {
		kv = keyval;
		String base = kv.getProperty ("Base");
		String specdir = base + kv.getProperty ("source");
		srcdir = base + kv.getProperty ("common");
		String useonedir = kv.getProperty ("onedirectory");
		String dest = base + kv.getProperty ("destination");
		if (useonedir.equals ("yes")) {
			datadir = dest;
		}
		else {
			datadir = dest + kv.getProperty ("dir_interpreter") + "/";
		}
		String gram = kv.getProperty ("grammar_file");
		grammarfile = specdir + gram;
		Gram = new Grammar (grammarfile);
		patterns = Gram.patterns;
		patorig = Gram.allpaths;
		tagsfile = srcdir + kv.getProperty ("penn_tags");
		Tags t = new Tags (tagsfile);
		gtags = t.gtags;
		infos = new TreeMap <String, Infonode> ();
		String specs = kv.getProperty ("specs_file");
		if (specs != null) {
			infosfile = specdir + specs;
			loadinfos (specdir, infosfile);
			Check c = new Check (this);
			c.checkAll ();
		}
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

/**
 * Generate all questions based on the grammar and details in the
 * specification file. Questions are generated for each field. There
 * is no provision for generating questions involving multiple nouns
 * since this creates a very large set of questions. Generated questions
 * are saved in a specified "questions_file".
 */
	
	public void generate () {
		questions = new Vector <String> ();
		if (kv.getProperty ("specs_file") == null) {
			generateSimple ();
			return;
		}
		for (int i=0; i<fields.length; i++) {
			String field = fields [i];
			generatefield (field);
		}
	}

	void generatefield (String field) {
		Log.info ("Getting field "+field);
		Infonode finfo = infos.get (field);
		String [] attrs = finfo.tagval.get ("attributes");
		for (int i=0; i<attrs.length; i++) {
			String attribute = attrs [i];
			generatefieldattribute (field, attribute);
		}
	}

	void generatefieldattribute (String field, String attribute) {
		Log.info ("Getting field "+field+" attribute "+attribute);
		Infonode finfo = infos.get (field);
		Infonode ainfo = infos.get (attribute);
		for (int i=0; i<patterns.size (); i++) {
			String pat [] = patterns.elementAt (i);
			String pato = patorig.elementAt (i);
			Log.fine (finfo.name+"."+ainfo.name+" "+pato);
			generatepattern (finfo, ainfo, pat, pato);
		}
	}

	void generatepattern (Infonode finfo, Infonode ainfo,
						  String pat [], String pato) {
		int n = pat.length;
		String q [] = new String [n];
		gt (finfo, ainfo, 0, pat, pato, q);
	}

	void gt (Infonode finfo, Infonode ainfo, int stage,
			 String pat [], String pato, String q []) {
		int n = pat.length;
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<stage; i++) {
			sb.append (q [i]);
			if (i < n-1) sb.append (' ');
		}
		String quest = new String (sb);
		Log.finest ("gt:"+stage+" "+quest);
		if (stage >= n) {	// done
			if (quest == null || pato == null) return;
			if (q.length == 0) return;
			String selection = getselection (finfo, ainfo, pat, q);
			String out = quest + "\t" + pato + "\t" + selection;
			String trimmed = out.trim ();
			if (trimmed.length () == 0) return;
			if (questions.indexOf (trimmed) == -1) questions.add (trimmed);
			return;
		}
		// consider the current pat

		String gtag = pat [stage];
		// any lower case pattern is passed through
		if (!gtag.equals (gtag.toUpperCase ())) {
			q [stage] = gtag;
			gt (finfo, ainfo, stage+1, pat, pato, q);
		}
		else if (finfo.tagval.get (gtag) != null || ainfo.tagval.get (gtag) != null) {
		// else if (gtag.startsWith ("N") || gtag.startsWith ("J") || gtag.startsWith ("R")) {
			String [] words = finfo.tagval.get (gtag);
			if (words == null) words = ainfo.tagval.get (gtag);
			if (words != null) {
				for (int i=0; i<words.length; i++) {
					String word = words [i];
					q [stage] = word;
					gt (finfo, ainfo, stage+1, pat, pato, q);
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
				gt (finfo, ainfo, stage+1, pat, pato, q);
			}
		}
	}

/**
 * This function creates a semantic specification for user inputs. This is
 * based on some heuristics, especially in terms of determining whether the
 * input is a WH (who, what, when, which, where) question. However this is
 * quite necessary to know the semantics, i.e. meaning, of a generated
 * user input since it identifies the field and attribute used in the question.
 * 
 * The string produced here is not used in recognizing a question, or in the
 * finite state machine operations. For new applications, the user may find
 * it necessary to generate a different type of semantic representation, or
 * to create one by hand. This is needed for example if an anticipated user
 * input involves more than one field and associated quantifiers and
 * attributes.
 * @param finfo
 * @param ainfo
 * @param pat
 * @param q
 * @return
 */
	
	public String getselection (Infonode finfo, Infonode ainfo, String pat [], String q []) {
		String field = finfo.name;
		String attr = ainfo.name;
		// to see if it is a followup see if field or attribute is unspecified'
		String quant = "_";
		// just find the adjective
		int n = pat.length;
		if (n == 0) return nosemantics; 
		String action = "(find, ";
		String els = "_";
		String first = pat [0].substring (0, 1).toLowerCase ();
		// note the grammar tag will be W if we have a wh question regardless
		// of language, otherwise you have to fix up whether it is ask or find
		if (!first.equals ("w")) {
			boolean foundelse = false;
			for (int i=0; i<n; i++) {
				String p = pat [i];
				if (p.startsWith ("ELS")) {
					foundelse = true;
					break;
				}
			}
			if (!foundelse) action = "(ask, ";
			else els = "els";
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
			if (p.startsWith ("adverb")) {
				if (adverb.equals ("")) adverb = q[i];
				else adverb = adverb+"-"+q [i];
			}
		}
		if (adverb.equals ("")) adverb = "_";
		// proper names
		String nnp = "";
		for (int i=0; i<n; i++) {
			String p = pat [i];
			if (p.startsWith ("NNP")) {
				if (nnp.equals ("")) nnp = q[i];
				else nnp = nnp+"-"+q [i];
			}
		}
		if (nnp.equals ("")) nnp = "_";
		// if (quant.equals ("_")) attr = "_";
		// note nnp will contain a space if it contains anything
		String s = action+field+", "+attr+", "+quant+", "+nnp+", "+els+", "+adverb+")";
		return s;
	}
	
/**
 * Generate simple questions that do not involve expanding grammar tags
 */
	
	void generateSimple () {
		TreeMap <String, String> test = new TreeMap <String, String> ();
		String yes = "yes";
		for (int i=0; i<patorig.size (); i++) {
			String quest = patorig.elementAt (i);
			String q = quest.trim ();
			if (q.length () == 0) continue;
			String seen = test.get (q);
			if (seen != null) continue;
			else test.put (q, yes);
			questions.add (new String (q+"\t"+q+"\t"+nosemantics));
		}
	}
	
/**
 * Save the generated question in the specified "questions_file"
 */

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
