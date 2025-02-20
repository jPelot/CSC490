package notation_parser;

import java.util.Arrays;

import notation_parser.Tokenizer.TokType;

public class Lexer {
	Tokenizer tok;
	
	TokType curType;
	String curToken;
	
	String operators[] = {"d","-","+"};
	
	public Lexer(String str) {
		this.tok = new Tokenizer(str);
		this.next();
	}
	
	public boolean next() {
		this.curType = tok.cur_type();
		this.curToken = tok.next();
		if (curToken == null) {
			return false;
		}
		return true;
	}
	
	public boolean atEnd() {
		return curToken == null;
	}
	
	/* State Checks */
	public boolean isOperator() {
		return Arrays.asList(operators).contains(curToken);
	}
	
	public boolean isAlias() {
		return curType == TokType.ALPHA && !isOperator();
	}
	
	public boolean isNum() {
		return curType == TokType.NUMBER;
	}
	
	/* Getters */
	public String curAlias() {
		if (!isAlias()) {return null;}
		return this.curToken;
	}
	
	public String curOperator() {
		if(!isOperator()) {return null;}
		return curToken;
	}
	
	public int curNumber() {
		if (!isNum()) {return 0;}
		return Integer.parseInt(curToken);
	}
	
	/* Misc */
	public boolean checkConsumeOp(String op) {
		if (isOperator() && op.equals(curToken)) {
			this.next();
			return true;
		}
		return false;
	}
}
