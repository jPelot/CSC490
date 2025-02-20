package notation_parser;

public abstract class NotationParser {
	
	private static Die parseElement(Lexer lex) {
		
		if(lex.atEnd()) return null;
		
		Die die = new Die();
		int first = -1;
		int second = -1;
		
		if(lex.checkConsumeOp("-")) {
			die.positive = false;
		} else {
			lex.checkConsumeOp("+");
		}
		
		if(lex.isNum()) {
			first = lex.curNumber();
			lex.next();
		}
		
		if(lex.checkConsumeOp("d")) {
			
			if (!lex.isNum()) {
				return null;
			}
			second = lex.curNumber();
			lex.next();
		}
		
		die.count = first;
		die.value = second;
		
		if(first == -1) {
			die.count = 1;
		}
		
		if(!lex.atEnd() && !lex.isOperator()) return null;
		
		return die;
	}
	
	public static DieExpression parse(String input) {
		
		DieExpression expression = new DieExpression();
		Lexer lex = new Lexer(input);
		Die die;
		
		while((die = parseElement(lex)) != null) {
			expression.addDie(die);
		}
		
		return expression;
	}
}
