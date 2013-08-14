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
/**
 * TestResponder responds to messages in trivial ways. It is used
 * to test agents in terms of connectivity and a few simple requests.
 */

package com.jaivox.agent;

/**
 * TestResponder responds to messages in trivial ways. It is used
 * to test agents in terms of connectivity and a few simple requests.
 */

public class TestResponder extends Responder {

/**
 * terminateRequest and uwhoRequest are actions that
 * are handled by this responder.
 */
	public static final String
		terminateRequest = "JviaTerminate",
		uwhoRequest = "JviaWho";
	
		// fetchRequest = "JviaFetch";
	
	static final int packetSize = 1024;
		
/**
 * Create a TestResponder. Normally created when a session is created.
 */
	public TestResponder () {
		super ();
	}
	
/**
 * Create a responder with a specific session.
@param own
 */
	
	public TestResponder (Session own) {
		super (own);
	}
	
/**
 * Respond to various requests. The first step is to create a response
 * message by switching the from and to fields of the incoming message.
 * Then we get the action specified in the request and handle the
 * action accordingly.
@param request
 */
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
		if (action.equals (terminateRequest)) {
			jd.setValue ("action", Session.terminateMessage);
		}
		else if (action.equals (uwhoRequest)) {
			jd.setValue ("action", Session.responseMessage);
			String s = "i am "+getOwner ().getId ();
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
