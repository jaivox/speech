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
package com.jaivox.synthesizer.espeak;

import com.jaivox.util.Log;
import java.util.Properties;

/**
 * The espeak synthesizer simply uses an espeak command
 * @author thomas
 */
public class Synthesizer {

	public static String cmd = "espeak -m -v ";
	public static String defaultLanguage = "en";

/**
 * Create a Synthesizer using information in a Properties class and
 * information about a base directory. This form is included only for
 * compatibility with other similar constructors in other packages.
@param base
@param kv
 */
	public Synthesizer (String base, Properties kv) {
		Log.info ("Synthesizer created");
		String language = kv.getProperty ("ttslang");
		if (language != null) defaultLanguage = language;
	}

/**
 * Create the default synthesizer. This is the form that is most often
 * used to construct the Synthesizer class.
 */

	public Synthesizer () {
		Log.info ("Synthesizer created");
	}


/**
 * Speak the given message using the given language code.
 * See the espeak voices directory for available voices
 * (for example it could be in /usr/share/espeak-data/voices/)
@param message
 */
	public boolean speak (String language, String message) {
		try {
			if (message.indexOf (" ") != -1)
				message = message.replaceAll (" ", "&nbsp;");
			String input = message;
			if (!message.startsWith ("\"")) {
				input = "\"" + message + "\"";
			}
			String command = cmd + language + " " + input;
			System.out.println ("Command: "+command);
			Runtime.getRuntime ().exec (command).waitFor ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
/**
 * Speak using the default language. This can be set in the properties
 * used when creating this class.
 * @param message
 * @return
 */

	public boolean speak (String message) {
		try {
			String language = defaultLanguage;
			if (message.indexOf (" ") != -1)
				message = message.replaceAll (" ", "&nbsp;");
			String input = message;
			if (!message.startsWith ("\"")) {
				input = "\"" + message + "\"";
			}
			String command = cmd + language + " " + input;
			Log.info ("espeak rinvoked: "+command);
			Runtime.getRuntime ().exec (command).waitFor ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
};

