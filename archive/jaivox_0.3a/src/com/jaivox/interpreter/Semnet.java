
package com.jaivox.interpreter;

import java.io.*;
import java.util.*;

import com.jaivox.util.Log;

/**
 * Semnet constructs a semantic network, specific to a particular application,
 * that relates different words that occur in that application. This network
 * is used to judge the general subject matter of a series of questions.
 */

public class Semnet {

	Properties kv;
	String datadir;
	TreeMap <String, Snode> nodes;

/**
 * Creates the semantic net. This is a network of related words.
 * When new strings are seen in the context, the network is updated.	
@param dir	directory containing data files
@param info	Info class of data
@param pp	properties from interact
 */
	public Semnet (String dir, Info info, Properties pp) {
		datadir = dir;
		kv = pp;
		nodes = new TreeMap <String, Snode> ();
		loadspecs (info);
		loadfields ();
		// shownodes ();
	}

	void loadspecs (Info info) {
		try {
			Snode root = new Snode (null, "root", "root");
			nodes.put ("root", root);
			Snode table = null;
			TreeMap <String, Infonode> specs = info.specs;
			Set <String> keys = specs.keySet ();
			for (Iterator <String> it = keys.iterator (); it.hasNext (); ) {
				String key = it.next ();
				Infonode inode = specs.get (key);
				String data = inode.name;
				String type = inode.tagvals ("type");
				if (type.equals ("table")) {
					Snode node = new Snode (root, data, type);
					nodes.put (data, node);
					table = node;
				}
				else {
					Snode node = new Snode (table, data, type);
					nodes.put (data, node);
					// load attributes also
					if (type.equals ("field")) {
						String attrs [] = inode.tagvalarray ("attributes");
						for (int i=0; i<attrs.length; i++) {
							String at = attrs [i];
							Snode atnode = new Snode (node, at, "attribute");
							nodes.put (at, atnode);
						}
					}
					// do the other fields
					LinkedHashMap <String, String []> tagval = inode.tagval;
					Set <String> tags = tagval.keySet ();
					for (Iterator<String> of = tags.iterator (); of.hasNext (); ) {
						String tag = of.next ();
						if (tag.equals (tag.toLowerCase ())) continue;
						// deal with only upper case Tags
						String vals [] = tagval.get (tag);
						for (int i=0; i<vals.length; i++) {
							String w = vals [i].toLowerCase ();
							if (nodes.get (w) != null) continue; // don't overwrite
							Snode vnode = new Snode (node, w, "vals");
							nodes.put (w, vnode);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void loadfields () {
		Set <String> keys = nodes.keySet ();
		Vector <Snode> fields = new Vector <Snode> ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Snode node = nodes.get (key);
			if (node.t.equals ("field")) {
				fields.add (node);
			}
		}
		String datafile = kv.getProperty ("data_file");
		for (int i=0; i<fields.size (); i++) {
			Snode node = fields.elementAt (i);
			if (datafile.startsWith (node.d)) {
				String filename = datadir + datafile;
				loadfile (node, filename);
			}
		}
	}

	void loadfile (Snode parent, String filename) {
		try {
			// Log.fine ("Loading file "+filename);
			BufferedReader in = new BufferedReader (new FileReader (filename));
			String line;
			while ((line = in.readLine ()) != null) {
				line = line.toLowerCase ();
				StringTokenizer st = new StringTokenizer (line, ",\t\r\n");
				if (!st.hasMoreTokens ()) continue;
				String first = st.nextToken ().trim ().toLowerCase ();
				// will overwrite duplicates
				Snode node = new Snode (parent, first, "data");
				nodes.put (first, node);
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	void shownodes () {
		Set <String> keys = nodes.keySet ();
		int i = 1;
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Snode node = nodes.get (key);
			Log.finest (""+i+"\t"+node.toString ());
			i++;
		}
	}

/**
 * Updates the semantic network based on a query or response
@param line
 */
	
	public void execute (String line) {
		String text = line.toLowerCase ();
		// Log.fine ("Checking: "+text);
		Set <String> keys = nodes.keySet ();
		int startactivation = 4;
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			String padded = " "+key+" ";	// to get whole strings
			Snode node = nodes.get (key);
			if (text.indexOf (padded) != -1) {
				// Log.fine ("Found node for "+key);
				node.propagate (startactivation);
			}
		}
		// showactivations ();
		updatepasts ();
	}


	void execute (TreeMap <String, Integer> starts) {
		Set<String> keys = starts.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Integer I = starts.get (key);
			int val = I.intValue ();
			Snode node = nodes.get (key);
			if (node != null) {
				node.propagate (val);
			}
		}
		showactivations ();
		updatepasts ();
	}

	void showactivations () {
		TreeMap <Integer, Vector<Snode>> acts = new TreeMap <Integer, Vector<Snode>> ();
		Set <String> keys = nodes.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Snode node = nodes.get (key);
			int num = node.past + node.now;
			if (num == 0) continue;
			Integer N = new Integer (-num);
			Vector<Snode> vals = acts.get (N);
			if (vals == null) vals = new Vector<Snode> ();
			vals.add (node);
			acts.put (N, vals);
		}
	}

	void updatepasts () {
		Set <String> keys = nodes.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Snode node = nodes.get (key);
			int num = node.past + node.now;
			if (num == 0) continue;
			if (node.now == 0) continue;
			node.update ();
		}
	}

/**
 * Picks a topic that seems to be important, based on the activations
 * in the semantic network.
@param count
@return
 */
	
	public String picktopic (int count) {
		TreeMap <Integer, Vector<Snode>> acts = new TreeMap <Integer, Vector<Snode>> ();
		Set <String> keys = nodes.keySet ();
		for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
			String key = it.next ();
			Snode node = nodes.get (key);
			int num = node.past;
			if (num == 0) continue;
			Integer N = new Integer (-num);
			Vector<Snode> vals = acts.get (N);
			if (vals == null) vals = new Vector<Snode> ();
			vals.add (node);
			acts.put (N, vals);
		}
		Set <Integer> ikeys = acts.keySet ();
		int found = 0;
		StringBuffer sb = new StringBuffer ();
		for (Iterator<Integer> it = ikeys.iterator (); it.hasNext (); ) {
			Integer N = it.next ();
			Vector <Snode> vals = acts.get (N);
			for (int i=0; i<vals.size (); i++) {
				Snode node = vals.elementAt (i);
				if (found > 0) sb.append (" or ");
				sb.append ("\""+node.d+"\"");
				found++;
				if (found >= count) break;
			}
			if (found >= count) break;
		}
		String result = new String (sb);
		// Log.fine ("picktopic: "+result);
		return result;
	}

};



