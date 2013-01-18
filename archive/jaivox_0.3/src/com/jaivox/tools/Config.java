/*
   Jaivox version 0.3 December 2012
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
