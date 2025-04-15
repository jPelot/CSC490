package dice_roller;

import java.util.ArrayList;

import notation_parser.Die;
import notation_parser.DieExpression;

public class ResultSet {
	
	Integer totalResult;
	ArrayList<Integer> results;
	DieExpression expression;
	
	public ResultSet() {
		this.totalResult = 0;
		this.results = new ArrayList<Integer>();
		this.expression = new DieExpression();
	}
	
	public void add(int result, Die die) {
		expression.addDie(die);
		results.add(result);
	}
	
	public String toString() {
		String out = "";
		for (Integer i : results) {
			out += i.toString() + ", ";
		}
		out += " = " + this.totalResult.toString();
		return out;
	}
	
	public ArrayList<Integer> results() {
		return this.results;
	}
	
}
