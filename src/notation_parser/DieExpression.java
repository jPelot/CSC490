package notation_parser;
import java.util.ArrayList;

public class DieExpression {
	
	ArrayList<Die> expression;
	
	public DieExpression() {
		this.expression = new ArrayList<Die>();
	}
	
	public void addDie(Die d) {
		this.expression.add(d);
	}
	
	public ArrayList<Die> dice() {
		return this.expression;
	}
	
	public String toString() {
		String out = "";
		for(Die die : expression) {
			if(die.positive) {
				out = out + "+";
			} else {
				out = out + "-";
			}
			
			if(die.count == -1) {
				out = out + "d" + die.value;
			} else if(die.value == -1) {
				out = out + die.count;
			} else {
				out = out + die.count + "d" + die.value;
			}
			
			out = out + "\n";
		}
		return out;
	}
	
	public void add(DieExpression exp) {
		for (Die d : exp.dice()) {
			this.expression.add(d);
		}
	}
	
	public void negate() {
		for (Die d : this.expression) {
			d.positive = !d.positive;
		}
	}
}
