package notation_parser;

import java.util.HashMap;

public abstract class NotationParser {
	
	private static Die parseElement(Lexer lex, DieExpression exp, HashMap<String, String> aliases) {
		
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
		
		if(lex.isAlias()) {
			String alias = lex.curAlias();
			DieExpression temp = parse(aliases.get(alias), aliases);
			if (die.positive == false) {
				temp.negate();
			}
			exp.add(temp);
			
			return null;
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
	
	public static DieExpression parse(String input, HashMap<String, String> aliases) {
		
		DieExpression expression = new DieExpression();
		Lexer lex = new Lexer(input);
		Die die;
		
		while((die = parseElement(lex, expression, aliases)) != null) {
			expression.addDie(die);
		}
		
		return expression;
	}
}
