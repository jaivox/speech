/*
   Jaivox version 0.5 August 2013
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

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

public class testGoogle {

	static String dir = "../audio/";
	String address = "http://www.google.com/speech-api/v1/recognize?lang=en-us&client=chromium";
	String agent = "Mozilla/5.0";
	String type = "audio/x-flac; rate=16000";

	String utt = "utterance";
	String first = "\":\"";
	String second = "\",\"";

	public testGoogle () {
		try {
			File F = new File (dir);
			String files [] = F.list ();
			TreeMap <String, String> map = new TreeMap <String, String> ();
			for (int i=0; i<files.length; i++) {
				String name = files [i];
				if (!name.endsWith (".flac")) continue;
				String num = "0" + name.substring (4, 6);
				map.put (num, name);
			}

			Set <String> keys = map.keySet ();
			for (Iterator <String> it = keys.iterator (); it.hasNext (); ) {
				String num = it.next ();
				String name = map.get (num);
				String filename = dir + name;
				test (num, filename);
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void test (String num, String flacfile) {
		try {
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
					System.out.println (num+"\tNo utt result");
					return;
				}
				int qos = result.indexOf (first, pos+1);
				if (qos == -1) {
					System.out.println (num+"\tNo first result");
					return;
				}
				int ros = result.indexOf (second, qos+1);
				if (ros == -1) {
					System.out.println (num+"\tNo second result");
					return;
				}
				String recognized = result.substring (qos+3, ros);
				System.out.println (num+"\t"+recognized);
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public static void main (String args []) {
		new testGoogle ();
	}

};


