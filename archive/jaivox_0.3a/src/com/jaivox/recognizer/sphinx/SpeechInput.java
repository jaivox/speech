
package com.jaivox.recognizer.sphinx;

import java.io.*;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
/**
//Adapted from Sphinx's HelloNGram application
//copyright notice for HelloNGram appears below
* Copyright 1999-2004 Carnegie Mellon University.
* Portions Copyright 2004 Sun Microsystems, Inc.
* Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
* All Rights Reserved.  Use is subject to license terms.
*
* See the file "license.terms" for information on usage and
* redistribution of this file, and for a DISCLAIMER OF ALL
* WARRANTIES.
*
 * HelloNGram is a simple demo built using Sphinx-4.
 * This version accepts speech based on a language model developed from
 * questions created in another jaivox module.
 * This version wraps a server that is then used for communication
 * with other agents.
*/

public class SpeechInput {

	String work1 = "send sphinx_0 {action: interpret, from: ";
	String work2 = ", to: ";
	String work3 = ", message: ";

/**
 * Initialize the sphinx program that handles speech recognition
@param server
@param configfile	xml configuration file describing the sphinx configuration
 */

    public SpeechInput (SphinxServer server, String inter, 
    		ConfigurationManager cm) {
//    		String configfile) {
//        ConfigurationManager cm;

        // cm = new ConfigurationManager(SpeechInput.class.getResource("speechinput.config.xml"));
        // cm = new ConfigurationManager(SpeechInput.class.getResource(configfile));
       // allocate the recognizer
        System.out.println("Loading...");
        Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
        recognizer.allocate();

        // start the microphone or exit if the programm if this is not possible
        Microphone microphone = (Microphone) cm.lookup("microphone");
        if (!microphone.startRecording()) {
            System.out.println("Cannot start microphone.");
            recognizer.deallocate();
            System.exit(1);
        }

        printInstructions();

        // loop the recognition until the programm exits.
        while (true) {
            System.out.println("Start speaking. Press Ctrl-C to quit.\n");

            Result result = recognizer.recognize();

            if (result != null) {
                String resultText = result.getBestResultNoFiller();
                System.out.println("You said: " + resultText + '\n');
                String name = server.getServerId ();
                String message = work1 + name + work2 + inter + work3 +
                	"\"" + resultText +"\"}";
                server.execute (message);
                try {
                	Thread.sleep (4000);
                }
                catch (Exception e) {
                	System.out.println (e.toString ());
                	break;
                }
            } else {
                System.out.println("I can't hear what you said.\n");
            }
        }
    }

   /** Prints out what to say for this demo. */
   private static void printInstructions() {
       System.out.println ("Sample sentences: are in q.text\n");
		try {
			BufferedReader in = new BufferedReader (new FileReader ("q.text"));
			String line;
			while ((line = in.readLine ()) != null) {
				int toss = (int)(Math.random ()*100.0);
				if (toss > 85) System.out.println (line);
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
   }
}
