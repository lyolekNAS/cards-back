package org.sav.cardsback.util;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.utils.StringTools;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringToolsTest {

	@Test
	void testParseWordsWithPunctuation() {
		String text = "Hello, world! How are you?";
		List<String> words = StringTools.parseWords(text);
		assertEquals(5, words.size());
		assertEquals(List.of("Hello", "world", "How", "are", "you"), words);
	}

	@Test
	void testParseWordsWithHyphens() {
		String text = "This is a well-known fact. The mother-in-law is here.";
		List<String> words = StringTools.parseWords(text);
		// Should keep hyphenated words intact
		assertTrue(words.contains("well-known"));
		assertTrue(words.contains("mother-in-law"));
	}

	@Test
	void testParseWordsWithVariousPunctuation() {
		String text = "Email: test@example.com; Price: $50.99!";
		List<String> words = StringTools.parseWords(text);
		// Should remove special chars but keep alphanumeric and hyphens
		assertTrue(words.stream().anyMatch(w -> w.contains("test")));
		assertTrue(words.stream().anyMatch(w -> w.contains("50")));
	}

	@Test
	void testParseWordsEmpty() {
		List<String> words = StringTools.parseWords("");
		assertTrue(words.isEmpty());
	}

	@Test
	void testParseWordsNull() {
		List<String> words = StringTools.parseWords(null);
		assertTrue(words.isEmpty());
	}

	@Test
	void testParseWordsToArray() {
		String text = "Hello, world!";
		String[] words = StringTools.parseWordsToArray(text);
		assertEquals(2, words.length);
		assertEquals("Hello", words[0]);
		assertEquals("world", words[1]);
	}
}

