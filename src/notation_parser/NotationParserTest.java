package notation_parser;


import java.util.HashMap;

import org.junit.jupiter.api.Test;

class NotationParserTest {

	@Test
	void test() {
		HashMap<String, String> aliases = new HashMap<String, String>();
		aliases.put("hello", "5d8-1");
		DieExpression exp = NotationParser.parse("1d20-2d8+7-hello", aliases);
		System.out.print(exp.toString());
	}

}
