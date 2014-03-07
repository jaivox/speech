/*
   Jaivox version 0.7 March 2014
   Copyright 2010-2014 by Bits and Pixels, Inc.

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
