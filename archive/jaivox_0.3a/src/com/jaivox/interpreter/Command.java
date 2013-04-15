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
