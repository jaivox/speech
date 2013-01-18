/*
   Jaivox version 0.3 December 2012
   Copyright 2010-2012 by Bits and Pixels, Inc.

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
package com.jaivox.interpreter;

/**
 * The default command class is a placeholder. In applications that connect
 * to external datbaases and programs, the Command class can be replaced with
 * a derived class that handles the specific situation. See the files and db
 * demos to see a different set of Command classes.
*/

public class Command {

/**
 * Create a Command instance. The default Command is created by the Interpeter.
 * This step can be changed to create a different Command classe.
 */
	public Command () {
		
	}
	
/**
 * Handle a specific command. The default class simply returns a message
 * saying it cannot handle anything. This will be replaced with code to
 * handle specific commands in each application that connects to an external
 * program.
@param p
@return
 */
	
	public String handleCommand (Qapair p) {
		String	result = "Cannot handle the command "+p.command+" Please ask something else.";
		return result;
	}

}
