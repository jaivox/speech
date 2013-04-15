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

package com.jaivox.protocol;

import com.jaivox.agent.Responder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;


/**
 * HtpResponder handles requests using HTTP (hypertext transfer protocol).
 * It is part of a very elementary web server (along with the HtpServer
 * and HtpSession classes included in this package.)
 */

public class HtpResponder extends Responder {
	
	static final String fileLocation = "./";
	
	static final String
		getMethod = "GET",
		protocol = "http://localhost:1080/";
	// response codes
	static final String
		httpOk = "200 OK",
		badRequest = "400 Bad Request",
		notFound = "404 Not Found",
		myError = "500 Internal Server Error",
		notImplemented = "501 Not Implemented";

	public HtpResponder () {
		super ();
	}
	
	public HtpResponder (HtpSession own) {
		super ();
		setOwner (own);
	}
	
	/**
	 * Obtain the response to an HTTP request. Due to the simple
	 * nature of this implementation, this responder can only
	 * process requests for a URL.
	 @param The HTTP request
	 @return String, often the contents of a URL
	 */
	
	public String responseString (String request) {
		// will handle only GET requests for html files
		StringTokenizer st = new StringTokenizer (request);
		if (st.countTokens () < 2) return badRequest;

		String method = st.nextToken ();
		String url = st.nextToken ();
		// Log.fine ("method:"+method+" url:"+url);
		if (!method.equals (getMethod)) return notImplemented;
		
		String response = handleHttpRequest (url);
		return response;
	}

	String handleHttpRequest (String url) {
		String stub = url.substring (1);
		String filename = fileLocation + stub;
		// Log.fine ("serving "+filename);
		
		// first add header information
		try {
			StringBuffer sb = new StringBuffer ();
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			while ((line = in.readLine ())!=null) {
				sb.append (line);
				sb.append ("\n");
			}
			in.close ();
			String html = new String (sb);
			int contentLength = html.length ();
			
			// pack output into string buffer
			sb = new StringBuffer ();
			sb.append ("HTTP/1.0 "+httpOk+"\r\n");
			sb.append ("Content-Type: text/html\r\n");
			
			SimpleDateFormat dateFormat = 
				new SimpleDateFormat ("E, d MMM yyyy HH:mm:ss 'CST'");
			String dateString = dateFormat.format (new Date ());
			sb.append ("Date: "+dateString+"\r\n");
			
			sb.append ("Content-length "+contentLength+"\r\n");
			
			sb.append (html+"\r\n");
			
			String response = new String (sb);
			// Log.info ("Sending:\n"+response);
			return response;
		}
		catch (Exception e) {
			// Log.fine ("While serving file: "+e.toString ());
			return myError;
		}
	}
}
