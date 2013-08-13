

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import java.io.File;
import java.net.URL;
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

public class testSphinx {

	static String dir = "../audio/";
	static String config = "batch.xml";

	public testSphinx () {
		try {
			ConfigurationManager cm = new ConfigurationManager (
					testSphinx.class.getResource (config));
			Recognizer recognizer = (Recognizer) cm.lookup ("recognizer");
			recognizer.allocate ();

			File F = new File (dir);
			String files [] = F.list ();
			TreeMap <String, String> map = new TreeMap <String, String> ();
			for (int i=0; i<files.length; i++) {
				String name = files [i];
				if (!name.endsWith (".wav")) continue;
				String num = "0" + name.substring (4, 6);
				map.put (num, name);
			}

			Set <String> keys = map.keySet ();
			for (Iterator <String> it = keys.iterator (); it.hasNext (); ) {
				String num = it.next ();
				String name = map.get (num);
				String filename = dir + name;
				URL audioURL = new File (filename).toURI ().toURL ();
				AudioFileDataSource dataSource = (AudioFileDataSource) cm.lookup ("audioFileDataSource");
				dataSource.setAudioFile (audioURL, null);

				Result result;
				while ((result = recognizer.recognize ()) != null) {
					String resultText = result.getBestResultNoFiller ();
					System.out.println (num + "\t" + resultText);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public static void main (String args []) {
		new testSphinx ();
	}
};


