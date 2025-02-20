package notation_parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TokenizerTest {

	@Test
	void test() {
		Tokenizer tok = new Tokenizer("1d20+5");
		assertTrue(tok.next().equals("1"));
		assertTrue(tok.next().equals("d"));
		assertTrue(tok.next().equals("20"));
		assertTrue(tok.next().equals("+"));
		assertTrue(tok.next().equals("5"));
		assertEquals(tok.next(), null);
	}

}
