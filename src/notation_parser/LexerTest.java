package notation_parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LexerTest {

	@Test
	void test() {
		Lexer lex = new Lexer("1d20+test");
		
		assertTrue(lex.isNum());
		assertFalse(lex.isAlias());
		assertFalse(lex.isOperator());
		assertEquals(lex.curNumber(), 1);
		
		assertTrue(lex.next());
		
		assertFalse(lex.isNum());
		assertFalse(lex.isAlias());
		assertTrue(lex.isOperator());
		assertTrue(lex.curOperator().equals("d"));
		assertFalse(lex.checkConsumeOp("-"));
		assertTrue(lex.checkConsumeOp("d"));
		assertTrue(lex.isNum());
		assertEquals(lex.curNumber(),20);
		
		assertTrue(lex.next());
		
		assertTrue(lex.checkConsumeOp("+"));
		assertFalse(lex.isNum());
		assertTrue(lex.isAlias());
		assertFalse(lex.isOperator());
		assertTrue(lex.curAlias().equals("test"));
		
		assertFalse(lex.next());
		
		
	}

}
