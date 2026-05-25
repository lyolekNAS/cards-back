package org.sav.cardsback.dto.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRequestTest {

	@Test
	void serializesJsonPropertyNamesCorrectly() throws Exception {
		ChatRequest request = new ChatRequest(
				"model-x",
				List.of(new ChatRequest.Message("user", "hello")),
				0.5,
				0.9,
				"none",
				new ChatRequest.ResponseFormat(
						"json_schema",
						new ChatRequest.JsonSchema(
								"sample",
								new ChatRequest.Schema(
										"object",
										Map.of("word", new ChatRequest.Property("string", null, null, null)),
										List.of("word")
								)
						)
				)
		);

		String json = new ObjectMapper().writeValueAsString(request);
		JsonNode root = new ObjectMapper().readTree(json);

		assertEquals(0.9, root.get("top_p").asDouble());
		assertEquals("none", root.get("reasoning_effort").asText());
		assertEquals("json_schema", root.get("response_format").get("type").asText());
		assertEquals("sample", root.get("response_format").get("json_schema").get("name").asText());
	}
}
