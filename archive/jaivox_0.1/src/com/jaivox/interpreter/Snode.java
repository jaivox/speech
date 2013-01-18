/**
 * An Snode is a semantic node. It occurs within the semantic network
 * managed by Semnet. It is used to record the activation of each
 * string that occurs in the semantic network.
 */

package com.jaivox.interpreter;

public class Snode {

	String d;	// data
	String t;	// type
	Snode p;	// parent

	int past;
	int now;	// scores

	public Snode (Snode parent, String data, String type) {
		p = parent;
		d = data;
		t = type;
	}

	void clearall () {
		past = 0;
		now = 0;
	}

	void clearpast () {
		past = 0;
	}

	void clearnow () {
		now = 0;
	}

	void update () {
		past += now;
		now = 0;
	}

	void propagate (int activation) {
		now += activation;
		if (activation > 0 && p != null) p.propagate (activation-1);
	}

	public String toString () {
		String pd = "0";
		if (p != null) pd = p.d;
		return (d+" "+t+" < "+pd);
	}
};
