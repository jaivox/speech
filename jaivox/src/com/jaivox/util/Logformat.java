package com.jaivox.util;

import java.util.logging.*;
import java.util.*;
import java.text.*;

/**
 * A custom formatter for log messages. This can be modified to make the
 * messages more suitable. To do so, simply change the information that
 * is written out from the LogRecord in the format method.
 */

public class Logformat extends java.util.logging.Formatter {

	public static DateFormat simple = new SimpleDateFormat ("yyMMddhhmmss: ");

	public String format (LogRecord R) {
		StringBuffer sb = new StringBuffer ();
		String datetime = simple.format (new Date ());
		sb.append (datetime);
		Level l = R.getLevel ();
		if (l == Level.SEVERE) sb.append ("Critical: ");
		String msg = R.getMessage ();
		sb.append (msg);
		sb.append ("\n");
		String formatted = new String (sb);
		return formatted;
	}
}
