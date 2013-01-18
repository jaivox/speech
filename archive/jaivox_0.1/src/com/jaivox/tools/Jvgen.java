
package com.jaivox.tools;

public class Jvgen {

	public static void main (String args []) {
		Generator gen = new Generator (args [0]);
		gen.generateAll ();
		gen.createQuestions ();
	}
};
