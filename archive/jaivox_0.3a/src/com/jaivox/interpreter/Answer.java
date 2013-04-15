package com.jaivox.interpreter;

import java.io.*;
import java.util.*;

import com.jaivox.util.*;

public class Answer {

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
	 * Answer holds the options for putting together a response in script.
	 * This is language independent, the answer forms are obtained from
	 * a file specified in the configuration under "answer_forms". For
	 * example, the file may be in common/lang/en/answer.txt (for English).
	@param filename
	 */

	public Answer (String filename) {
		// read the answer data and load the above tables
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			boolean started = false;
			Vector <String> hold = new Vector <String> ();
			String tag = "";
			while ((line = in.readLine ()) != null) {
				String trimmed = line.trim ();
				if (trimmed.startsWith ("{")) {
					started = true;
					continue;
				}
				if (!started) continue;
				else {
					if (trimmed.startsWith ("}")) {
						started = false;
						break;
					}
				}
				if (trimmed.endsWith (":")) {
					if (hold.size () > 0) {
						createArray (tag, hold);
					}
					tag = trimmed.substring (0, trimmed.length () - 1);
					hold = new Vector <String> ();
					continue;
				}
				else {
					if (trimmed.length () == 0) continue;
					hold.add (trimmed);
				}
			}
			in.close ();
			// at end if there is something in hold
			if (hold.size () > 0) {
				createArray (tag, hold);
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void createArray (String tag, Vector <String> hold) {
		int n = hold.size ();
		String data [] = new String [n];
		for (int i=0; i<n; i++) {
			data [i] = hold.elementAt (i);
		}
		/*
		// show
		System.out.println (tag+":");
		for (int i=0; i<n; i++) {
			System.out.println ("\t"+data [i]);
		}
		*/
		
		if (tag.equals ("intro")) intro = (String [])data.clone ();

		else if (tag.equals ("yesanswers")) yesanswers = (String [])data.clone ();
		else if (tag.equals ("noanswers")) noanswers = (String [])data.clone ();
		else if (tag.equals ("confused")) confused = (String [])data.clone ();
		else if (tag.equals ("followup")) followup = (String [])data.clone ();
		else if (tag.equals ("topics")) topics = (String [])data.clone ();


		else if (tag.equals ("oneitem")) oneitem = (String [])data.clone ();
		else if (tag.equals ("twoitems")) twoitems = (String [])data.clone ();
		else if (tag.equals ("manyitems")) manyitems = (String [])data.clone ();
		else if (tag.equals ("forinstance")) forinstance = (String [])data.clone ();
		else if (tag.equals ("askanother")) askanother = (String [])data.clone ();

		else if (tag.equals ("dontknow")) dontknow = (String [])data.clone ();
		else Log.warning ("Invalid tag "+tag);
	}

};
