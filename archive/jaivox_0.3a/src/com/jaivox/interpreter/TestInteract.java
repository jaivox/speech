package com.jaivox.interpreter;

import java.io.*;
import java.util.*;

import com.jaivox.tools.Config;
import com.jaivox.util.*;

/**
 * TestInteract is a command line program to test just the interpreter
 * without the use of agents. there are various tests to see if the
 * answers generated are reasonable. The questions and answers will
 * be recorded in a text file.
 */

public class TestInteract extends Thread {

	static int waittime = 500; // milliseconds
	
	static String questions [];
	static String fullyspeced [];
	static String variablyspeced [];
	static TreeMap <String, Vector <String>> specLinks;

	static void gatherQuestions (Interact inter) {
		TreeMap<String, String> qspecs = inter.qspecs;
		Set<String> qq = qspecs.keySet ();
		int n = qspecs.size ();
		questions = new String[n];
		int i = 0;
		for (Iterator<String> it = qq.iterator (); it.hasNext ();) {
			questions[i++] = it.next ().trim ();
		}
	}

	static void gatherSpecQuestions (Interact inter) {
		TreeMap<String, String> qspecs = inter.qspecs;
		Set<String> qq = qspecs.keySet ();
		Vector <String> full = new Vector <String> ();
		Vector <String> not = new Vector <String> ();
		for (Iterator<String> it = qq.iterator (); it.hasNext ();) {
			String q = it.next ();
			String s = qspecs.get (q);
			if (s.indexOf ("_") == -1) {
				full.add (q);
			}
			else {
				not.add (q);
			}
		}
		int n = full.size ();
		fullyspeced = new String [n];
		for (int i=0; i<n; i++) {
			fullyspeced [i] = full.elementAt (i);
		}
		int m = not.size ();
		variablyspeced = new String [m];
		for (int i=0; i<m; i++) {
			variablyspeced [i] = not.elementAt (i);
		}
		linkSpecs (inter);
	}
	
	static void linkSpecs (Interact inter) {
		String dummy = "dummy";
		specLinks = new TreeMap <String, Vector <String>> ();
		for (int i=0; i<fullyspeced.length; i++) {
			String f = fullyspeced [i];
			// String fspec = inter.qspecs.get (f.toLowerCase ());
			Qapair fqa = new Qapair (inter, 0, f, dummy);
			Vector <String> hold = new Vector <String> ();
			for (int j=0; j<variablyspeced.length; j++) {
				String v = variablyspeced [j];
				// String vspec = inter.qspecs.get (v.toLowerCase ());
				Qapair vqa = new Qapair (inter, 0, v, dummy);
				if (qaMatches (fqa, vqa)) hold.add (v);
			}
			if (hold.size () > 0) {
				specLinks.put (f, hold);
				// System.out.println ("Added "+hold.size ()+" matches for "+f);
			}
		}
	}
	
	static boolean qaMatches (Qapair a, Qapair b) {
		if (!b.field.equals ("_") && !b.field.equals ("")) {
			if (!b.field.equals (a.field)) return false;
		}
		if (!b.attribute.equals ("_") && !b.attribute.equals ("")) {
			if (!b.attribute.equals (a.attribute)) return false;
		}
		if (!b.quant.equals ("_") && !b.quant.equals ("")) {
			if (!b.quant.equals (a.quant)) return false;
		}
		if (!b.nnp.equals ("_") && !b.nnp.equals ("")) {
			if (!b.nnp.equals (a.nnp)) return false;
		}
		return true;
	}

	static void doRandomTests (Interact inter) {
		int N = 20;
		int n = questions.length;
		double nx = (double)n;
		for (int j = 0; j < N; j++) {
			int id = (int) (nx * Math.random ());
			if (id >= n)
				id = n - 1;
			String question = questions [id];
			System.out.println ("> " + question);
			if (question.length () == 0)
				continue;
			String result = inter.execute (question);
			System.out.println (result);
		}
	}
	
	static void followonTests (Interact inter) {
		int N = 20;
		int M = 8;
		int n = fullyspeced.length;
		double nx = (double)n;
		for (int i=0; i<N; i++) {
			int id = (int) (nx * Math.random ());
			if (id >= n)
				id = n - 1;
			// do the qa with the fully speced question
			String question = fullyspeced [id];
			System.out.println ("> " + question);
			if (question.length () == 0)
				continue;
			String result = inter.execute (question);
			System.out.println (result);
			int Mnow = (int)(Math.random ()*M);
			Vector <String> matches = specLinks.get (question);
			if (matches == null) continue;
			int mn = matches.size ();
			int mxx = Math.min (mn, Mnow);
			for (int j=0; j<mxx; j++) {
				int jd = (int)(mn*Math.random ());
				if (jd >= mn) jd = mn - 1;
				String follow = matches.elementAt (jd);
				System.out.println ("> " + follow);
				result = inter.execute (follow);
				System.out.println (result);
			}
		}
	}
	
	// cause num edit errors in the given word
	static String causeEditErrors (String word, int num) {
		char c [] = word.toCharArray ();
		int n = c.length;
		int count = 0;
		while (count < num) {
			int toss = (int)(Math.random () * 3.01);
			if (toss == 3) continue;
			switch (toss) {
			case 0: { // create a deletion error
				int i = (int)(Math.random () * n);
				StringBuffer sb = new StringBuffer ();
				for (int j=0; j<i; j++) {
					sb.append (c [j]);
				}
				for (int j=i+1; j<n; j++) {
					sb.append (c [j]);
				}
				word = new String (sb);
				c = word.toCharArray ();
				count++;
				n--;
				continue;
			}
			case 1:  {// create an insertion error
				int i = (int)(Math.random () * n);
				StringBuffer sb = new StringBuffer ();
				for (int j=0; j<i; j++) {
					sb.append (c [j]);
				}
				sb.append ('a');
				for (int j=i; j<n; j++) {
					sb.append (c [j]);
				}
				word = new String (sb);
				c = word.toCharArray ();
				count++;
				n++;
				continue;
			}
			case 2: { // switch two characters
				int i = (int)(Math.random () * n);
				int j = (int)(Math.random () * n);
				char d = c [i];
				c [i] = c [j];
				c [j] = d;
				count++;
				continue;
			}
			}
		}
		String result = new String (c);
		return result;
	}
	
	static String causeSentenceErrors (String sentence, int num) {
		String words [] = sentence.split (" ");
		int n = words.length;
		int count = 0;
		while (count < num) {
			int toss = (int)(Math.random () * 3.01);
			if (toss == 3) continue;
			switch (toss) {
			case 0: { // create a deletion error
				int i = (int)(Math.random () * n);
				StringBuffer sb = new StringBuffer ();
				for (int j=0; j<i; j++) {
					sb.append (words [j]);
					sb.append (" ");
				}
				for (int j=i+1; j<n; j++) {
					sb.append (words [j]);
					sb.append (" ");
				}
				String full = new String (sb);
				words =  full.split (" ");
				n--;
				count++;
				continue;
			}
			case 1:  {// create an insertion error
				int i = (int)(Math.random () * n);
				StringBuffer sb = new StringBuffer ();
				for (int j=0; j<i; j++) {
					sb.append (words [j]);
					sb.append (" ");
				}
				sb.append ("aaaaa ");
				for (int j=i; j<n; j++) {
					sb.append (words [j]);
					sb.append (" ");
				}
				String full = new String (sb);
				words =  full.split (" ");
				count++;
				n++;
				continue;
			}
			case 2: { // switch two characters
				int i = (int)(Math.random () * n);
				int j = (int)(Math.random () * n);
				String d = words [i];
				words [i] = words [j];
				words [j] = d;
				count++;
				continue;
			}
			}
		}
		String result = join (words, " ");
		return result;
	}
	
	static String join (String words [], String sep) {
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<words.length-1; i++) {
			sb.append (words [i]);
			sb.append (sep);
		}
		sb.append (words [words.length-1]);
		String result = new String (sb);
		return result;
	}
	
	static String messup (String question) {
		// should we mess up some words
		int toss = (int)(Math.random ()*10.0);
		String result = "";
		if (toss > 5) {
			// how many words to mess up?
			String words [] = question.split (" ");
			int n = words.length;
			int count = (int)(Math.random ()*4);
			for (int i=0; i<count; i++) {
				int j = (int)(Math.random ()*(n-1));
				String word = words [j];
				// int errors = (int)(Math.random ()*(word.length ()/2));
				int errors = 2;
				words [j] = causeEditErrors (word, errors);
			}
			result = join (words, " ");
		}
		else {
			int count = 2;
			result = causeSentenceErrors (question, count);
		}
		return result;
	}
	
	static void fintestateTests (Interact inter) {
		int N = 20;
		int n = fullyspeced.length;
		double nx = (double)n;
		int count = 0;
		String question = "";
		while (count < N) {
			String qs = inter.qstate;
			if (qs.equals ("initial")) {
				int id = (int) (nx * Math.random ());
				if (id >= n) id = n - 1;
				question = fullyspeced [id];
				// should we mess this up?
				if (Math.random () > 0.4) {
						// System.out.println ("original: "+question);
						question = messup (question);
						// System.out.println ("messed up: "+question);
						// record what was heard
						// inter.Record.record ("H: "+question);
				}
			}
			else if (qs.equals ("asking_confirmation")) {
				if (Math.random () > 0.4) {
					question = "yes";
				}
				else {
					question = "no";
				}
			}
			else {
				question = "Please enter quit interactively.";
			}
			System.out.println ("> " + question);
			if (question.length () == 0)
				continue;
			String result = inter.execute (question);
			System.out.println (result);
			count++;
		}
	}

/**
 * Do various tests. You can specify the type of test using the second
 * command line argument. Here you can have "random" tests, tests of
 * "followon" or followup questions and tests of the finite state machine
 * involved in handling conversations.
@param args
 */
	public static void main (String args[]) {
		new Config (args [0]);
		Properties kv = Config.kv;
		Interact inter = new Interact ("./", kv);
		gatherQuestions (inter);
		gatherSpecQuestions (inter);
		try {
			if (args.length > 1) {
				String command = args[1];
				if (command.equals ("random")) {
					doRandomTests (inter);
					return;
				}
				else if (command.equals ("followon")) {
					followonTests (inter);
					return;
				}
				else if (command.equals ("finitestates")) {
					fintestateTests (inter);
					return;
				}
				// else
				BufferedReader in = new BufferedReader (
						new FileReader (command));
				String line;
				while ((line = in.readLine ()) != null) {
					System.out.println ("> " + line);
					if (line.trim ().length () == 0)
						continue;
					String result = inter.execute (line);
					System.out.println (result);
				}
				in.close ();
				inter.terminate ();
			} else {
				BufferedReader in = new BufferedReader (new InputStreamReader (
						System.in));
				while (true) {
					System.out.print ("> ");
					String line = in.readLine ();
					if (line.equals ("quit")) {
						System.out.println ("Ending test program");
						inter.terminate ();
					}
					String result = inter.execute (line);
					sleep (waittime);
					System.out.println ("Result: " + result);
				}
			}
		} catch (Exception e) {
			Log.severe ("Unknown file or command "+args [1]);
		}
	}
}

