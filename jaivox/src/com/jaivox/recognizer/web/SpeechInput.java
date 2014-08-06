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

package com.jaivox.recognizer.web;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import com.jaivox.util.Log;

/**
 * SpeechInput handles recognition tasks. In the current implementation,
 * web recognition is done using Google's (unofficial as of August 2013)
 * speech api. When the api becomes official, we expect the addition of
 * an API key to the web location.
 * 
 */

public class SpeechInput {
	String address1 = "https://www.google.com/speech-api/v2/recognize?output=json&lang=";
	// String address1 = "https://www.google.com/speech-api/v2/recognize?lang=";
	// put en-us in between these
	String keystub = "&key=";
	String key;
	String address2 = "&client=chromium";
	String agent = "Mozilla/5.0";
	String type = "audio/x-flac; rate=16000";

	String utt = "transcript";
	String first = "\":\"";
	String second = "\"";

/**
 * API key from Google is needed to use the Speech API
 * @param keystring 
 */
	public SpeechInput (String keystring) {
		if (keystring == null || keystring.trim ().length () == 0 || keystring.equals ("xxxx")) {
			Log.severe ("Google recognizer now requies an API key.");
			System.out.println ("Please see http://www.chromium.org/developers/how-tos/api-keys");
			System.out.println ("Please read instructions there, you need to join the ");
			System.out.println ("Chromium developer's group.");
			System.out.println ("Add this key as the value of \"googleapikey\" in your conf file");
			key = "";
			return;
		}
		key = keystring;
	}

/**
 * Recognize a particular recording, in flac format, assuming that the
 * recording is in the designated language. Language codes for recognition
 * are determined (in this case) by Google.
 * @param flacfile
 * @param lang
 * @return
 */
/*
  Possible format of result:
  
  {"result":[]}
	{"result":[{"alternative":[
	{"transcript":"is Avenue of the quickest highway"},
	{"transcript":"is Avenue the quickest highway"},
	{"transcript":"is Avenue of the quickest Highway"},
	{"transcript":"is Avenue over the quickest highway"},
	{"transcript":"is Avenue old the quickest highway"}],"final":true}],
	"result_index":0}
 */
	public String recognize (String flacfile, String lang) {
		try {
			if (key.equals ("")) {
				Log.severe ("Please use your valid API key to create SpeechInput");
				return "Error";
			}
			String address = address1 + lang +keystub + key + address2;
			URL url = new URL (address);
			URLConnection urlConnection = url.openConnection ();
            urlConnection.setUseCaches(false);
			HttpURLConnection link = (HttpURLConnection) urlConnection;
            link.setInstanceFollowRedirects (false);
			link.setRequestMethod ("POST");
			urlConnection.setDoOutput (true);
			link.setRequestProperty ("User-Agent", agent );
			link.setRequestProperty ("Content-Type", type);
			DataInputStream inStream = new DataInputStream (
				new FileInputStream (flacfile));
			DataOutputStream outStream = new DataOutputStream (
												link.getOutputStream());
			byte buffer [] = new byte[4096];
			int len;
			while ((len = inStream.read (buffer)) > 0) {
				outStream.write(buffer, 0, len);
			}
			outStream.close ();
			inStream.close ();
			Thread.sleep (100);
			String recognized = "";

			int responseCode = link.getResponseCode ();
			if (responseCode == 200) {
				InputStream resultStream = link.getInputStream ();
				BufferedReader in = new BufferedReader (
					new InputStreamReader (resultStream));
				StringBuffer sb = new StringBuffer ();
				String line = null;
				while ((line = in.readLine ()) != null) {
					sb.append (line);
					sb.append ("\n");
				}
				in.close ();
				String result = new String (sb);
				Log.info (result);
				int pos = result.indexOf (utt);
				if (pos == -1) {
					Log.severe (flacfile+"\tNo transcript in result");
					return "error";
				}
				int qos = result.indexOf (first, pos+1);
				if (qos == -1) {
					Log.severe (flacfile+"\tNo first result");
					return "error";
				}
				int ros = result.indexOf (second, qos+3);
				if (ros == -1) {
					Log.severe (flacfile+"\tNo termination for first result");
					return "error";
				}
				recognized = result.substring (qos+3, ros);
				Log.info (flacfile+"\t"+recognized);
			}
			return recognized;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "error";
		}
	}
};

