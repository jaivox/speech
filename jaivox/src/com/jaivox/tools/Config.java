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

package com.jaivox.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Reads a configuration file and sets relevant values in a key value
 * hash table kv.
 * In addition to various locations, the configuration file specifies
 * the different components used for user interaction
 * 
 * iomode: live, batch, console
 * directories: one or multiple
 * recognizer: sphinx or web
 * synthesizer: festival or one of (freetts, espeak, web)
 */

public class Config {

/**
 * key value table of properties from the configuation file
 */
	public static Properties kv;

/**
 * all locations are relative to location of the input file
 */
	public String basedir;

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
			File F = new File (filename);
			basedir = F.getParent ();
			StringBuffer sb = new StringBuffer ();
			sb.append (System.getProperty ("file.separator"));
			String sep = new String (sb);
			if (!basedir.endsWith (sep)) basedir = basedir + sep;
			kv.setProperty ("Base", basedir);
			// System.out.println ("Base directory: "+basedir);
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
