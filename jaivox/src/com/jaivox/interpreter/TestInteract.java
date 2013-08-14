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
package com.jaivox.interpreter;

import com.jaivox.tools.Config;
import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * TestInteract is a command line program to test just the interpreter
 * without the use of agents. The test system asks random questions to
 * the interpreter.. The questions and answers will
 * be recorded in a text file.
 */

public class TestInteract extends Thread {

	static int waittime = 500; // milliseconds
	
	static String questions [];

	static void gatherQuestions (Interact inter) {
		questions = inter.questions;
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
	
	
/**
 * Do a random test, but mess up the questions using the messup function
 * above. This causes edit errors and sends the system into a state where
 * it may not understand the question. 
 * @param inter
 */
	static void doRandomMessups (Interact inter) {
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
			
			String messed = messup (question);
			System.out.println ("Messed: "+messed);
			String result = inter.execute (messed);
			System.out.println (result);
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
		try {
			if (args.length > 1) {
				String command = args[1];
				if (command.equals ("random")) {
					doRandomTests (inter);
					return;
				}
				else if (command.equals ("messup")) {
					doRandomMessups (inter);
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

