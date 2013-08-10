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

import com.jaivox.util.Log;

import java.io.BufferedReader;
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
 * key value table of properties from the configuration file
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
        loadDefaultValues (kv);
		try {
			BufferedReader in = new BufferedReader (new FileReader (filename));
			kv.load (in);
			String sep = System.getProperty ("file.separator");
            int pos = filename.indexOf (sep);
            if (pos == -1) {
                // assume filename is given without a path, add "." + sep to filename
                filename = "."+sep+filename;
                pos = filename.indexOf (sep);
            }
            basedir = filename.substring (0, pos+1);
			kv.setProperty ("Base", basedir);
            Log.info ("Base directory is "+basedir);
 			setAgentNames ();
			kv.list (System.out);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
    
    void loadDefaultValues (Properties p) {
        p.setProperty ("batch", "batch");
        p.setProperty ("console", "console");
        p.setProperty ("live", "live");
        p.setProperty ("lang", "en-US");
        p.setProperty ("ttslang", "en");
        p.setProperty ("log_level", "info");
        p.setProperty ("common_words", "common_en.txt");
        p.setProperty ("freettsjar", "/usr/local/freetts/lib/freetts.jar");
        p.setProperty ("onedirectory", "yes");
        p.setProperty ("overwrite_files", "no");
        p.setProperty ("penn_tags", "penn.txt");
    }

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
