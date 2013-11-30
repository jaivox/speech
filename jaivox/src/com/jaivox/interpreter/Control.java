
package com.jaivox.interpreter;

import com.jaivox.util.Log;
import java.util.Properties;
import java.util.Vector;



public class Control {
	
	Interact I;
	Script S;
	Vector <HistNode> Past;
	Properties kv;
	String lastReason;
	static String okay = "Okay";
	
	public Control (Script s) {
		S = s;
		I = S.I;
		kv = S.getKv ();
		lastReason = okay;
		Past = new Vector <HistNode> ();
	}
	
	public boolean approves (Vector <HistNode> history) {
		int n = Past.size ();
		if (history.size () == 0) return true;
		int num = toAdd (history);
		if (num == 0) {
			lastReason = "No change to history at position "+n;
			Log.info (lastReason);
			return false;
		}
		else {
			for (int i=0; i<num; i++) {
				HistNode next = history.elementAt (n+i);
				HistNode copy = next.clone ();
				Past.add (copy);
				String s = copy.toString ();
				Log.finest ("Adding "+s+" to Past history");
			}
			lastReason = okay;
			return true;
		}
	}
	
	int toAdd (Vector <HistNode> history) {
		int n1 = Past.size ();
		int n2 = history.size ();
		// should check if the same up to n1
		// but for now assume that we just take n1
		if (n2 > n1) return n2 - n1;
		else return 0;
	}
	
	public String getReason () {
		return lastReason;
	}
	
}
