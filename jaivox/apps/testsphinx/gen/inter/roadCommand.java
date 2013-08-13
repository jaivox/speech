
import java.util.Vector;

import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.HistNode;

public class roadCommand extends Command {

	public roadCommand () {
	}

	public String [] handle (String f,
		String question, String spec, String instate,
		Vector <HistNode> history) {
		if (f.equals ("ask"))
			return ask (question, spec, instate, history);
		else if (f.equals ("find"))
			return find (question, spec, instate, history);
		else return null;
	}

	String [] ask (String question, String spec, String instate, Vector <HistNode> history) {
		String result [] = new String [1];
		result [0] = "implement ask command!";
		return result;
	}

	String [] find (String question, String spec, String instate, Vector <HistNode> history) {
		String result [] = new String [1];
		result [0] = "implement find command!";
		return result;
	}

}

