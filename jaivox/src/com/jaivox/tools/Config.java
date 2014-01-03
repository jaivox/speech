/*
   Jaivox version 0.6 December 2013
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

import com.jaivox.interpreter.Utils;
import com.jaivox.util.Log;
import com.jaivox.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

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
 * key value table of properties from the configuration file
 */
	public static Properties kv;

/**
 * all locations are relative to location of the input file
 */
	public String basedir;
	
/**
 * file separator
 * see System.Properties ()
 * file.separator	File separator ("/" on UNIX)
 */
 
 	String Sep;
	
/**
 * Property names sorted by length. This is to make sure that longer
 * keys are replaced first when generating from a template
 */
	String sortedKeys [];
	
/**
 * are the specs valid?
 */
	static boolean Valid = true;

/**
 * Create the key value properties from the given configuration
 * file name.
@param filename
 */
	public Config (String filename) {
		kv = new Properties ();
		Sep = System.getProperty ("file.separator");
        loadDefaultValues ();
		boolean ok = loadProperties (filename);
		if (!ok) {
			Valid = false;
			return;
		}
		getBaseDir (filename);
        Log.info ("Base directory is "+basedir);
		fixRequiredDirectories ();
		createSortedKeys ();
		setAgentNames ();
 		kv.list (System.out);
	}
	
/**
 * Create the Config starting with a properties file. Note that Base should
 * be specified in the properties.
 */
	
	public Config (Properties pp) {
		kv = pp;
		Sep = System.getProperty ("file.separator");
        loadDefaultValues ();
		String path = kv.getProperty ("Base");
		if (path == null) {
			Log.severe ("No Base path specified.");
			Valid = false;
			return;
		}
		File G = new File (path);
		if (G.exists ()) basedir = G.getAbsolutePath ();
		if (!basedir.endsWith (Sep)) basedir = basedir + Sep;
		kv.setProperty ("Base", basedir);
        Log.info ("Base directory is "+basedir);
		fixRequiredDirectories ();
		createSortedKeys ();
		setAgentNames ();
 		kv.list (System.out);
	}
	
/**
 * Load properties from file
 */	
	boolean loadProperties (String filename) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			kv.load (in);
			return true;
		}
		catch (Exception e) {
			Log.severe (e.toString ());
			return false;
		}
	}

/**
 * Check if configuration looks ok
 * @return 
 */
	public boolean isValid () {
		return Valid;
	}

    void loadDefaultValues () {
        kv.setProperty ("batch", "batch");
        kv.setProperty ("console", "console");
        kv.setProperty ("live", "live");
        kv.setProperty ("lang", "en-US");
        kv.setProperty ("ttslang", "en");
        kv.setProperty ("log_level", "info");
        kv.setProperty ("common_words", "common_en.txt");
        kv.setProperty ("freettsjar", "/usr/local/freetts/lib/freetts.jar");
        kv.setProperty ("onedirectory", "yes");
        kv.setProperty ("overwrite_files", "yes");
        kv.setProperty ("penn_tags", "penn.txt");
    }
	
	void getBaseDir (String configfile) {
		File F = new File (configfile);
		String path = F.getAbsolutePath ();
		int pos = path.lastIndexOf (Sep);
		if (pos !=1) path = path.substring (0, pos+1);
		System.out.println ("basedir path candidate: "+path);
		String temp = kv.getProperty ("Base");
		if (temp != null) {
			if (temp.startsWith (Sep)) {
				path = temp;
			}
			else {
				path = path + temp;
			}
		}
		File G = new File (path);
		System.out.println ("getBaseDir:" + path);
		if (G.exists ()) basedir = G.getAbsolutePath ();
		System.out.println ("getBaseDir: basedir = "+basedir);
		if (!basedir.endsWith (Sep)) basedir = basedir + Sep;
		kv.setProperty ("Base", basedir);
	}
	
	// need common, source and destination
    
	void fixRequiredDirectories () {
		fixDirectory ("common");
		fixDirectory ("source");
		fixDirectory ("destination");
		if (!checkExists ("common")) {
			Valid = false;
			return;
		}
		if (!checkExists ("source")) {
			Valid = false;
			return;
		}
		if (!createDirectory ("destination")) {
			Valid = false;
			return;
		}
	}
	
	void fixDirectory (String key) {
		String val = kv.getProperty (key);
		if (val == null) {
			Log.severe ("Required "+key+" not found.");
			Valid = false;
			return;
		}
		if (!val.startsWith (Sep)) {
			val = basedir + val;
		}
		if (!val.endsWith (Sep)) {
			val = val + Sep;
		}
		kv.setProperty (key, val);
	}
	
	boolean checkExists (String key) {
		String val = kv.getProperty (key);
		File F = new File (val);
		if (!F.exists ()) {
			Log.severe ("Required file/directory "+val+" does not exist.");
			return false;
		}
		return true;
	}
	
	boolean createDirectory (String key) {
		String val = kv.getProperty (key);
		File F = new File (val);
		if (!F.exists ()) {
			boolean ok = F.mkdirs ();
			if (!ok) {
				Log.severe ("Could not create "+key+" directory "+val);
				return ok;
			}
		}
		return true;
	}
	
/**
 * Check whether a property is defined. If the property is not defined,
 * an INFO message is logged, but Valid is not modified.
 * @param key
 * @return 
 */
	
	public boolean checkProperty (String key) {
		String val = kv.getProperty (key);
		if (val == null) {
			Log.info ("Value for "+key+" not found.");
			return false;
		}
		return true;
	}

/**
 * Get the property for a specific key. If the values is not found, then
 * Valid is set to false so that subsequent calls to isValid returns false.
 * @param key
 * @return 
 */

	public String getProperty (String key) {
		String val = kv.getProperty (key);
		if (val == null) {
			Log.warning ("Required value for "+key+" not in configuration.");
			Valid = false;
			return null;
		}
		return val;
	}
	
	void createSortedKeys () {
		Set<String> keys = kv.stringPropertyNames ();
		int n = keys.size ();
		String okeys [] = new String [n];
		Pair op [] = new Pair [n];
		int pi = 0;
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			okeys [pi] = key;
			op [pi] = new Pair (pi, -key.length ());
			pi++;
		}
		Utils.quicksortpointy(op, 0, n-1);
		sortedKeys = new String [n];
		for (int i=0; i<n; i++) {
			Pair p = op [i];
			String key = okeys [p.x];
			sortedKeys [i] = key;
		}
	}
	
/**
 * Get the sorted keys from sortedKeys
 */
	public String [] getSortedKeys () {
		if (sortedKeys == null) createSortedKeys ();
		return sortedKeys;
	}
	
/**
 * Agent names are needed for multiagent situations
 */
	void setAgentNames () {
        String onedir = kv.getProperty ("onedirectory");
        if (onedir.equals ("yes")) return;
        
        String rname = kv.getProperty ("dir_recognizer");
        String iname = kv.getProperty ("dir_interpreter");
        String sname = kv.getProperty ("dir_synthesizer");
        
        if (rname.equals (iname)) iname = iname+"_a";
        if (iname.equals (sname)) sname = iname+"_b";
        if (rname.equals (sname)) sname = sname+"_c";
        
        kv.setProperty ("namerecognizer", rname);
        kv.setProperty ("nameinterpreter", iname);
        kv.setProperty ("namesynthesizer", sname);
	}

};
