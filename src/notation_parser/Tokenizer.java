package notation_parser;

public class Tokenizer {
	String str;
	int index;
	
	public enum TokType {
		NUMBER,
		ALPHA,
		OPERATOR
	}
	
	public Tokenizer(String str) {
		this.str = str;
		this.index = 0;
	}
	
	public TokType cur_type() {
		if (index == str.length()) {
			return null;
		}
		
		char c = str.charAt(index);
		
		if (Character.isAlphabetic(c)) {
			return TokType.ALPHA;
		} else if (Character.isDigit(c)) {
			return TokType.NUMBER;
		} else {
			return TokType.OPERATOR;
		}
	}
	
	public String next() {
		TokType type = cur_type();
		
		if (type == null) {
			return null;
		}
		
		String out = "";
		out = out + str.charAt(index++);
		
		while (cur_type() == type) {
			out += str.charAt(index);
			index++;
		}
		return out;
	}
}
