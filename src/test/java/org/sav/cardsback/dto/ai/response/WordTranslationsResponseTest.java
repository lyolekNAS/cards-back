package org.sav.cardsback.dto.ai.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordTranslationsResponseTest {

	@Test
	void record_exposesFields() {
		WordTranslationsResponse response = new WordTranslationsResponse(
				"hello",
				List.of("привіт", "вітаю")
		);

		assertEquals("hello", response.word());
		assertEquals(List.of("привіт", "вітаю"), response.translations());
	}

	@Test
	void serializesAndDeserializesWithExpectedJsonFields() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		WordTranslationsResponse response = new WordTranslationsResponse(
				"hello",
				List.of("привіт", "вітаю")
		);

		String json = mapper.writeValueAsString(response);
		JsonNode root = mapper.readTree(json);
		assertEquals("hello", root.get("word").asText());
		assertEquals(2, root.get("translations").size());

		WordTranslationsResponse restored = mapper.readValue(json, WordTranslationsResponse.class);
		assertEquals(response, restored);
	}
}
