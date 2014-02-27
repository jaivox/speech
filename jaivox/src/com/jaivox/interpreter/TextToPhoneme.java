package com.jaivox.interpreter;

import com.jaivox.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * convert text to phonemes. This utilizes a trained set of rules. For English
 * these are learned from the CMU Pronouncing dictionary created by the
 * CMU t2p perl program.
 * @author dev
 */

public class TextToPhoneme {
	
	TreeNode Root;
	int maxlevels = 100;

/**
 * Create a TextToPhoneme converter using the designated rules. For English
 * you can use the rules given in apps/commmon/t2prules_en.tree
 * @param rules 
 */
	public TextToPhoneme (String rules) {
		Root = new TreeNode ("0");
		boolean ok = initializeRules (rules);
		if (!ok) {
			Log.severe ("Did not load "+rules);
			return;
		}
	}
	
	boolean initializeRules (String rules) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (rules));
			String line;
			TreeNode levels [] = new TreeNode [maxlevels];
			levels [0] = Root;
			int linenum = 0;
			while ((line = in.readLine ()) != null) {
				if (line.trim ().length () == 0) break;
				linenum++;
				TreeNode n = new TreeNode (line);
				int l = n.level;
				if (l == 0) break;
				TreeNode parent = levels [l-1];
				if (parent == null) {
					Log.finest ("No parent Level:"+l+" line:"+linenum+" tag:"+n.tag);
					break;
				}
				levels [l-1].add (n);
				levels [l] = n;
			}
			in.close ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
/**
 * convert a given string into an array of phonemes
 * @param word
 * @return 
 */
	public String [] lexicalToPhoneme (String word) {
		ArrayList <String> result = new ArrayList <String> ();
		String w = word.toUpperCase ();
		int n = w.length ();
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<3; i++) {
			sb.append ("-");
		}
		for (int i=0; i<n; i++) {
			sb.append (w.charAt (i));
		}
		for (int i=0; i<3; i++) {
			sb.append ("-");
		}
		String text = new String (sb);
		
		for (int i=0; i<n; i++) {
			String letters = text.substring (i, i+7);
			String phone = context2phone (letters).toLowerCase ();
			result.add (phone);
		}
		int m = result.size ();
		String all [] = result.toArray (new String [m]);
		return all;
	}
	
/**
 * The standard way to convert a sentence to phonemes is to convert first
 * to words, and then to convert the words into phonemes, joining everything
 * together afterwards.
 * @param text
 * @return 
 */
	public String convertToPhonemes (String text) {
		StringBuffer sb = new StringBuffer ();
		StringTokenizer st = new StringTokenizer (text);
		while (st.hasMoreTokens ()) {
			String word = st.nextToken ();
			String phones = l2p (word);
			sb.append (phones);
			sb.append (' ');
		}
		String result = new String (sb).trim ();
		return result;
	}
		
/**
 * Convert a string to a space delimited string of phonemes
 * @param word
 * @return 
 */
	// old stuff from t2p
	String l2p (String word) {
		String w = word.toUpperCase ();
		int n = w.length ();
		StringBuffer sb = new StringBuffer ();
		for (int i=0; i<3; i++) {
			sb.append ("-");
		}
		for (int i=0; i<n; i++) {
			sb.append (w.charAt (i));
		}
		for (int i=0; i<3; i++) {
			sb.append ("-");
		}
		String text = new String (sb);
		
		StringBuffer result = new StringBuffer ();
		for (int i=0; i<n; i++) {
			String letters = text.substring (i, i+7);
			String phone = context2phone (letters);
			result.append (phone);
			result.append (' ');
		}
		String all = new String (result).toLowerCase ();
		return all;
	}
		
	String context2phone (String letters) {
		if (letters.length () != 7) return "_";
		char L3 = letters.charAt (0);
		char L2 = letters.charAt (1);
		char L1 = letters.charAt (2);
		char L  = letters.charAt (3);
		char R1 = letters.charAt (4);
		char R2 = letters.charAt (5);
		char R3 = letters.charAt (6);
		
		TreeNode current = Root;
		String result = "*";
		while (current != null) {
			TreeNode next = getMatch (current, L3, L2, L1, L, R1, R2, R3);
			if (next != null) {
				current = next;
				continue;
			}
			else {
				result = current.tag;
				break;
			}
		}
		return result;
	}
	
	TreeNode getMatch (TreeNode current, char L3, char L2, char L1, char L, char R1, char R2, char R3) {
		int n = current.nchildren ();
		if (n == 0) return null;
		for (int i=0; i<n; i++) {
			TreeNode child = current.get (i);
			String pattern = child.tag;
			String tokens [] = pattern.split ("/");
			if (tokens.length != 3) return child;
			char pat = tokens [2].charAt (0);
			
			if (tokens [1].equals ("L3")) {
				if (pat == L3) {
					return child;
				}
			}
			if (tokens [1].equals ("L2")) {
				if (pat == L2) {
					return child;
				}
			}
			if (tokens [1].equals ("L1")) {
				if (pat == L1) {
					return child;
				}
			}
			if (tokens [1].equals ("L")) {
				if (pat == L) {
					return child;
				}
			}
			if (tokens [1].equals ("R1")) {
				if (pat == R1) {
					return child;
				}
			}
			if (tokens [1].equals ("R2")) {
				if (pat == R2) {
					return child;
				}
			}
			if (tokens [1].equals ("R3")) {
				if (pat == R3) {
					return child;
				}
			}
		}
		return null;
	}
	
	
}
