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

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

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
	 * example, the file may be in answer_en.txt (for English).
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

	String selectPhrase (String [] phrases) {
			int n = phrases.length;
			int selected = (int)(Math.random ()*(double)n);
			if (selected >= n) selected = n-1;
			return phrases [selected];
	}


};
