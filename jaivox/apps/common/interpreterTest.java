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

import com.jaivox.interpreter.*;
import com.jaivox.util.*;
import java.io.*;
import java.util.Properties;

public class PATinterpreterTest extends Thread {

	static int port = PATport_interpreter;
	static int waitTime = 5000; // milliseconds

	static String
	connect1 = "connect localhost PATport_recognizer",
	connect2 = "connect localhost PATport_synthesizer",
	who1 = "{action: JviaWho, from: PATnameinterpreter, to: PATnamerecognizer, message: Jviawho}",
	who2 = "{action: JviaWho, from: PATnameinterpreter, to: PATnamesynthesizer, message: Jviawho}";

	public static void main (String args []) {
		InterServer intertest;

		Properties kv = new Properties ();
		kv.setProperty ("data_file", "PATdata_file");
		kv.setProperty ("common_words", "PATcommon_words");
		kv.setProperty ("answer_forms", "PATanswer_forms");
		kv.setProperty ("specs_file", "PATspecs_file");
		kv.setProperty ("questions_file", "PATquestions_file");
		kv.setProperty ("recognizer", "PATnamerecognizer");
		kv.setProperty ("interpreter", "PATnameinterpreter");
		kv.setProperty ("synthesizer", "PATnamesynthesizer");
		kv.setProperty ("grammar_file", "PATgrammar_file");
		
		Log log = new Log ();
		log.setLevelByName ("PATlog_level");
		
		try {
			File currentDir = new File (".");
			String path = currentDir.getCanonicalPath ();
			String dir = path + "/";
			Command cmd = new Command ();
			intertest = new InterServer ("PATnameinterpreter", port, dir, kv, cmd);
			BufferedReader in = new BufferedReader (
				new InputStreamReader (System.in));
			while (true) {
				String line = in.readLine ();
				if (line.startsWith ("connect1")) line = connect1;
				else if (line.startsWith ("connect2")) line = connect2;
				else if (line.endsWith ("who1")) line = line.replaceAll ("who1", who1);
				else if (line.endsWith ("who2")) line = line.replaceAll ("who2", who2);
				intertest.execute (line);
				sleep (waitTime);
				log.info ("Executed: "+line);
				if (line.equals ("terminate")) {
					Log.info ("Ending test program");
					System.exit (1);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
