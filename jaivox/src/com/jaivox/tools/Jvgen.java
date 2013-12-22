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

import com.jaivox.util.Log;

/**
 * Main program for generating an application.
 * If generated questions have been modified by hand, then call
 * "com.jaivox.tools.Jvgen whatever.conf -update" 
 * instead of "com.jaivox.tools.Jvgen whatever.conf"
 * 
 * Most of the work of generating files is done in Generator.
 */

public class Jvgen {

	public static void main (String args []) {
		new Log ();
		Log.setLevelByName ("INFO");
		if (args.length == 0) {
			System.out.println ("syntax \"java com.jaivox.tools.Jvgen conf_file [-update]\"");
			return;
		}
		Generator gen = new Generator (args [0]);
		if (!gen.isValid ()) {
			System.out.println ("Invalid specifications, generation is incomplete.");
			return;
		}
		if (args.length > 1) {
			if (args[1].toLowerCase ().equals ("-update")) {
				gen.updateLmQuestions ();
			}
			else {
				System.out.println ("\"Jvgen conf_file -update\" is the only option.");
			}
		}
		else {
			gen.createQuestions ();
			gen.createCustomCommands ();
		}
	}
};
