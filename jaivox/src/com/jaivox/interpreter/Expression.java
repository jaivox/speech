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

import com.jaivox.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expression evaluates arithmetic expressions.
 */

public class Expression {

	static String decimal = "(\\d*\\.\\d+)";
	static String numeral = "(\\d+)";
	static String operators = "([\\+\\-\\*/\\(\\)])";
	static String extops = "([\\+\\-\\*/=<>&~\\(\\)\\|])";

	static String word = "(\\w+)";

	static String numeric = "(" + decimal + "|" + numeral + "|" + operators + ")";
	static String formula = "(" + decimal + "|" + word + "|" + numeral +"|" + extops + ")";
	static Pattern Numeric = Pattern.compile (numeric);
	static Pattern Formula = Pattern.compile (formula);

	/**
	 * You do not need to create a class to use the functions here
	 */
	public Expression () {
	}
	
	/**
	 * create the tokens in a numeric expression
	 * @param input
	 * @return
	 */

	static String tokenizeNumeric (String input) {
		Matcher m = Numeric.matcher (input);
		StringBuffer sb = new StringBuffer ();
		while (m.find ()) sb.append (m.group ().toString () + " ");
		String result = new String (sb);
		return result;
	}
	
	/**
	 * Create tokens in an expression involving relationships and variables
	 * @param input
	 * @return
	 */

	static String tokenizeFormula (String input) {
		Matcher m = Formula.matcher (input);
		StringBuffer sb = new StringBuffer ();
		while (m.find ()) sb.append (m.group ().toString () + " ");
		String result = new String (sb);
		return result;
	}

	/**
	 * Convert the tokenized stream into postfix (where the operators are moved to
	 * the end to make evaluation easier.)
	 * @param tokenized
	 * @return
	 */
	static String postFix (String tokenized) {
		try {
			String tokens [] = tokenized.split (" ");
			int n = tokens.length;
			Stack <String> stack = new Stack <String> ();
			StringBuffer sb = new StringBuffer ();

			for (int i=0; i<n; i++) {
				String e = tokens [i];
				int pe = priority (e);
				if (pe != 0) {
					while (!stack.empty () && priority (stack.peek ()) >= pe) {
						sb.append (stack.pop () + " ");
					}
					stack.push (e);
				}
				else if (e.equals ("(")) {
					stack.push (e);
				}
				else if (e.equals (")")) {
					while (!stack.peek ().equals ("(")) {
						sb.append (stack.pop () + " ");
					}
					stack.pop ();
				}
				else {
					sb.append (e+" ");
				}
			}
			while (!stack.empty ()) sb.append (stack.pop () + " ");
			String postfix = new String (sb).trim ();
			return postfix;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return ("0");
		}
	}

	/**
	 * Priority table for operators and relations
	 * @param op
	 * @return
	 */
	
	static int priority (String op) {
		if (op.equals ("=") || op.equals ("<") || op.equals (">")) return 1;
		else if (op.equals ("&")) return 2;
		else if (op.equals ("|")) return 3;
		else if (op.equals ("+") || op.equals ("-")) return 4;
		else if (op.equals ("*") || op.equals ("/")) return 5;
		else return 0;
	}

	/**
	 * Evaluate a postfix numeric expression, returning the value
	 * @param postfix
	 * @return
	 */
	
	static int evaluate (String postfix) {
		try {
			String tokens [] = postfix.split (" ");
			Stack <String> stack = new Stack <String> ();
			int n = tokens.length;

			for (int i=0; i<n; i++) {
				String e = tokens [i];
				int pe = priority (e);
				if (e.equals ("(") || e.equals (")")) {
					Log.severe ("Unexpected parens in expression "+postfix);
					return 0;
				}
				if (pe == 0) {
					stack.push (e);
					continue;
				}

				// unary ~
				if (e.equals ("~")) {
					int a = Integer.parseInt (stack.pop ());
					int b = -a;
					stack.push (""+b);
					continue;
				}

				// all binary
				int a = Integer.parseInt (stack.pop ());
				int b = Integer.parseInt (stack.pop ());
				int c = 0;

				if (e.equals ("+")) {
					c = a + b;
				}
				else if (e.equals ("*")) {
					c = a * b;
				}
				else if (e.equals ("-")) {
					c = b - a;
				}
				else if (e.equals ("/")) {
					c = b/a;
				}
				else if (e.equals ("|")) {
					c = (a!=0 || b!=0 ? 1 : 0);
				}
				else if (e.equals ("&")) {
					c = (a!=0 && b!=0 ? 1 : 0);
				}
				else if (e.equals ("=")) {
					c = (a==b ? 1 : 0);
				}
				else if (e.equals ("<")) {
					c = (b<a ? 1 : 0);
				}
				else if (e.equals (">")) {
					c = (b>a ? 1 : 0);
				}
				else {
					Log.severe ("Unexpected operator "+e);
					return 0;
				}
				stack.push (""+c);
			}
			int val = Integer.parseInt (stack.pop ());
			return val;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return 0;
		}

	}

	/**
	 * test program with output to show how this works
	 */

	public static void main (String args []) {
		try {
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));
			System.out.println ("Enter numeric expression");
			// System.out.println ("Enter formula");
			String s = in.readLine ();
			String t = Expression.tokenizeNumeric (s);
			// String t = E.tokenizeFormula (s);
			System.out.println (t);
			String p = Expression.postFix (t);
			System.out.println (p);
			int v = Expression.evaluate (p);
			System.out.println ("value = "+v);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
};

