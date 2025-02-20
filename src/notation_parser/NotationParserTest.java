package notation_parser;


import org.junit.jupiter.api.Test;

class NotationParserTest {

	@Test
	void test() {
		DieExpression exp = NotationParser.parse("1d20-2d8+d");
		System.out.print(exp.toString());
	}

}
