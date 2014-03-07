/*
   Jaivox version 0.7 March 2014
   Copyright 2010-2014 by Bits and Pixels, Inc.

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
package com.jaivox.synthesizer.web;

import com.jaivox.util.Log;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Vector;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


/**
 * synthesizes speech in various languages using Google's Text to Speech
 * System. Currently (August 2013) is not through an official API (as far
 * as we know), though the translation system that uses it has an official
 * API. When an official API is released, we expect the URL below to
 * include an API key (you may have to pay for it, as you have to with
 * the translation API.)
 * 
 * This version requires the javazoom jLayer1.0.1 library from
 * http://www.javazoom.net/ 
 */

public class Synthesizer extends com.jaivox.synthesizer.Synthesizer {

	public String location = "http://translate.google.com/translate_tts?";
	public String target = "tl=";
	public String tail = "&ie=UTF-8&q=";
	public String agent = "Mozilla/5.0";
	public String defaultLanguage = "en";

/**
 * Create a synthesizer. The language of the synthesizer is given in
 * the property "ttslang"
 * @param kv 
 */
	public Synthesizer (Properties kv) {
		Log.info ("Synthesizer created");
		String language = kv.getProperty ("ttslang");
		if (language != null) defaultLanguage = language;
	}


	public Synthesizer () {
		Log.info ("Synthesizer created");
	}

	
/**
 * Speak a message in the specified language. The language codes used are
 * not the same as for the speech recognizer and may change at any time. Now (August 2013)
 * the language codes may be found at
 * https://sites.google.com/site/tomihasa/google-language-codes
 * @param lang
 * @param message
 * @return
 */
	@Override
	public boolean speak (String lang, String message) {
		try {
			int maxchar = 80;
			String messages [] = breakup (message, maxchar);
			for (int i=0; i<messages.length; i++) {
				String encoded = URLEncoder.encode (message, "UTF-8");
				String request = location + target + lang + tail + encoded;
				Log.info ("Request: "+request);
				URL url = new URL (request);

				HttpURLConnection link = (HttpURLConnection)url.openConnection ();
				link.setRequestProperty ("User-Agent", agent);

				BufferedInputStream stream = new BufferedInputStream (link.getInputStream ());
				play (stream);
			}
			
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
	String [] breakup (String message, int maxchar) {
		int start = 0;
		int n = message.length ();
		if (n < maxchar) {
			String result [] = new String [1];
			result [0] = message;
			return result;
		}
		Vector <String> hold = new Vector <String> ();
		// will just break up at ". "
		do {
			int pos = message.indexOf (". ", start);
			if (pos != -1) {
				hold.add (message.substring (start, pos+1));
				start = pos+2;
			}
			else break;
		} while (start < n);
		if (start < n) hold.add (message.substring (start));
		int m = hold.size ();
		String results [] = hold.toArray (new String [m]);
		return results;
	}

/**
 * speak the given message in the default language
@param message
 */
	@Override
	public boolean speak (String message) {
		try {
			String lang = defaultLanguage;
			String encoded = URLEncoder.encode (message, "UTF-8");
			String request = location + target + lang + tail + encoded;
			Log.info ("Request: "+request);
			URL url = new URL (request);
			
			HttpURLConnection link = (HttpURLConnection)url.openConnection ();
			link.setRequestProperty ("User-Agent", agent);
			
			BufferedInputStream stream = new BufferedInputStream (link.getInputStream ());
			play (stream);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
	// uses Javazoom's player
	public void play(InputStream sound) throws JavaLayerException {
		new Player(sound).play();
	}

};

