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

package com.jaivox.recognizer.sphinx;

import com.jaivox.agent.MessageData;
import com.jaivox.agent.Responder;
import com.jaivox.agent.Session;

/**
 * This class is a simple responder, since the Sphinx agent mostly just
 * sends recognized strings to something else. It is not much different
 * from the agent.TestResponder. It is kept here since the SpeechSession
 * requires some responder.
 */

public class SphinxResponder extends Responder {

	public static final String
		terminateRequest = "JviaTerminate",
		uwhoRequest = "JviaWho",
		fetchRequest = "JviaFetch";
	
	static final int packetSize = 1024;
		
	public SphinxResponder () {
		super ();
	}
	
	public SphinxResponder (Session own) {
		super (own);
	}
	
	public MessageData respond (String request) {
		MessageData req = new MessageData (request);
		MessageData jd = new MessageData ();
		String action = Session.responseMessage;
		jd.setValue ("action", action);
		String from = getOwner().getSid ();
		String to = "undetermined";
		if (!req.isValid ()) {
			action = Session.invalidMessage;
		}
		to = req.getValue ("from");
		action = req.getValue ("action");
		jd.setValue ("to", to);
		jd.setValue ("from", from);
		// String actreq = req.getValue ("message");
		if (action.equals ("inform")) {
			System.out.println ("Received inform message, end of chain");
			System.out.println ("Informed: "+request);
			jd.setValue ("action", Session.finishedMessage);
		}
		else if (action.equals (terminateRequest)) {
			jd.setValue ("action", Session.terminateMessage);
		}
		else if (action.equals (uwhoRequest)) {
			jd.setValue ("action", Session.responseMessage);
			String s = "i am "+ getOwner ().getSid ();
			jd.setValue ("message", s);
		}
		else if (action.equals (Session.responseMessage)) {
			jd.setValue ("action", Session.finishedMessage);
		}
		else {
			jd.setValue ("action", Session.invalidMessage);
		}
		return jd;
	}
	
}
