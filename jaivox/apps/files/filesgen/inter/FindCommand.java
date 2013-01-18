/*
   Copyright 2010-2012 by Bits and Pixels, Inc.

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

import com.jaivox.interpreter.*;
import java.util.*;
import java.io.*;

public class FindCommand extends Command {

	String category;
	String instruction;
	String extra;

	public FindCommand () {
		super ();
	}

	public String handleCommand (Qapair p) {
		String arg = p.getArg ();
		StringTokenizer st = new StringTokenizer (arg, ",()");
		category = "";
		instruction = "";
		if (st.hasMoreTokens ()) category = st.nextToken ();
		if (st.hasMoreTokens ()) instruction = st.nextToken ();
		if (st.hasMoreTokens ()) extra = st.nextToken ();
		else extra = "";

		String result = "Not yet implemeted: "+p.toString ();
		System.out.println ("Qapair command=" + p.getCommand ());
		System.out.println ("Qapair arg=" + arg);
		System.out.println ("Category: "+category);
		System.out.println ("Instruction: "+instruction);
		System.out.println ("Extra: "+extra);

		// branching according to the extra information
		if (extra.equals ("matchingFile")) {
			result = matchingFile (instruction);
		}
		if (extra.equals ("latestTime")) {
			result = matchingTime (instruction);
		}
		if (extra.equals ("mostDir")) {
			result = secondResult (instruction);
		}
		return result;
	}

	String matchingFile (String input) {
		try {
			String temp = runbinsh (input, "test", "./");
			// to formulate the answer count lines then
			// list the first one
			StringTokenizer st = new StringTokenizer (temp, "\n");
			int n = st.countTokens ();
			System.out.println ("Number of matches = "+n);
			String first = filter (st.nextToken ());
			String result;
			if (n == 0) {
				result = "Sorry, there is no such file.";
			}
			else if (n == 1) {
				result = "There is only one file, namely "+first;
			}
			else if (n == 2) {
				String second = filter (st.nextToken ());
				result = "There are only two files, "+first+" and "+second;
			}
			else {
				result = "There are "+n+" files, the most recent is "+first;
			}
			return result;
		}
		catch (Exception e) {
			return "Sorry, I cannot find an answer to your question.";
		}
	}

	String matchingTime (String input) {
		try {
			String temp = runbinsh (input, "test", "./");
			StringTokenizer st = new StringTokenizer (temp, "\n");
			int n = st.countTokens ();
			System.out.println ("Number of matches = "+n);
			String first = getDate (st.nextToken ());
			String result;
			if (n == 0) {
				result = "It does not look like you have any doc files";
			}
			else {
				result = "The most recent time is "+first;
			}
			return result;
		}
		catch (Exception e) {
			return "Sorry, I cannot find an answer to your question.";
		}
	}

	String secondResult (String input) {
		try {
			String temp = runbinsh (input, "test", "./");
			// to formulate the answer count lines then
			// list the first one
			StringTokenizer st = new StringTokenizer (temp, "\n");
			int n = st.countTokens ();
			System.out.println ("Number of matches = "+n);
			String discard = st.nextToken ();
			String second = filter (st.nextToken ());
			String result;
			if (n == 0) {
				result = "Sorry, there is no such location.";
			}
			else {
				result = "The most recent is "+second;
			}
			return result;
		}
		catch (Exception e) {
			return "Sorry, I cannot find an answer to your question.";
		}
	}


	String runcommand (String input) {
		try {
			Process p = Runtime.getRuntime ().exec (input);
			StringBuffer buffer = new StringBuffer ();
			InputStream in = p.getInputStream ();
			BufferedInputStream d = new BufferedInputStream (in);
			do {
				int ch = d.read ();
				if (ch == -1)
					break;
				buffer.append ((char) ch);
			} while (true);
			in.close ();
			String temp = new String (buffer);
			return temp;
		} catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}

	String runbinsh (String command, String stub, String directory) {
		try {
			String script = new String (directory + stub + "_script");
			PrintWriter out = new PrintWriter (new FileWriter (script));
			out.println ("#!/bin/bash");
			// out.println ("cd "+directory);
			out.println (command);
			out.close ();
			String result1 = runcommand ("chmod a+x " + script);
			String result2 = runcommand (script);
			// runcommand ("/bin/rm ./"+script);
			return result2;
		} catch (Exception e) {
			e.printStackTrace ();
			return "";
		}
	}

	String filter (String filename) {
		int pos = filename.indexOf ("/");
		if (pos == -1) pos = 0;
		return filename.substring (pos);
	}

	// look for the 6th field
	String getDate (String filename) {
		String fields [] = filename.split (" ");
		int n = fields.length;
		for (int i=0; i<n; i++) {
			System.out.println (""+i+" "+fields [i]);
		}
		if (n < 7) return "well, I can't figure it out.";
		String result = fields [6];
		return result;
	}
};

