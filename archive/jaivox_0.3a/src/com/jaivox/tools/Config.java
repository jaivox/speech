
package com.jaivox.tools;

import java.io.*;
import java.util.*;

/**
 * Reads a configuration file and sets relevant values in a key value
 * hash table kv.
 */

public class Config {

	/**
	 * key value table of properties from the configuation file
	 */
	public static Properties kv;

	/**
	 * create the key value properties from the given configuration
	 * file name.
	@param filename
	 */
	public Config (String filename) {
		kv = new Properties ();
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			kv.load (in);
			setAgentNames ();
			// kv.list (System.out);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void setAgentNames () {
		String prefix = kv.getProperty ("prefix");
		if (prefix == null) return;

		String recognizer = kv.getProperty ("recognizer");
		recognizer = prefix + "_" + recognizer;
		kv.setProperty ("recognizer", recognizer);

		String interpreter = kv.getProperty ("interpreter");
		interpreter = prefix + "_" + interpreter;
		kv.setProperty ("interpreter", interpreter);

		String synthesizer = kv.getProperty ("synthesizer");
		synthesizer = prefix + "_" + synthesizer;
		kv.setProperty ("synthesizer", synthesizer);
	}

};
