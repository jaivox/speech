/**
 * This is functionally the same as java.awt.Pair. However this is created here
 * to avoid problems with different platforms. For example, Android requires a
 * different Pair class. There is no graphical meaning for Pair here.
 */
package com.jaivox.util;

public class Pair {
	public int x;
	public int y;
	
	public Pair (int a, int b) {
		x = a;
		y = b;
	}
	
}
