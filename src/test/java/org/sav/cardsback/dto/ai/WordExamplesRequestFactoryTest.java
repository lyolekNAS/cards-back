package org.sav.cardsback.dto.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WordExamplesRequestFactoryTest {

	@Test
	void create_buildsExpectedRequest() {
		ChatRequest request = WordExamplesRequestFactory.create("hello");

		assertEquals("ai-examples", request.model());
		assertEquals(1, request.messages().size());
		assertEquals("user", request.messages().getFirst().role());
		assertEquals("hello", request.messages().getFirst().content());
		assertEquals(0.7, request.temperature());
		assertEquals(0.9, request.topP());
		assertEquals("none", request.reasoningEffort());

		ChatRequest.ResponseFormat format = request.responseFormat();
		assertNotNull(format);
		assertEquals("json_schema", format.type());
		assertEquals("word_examples", format.jsonSchema().name());

		ChatRequest.Schema schema = format.jsonSchema().schema();
		assertEquals("object", schema.type());
		assertEquals(2, schema.properties().size());
		assertEquals("string", schema.properties().get("word").type());
		assertEquals("array", schema.properties().get("examples").type());
		assertEquals(3, schema.properties().get("examples").minItems());
		assertEquals(3, schema.properties().get("examples").maxItems());
		assertEquals("string", schema.properties().get("examples").items().get("type"));
		assertEquals(2, schema.required().size());
		assertEquals("word", schema.required().get(0));
		assertEquals("examples", schema.required().get(1));
	}
}
