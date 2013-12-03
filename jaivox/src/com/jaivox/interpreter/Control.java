
package com.jaivox.interpreter;

import com.jaivox.util.Log;
import java.util.Properties;
import java.util.Vector;



public class Control {
	
	Interact I;
	Script S;
	Vector <HistNode> Past;
	Vector <String> track;
	Properties kv;
	String lastReason;
	String trackReason;
	static String okay = "Okay";
	
	public Control (Script s) {
		S = s;
		I = S.I;
		kv = S.getKv ();
		lastReason = okay;
		trackReason = okay;
		Past = new Vector <HistNode> ();
		track = new Vector <String> ();
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
	
	public boolean addTrack (String s) {
		Log.finest ("addTrack: "+s);
		track.add (s);
		boolean ok = checkTrack (s);
		return ok;
	}
	
	boolean checkTrack (String s) {
		// see if there are multiple handleInputDirect after last
		// handleInputValue
		int n = track.size ();
		int last = -1;
		for (int i=n-1; i>=0; i--) {
			String item = track.elementAt (i);
			if (item.startsWith ("handleInputValue/")) {
				last = i;
				break;
			}
		}
		if (last == -1) {
			trackReason = "No handleInputValue in track";
			showTrack ();
			Log.finest ("addTrack: "+trackReason);
			return false;
		}
		int count = 0;
		for (int i=last+1; i<n; i++) {
			String item = track.elementAt (i);
			if (item.startsWith ("handleInputDirect/")) count++;
		}
		if (count > 1) {
			trackReason = "Muliple handleInputDirect after last handleInputValue";
			Log.finest ("addTrack: "+trackReason);
			return false;
		}
		return true;
	}
	
	public String getTrackReason () {
		return trackReason;
	}
	
	public void showTrack () {
		int n = track.size ();
		System.out.println ("Track contents:");
		for (int i=0; i<n; i++) {
			System.out.println (track.elementAt (i));
		}
		System.out.println ();
	}
	
}
