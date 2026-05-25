package org.sav.cardsback.dto.ai.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordResponseTest {

	private record StubWordResponse(String word) implements WordResponse {}

	@Test
	void word_returnsProvidedValue() {
		WordResponse response = new StubWordResponse("hello");

		assertEquals("hello", response.word());
	}
}
