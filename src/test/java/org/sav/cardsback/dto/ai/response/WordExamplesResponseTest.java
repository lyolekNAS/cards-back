package org.sav.cardsback.dto.ai.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordExamplesResponseTest {

	@Test
	void record_exposesFields() {
		WordExamplesResponse response = new WordExamplesResponse(
				"hello",
				List.of("ex-1", "ex-2", "ex-3")
		);

		assertEquals("hello", response.word());
		assertEquals(List.of("ex-1", "ex-2", "ex-3"), response.examples());
	}

	@Test
	void serializesAndDeserializesWithExpectedJsonFields() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		WordExamplesResponse response = new WordExamplesResponse(
				"hello",
				List.of("ex-1", "ex-2", "ex-3")
		);

		String json = mapper.writeValueAsString(response);
		JsonNode root = mapper.readTree(json);
		assertEquals("hello", root.get("word").asText());
		assertEquals(3, root.get("examples").size());

		WordExamplesResponse restored = mapper.readValue(json, WordExamplesResponse.class);
		assertEquals(response, restored);
	}
}
