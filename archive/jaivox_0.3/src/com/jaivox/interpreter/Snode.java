/*
   Jaivox version 0.3 December 2012
   Copyright 2010-2012 by Bits and Pixels, Inc.

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

package com.jaivox.interpreter;

/**
 * An Snode is a semantic node. It occurs within the semantic network
 * managed by Semnet. It is used to record the activation of each
 * string that occurs in the semantic network.
 */

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
