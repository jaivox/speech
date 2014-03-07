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
package com.jaivox.interpreter;

import java.util.Vector;
/**
 * The default command class is a placeholder. In applications that connect
 * to external databases and programs, the Command class can be replaced with
 * a derived class that handles the specific situation. See the files and db
 * demos to see a different set of Command classes.
*/

public class Command {

/**
 * Create a Command instance. The default Command is created by the Interpreter.
 * This step can be changed to create a different Command class.
 */
	public Command () {
		
	}
	
/**
 * handle the specific function. The functions are user defined in derived
 * classes. The response here is a placeholder, if the user gets the error
 * message here, that means that there is a function that is user defined and
 * it has not been defined in a derived class.
 * @param f	name of a user-defined function
 * @param question	query from the user
 * @param spec	information about the query, generally grammar
 * @param state	state of the system when this function is called
 * @param history	history of previous actions
 * @return	a string array, result [0] is the answer. optional result [1] is the
 * state to set after completing this function.
 */	
	public String [] handle (String f, String question, String spec, String state, 
		Vector <HistNode> history) {
		String result [] = new String [1];
		result [0] = "Cannot handle the command "+question+" Please ask something else.";
		return result;
	}

}
