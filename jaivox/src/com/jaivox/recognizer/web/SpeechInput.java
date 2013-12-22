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

	String address1 = "http://www.google.com/speech-api/v1/recognize?lang=";
	// put en-us in between these
	String address2 = "&client=chromium";
	String agent = "Mozilla/5.0";
	String type = "audio/x-flac; rate=16000";

	String utt = "utterance";
	String first = "\":\"";
	String second = "\",\"";

	public SpeechInput () {
	}

/**
 * Recognize a particular recording, in flac format, assuming that the
 * recording is in the designated language. Language codes for recognition
 * are determined (in this case) by Google.
 * @param flacfile
 * @param lang
 * @return
 */
	public String recognize (String flacfile, String lang) {
		try {
			String address = address1 + lang + address2;
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
				int pos = result.indexOf (utt);
				if (pos == -1) {
					Log.severe (flacfile+"\tNo utt result");
					return "error";
				}
				int qos = result.indexOf (first, pos+1);
				if (qos == -1) {
					Log.severe (flacfile+"\tNo first result");
					return "error";
				}
				int ros = result.indexOf (second, qos+1);
				if (ros == -1) {
					Log.severe (flacfile+"\tNo second result");
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

