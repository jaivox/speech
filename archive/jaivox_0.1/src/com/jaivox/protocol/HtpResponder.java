
/**
 * HtpResponder handles requests using HTTP (hypertext transfer protocol).
 * It is part of a very elementary web server (along with the HtpServer
 * and HtpSession classes included in this package.)
 */

package com.jaivox.protocol;
import com.jaivox.agent.Responder;

import java.io.*;
import java.util.*;
import java.text.*;

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
		// Debug ("method:"+method+" url:"+url);
		if (!method.equals (getMethod)) return notImplemented;
		
		String response = handleHttpRequest (url);
		return response;
	}

	String handleHttpRequest (String url) {
		String stub = url.substring (1);
		String filename = fileLocation + stub;
		// Debug ("serving "+filename);
		
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
			// Debug ("Sending:\n"+response);
			return response;
		}
		catch (Exception e) {
			// Debug ("While serving file: "+e.toString ());
			return myError;
		}
	}
}
