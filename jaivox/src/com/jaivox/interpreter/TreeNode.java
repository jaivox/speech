package com.jaivox.interpreter;

import java.util.ArrayList;

public class TreeNode {
	int level;
	String tag;
	ArrayList <TreeNode> children;
	
	static char indent = '.';
	
	public TreeNode (String raw) {
		char a = raw.charAt (0);
		if (a == indent) {
			int count = 0;
			for (int i=0; i<raw.length (); i++) {
				a = raw.charAt (i);
				if (a == indent) count++;
				else break;
			}
			level = count;
			tag = raw.substring (count);
		}
		else {
			level = 0;
			tag = raw;
		}
	}
	
	void add (TreeNode n) {
		if (children == null) children = new ArrayList <TreeNode> ();
		children.add (n);
	}
	
	TreeNode get (int i) {
		if (children == null) return null;
		if (i >= children.size ()) return null;
		return children.get (i);
	}
	
	int nchildren () {
		if (children == null) return 0;
		else return children.size ();
	}
	
	void print () {
		StringBuilder sb = new StringBuilder ();
		for (int i=0; i<level; i++) sb.append ("  "); // two spaces
		sb.append (tag);
		System.out.println (new String (sb));
		if (children == null) return;
		int n = children.size ();
		for (int i=0; i<n; i++) {
			TreeNode child = get (i);
			child.print ();
		}
	}
	
}
