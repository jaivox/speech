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

import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.HistNode;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

public class findCommand extends Command {

	// replace the /home/you etc here with some smaller part of our file system
	// otherwise the find command may take a long time to return a result

	String s1 = "find /home/you/somewhere/small/ -type f -name \'*.java\' -exec grep -il entropy \'{}\' \\; | xargs ls -lt";
	String s2 = "find /home/you/somehwere/else/ -type f -name \'*.odt\' | xargs ls -lt";
	String s3 = "find /home/you/some/papers/ -maxdepth 1 -type d | while read -r dir; do printf \"%s:\t\" \"$dir\"; find \"$dir\" | wc -l; done | sort -n -r -k2";

	PleaseChange_s1s2s3
	// the line above is to cause a compilation error so that you will change
	// s1, s2, s3 above to something suitable to your system.

	public findCommand () {
	}

	public String [] handle (String f,
		String question, String spec, String instate,
		Vector <HistNode> history) {
		if (f.equals ("find"))
			return find (question, spec, instate, history);
		else return null;
	}

	String [] find (String question, String spec, String instate, Vector <HistNode> history) {
		String result [] = new String [1];
		result [0] = "";
		if (spec.equals ("(find matchingFile)")) {
			result [0] = matchingFile (s1);
		}
		if (spec.equals ("(find latestTime)")) {
			result [0] = matchingTime (s2);
		}
		if (spec.equals ("(find mostDir)")) {
			result [0] = secondResult (s3);
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
				result = "There are "+n+" files";
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
}

