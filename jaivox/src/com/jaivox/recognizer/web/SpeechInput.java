
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

