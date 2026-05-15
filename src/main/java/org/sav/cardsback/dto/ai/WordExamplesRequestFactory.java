package org.sav.cardsback.dto.ai;

import java.util.List;
import java.util.Map;

public class WordExamplesRequestFactory {

	public static ChatRequest create(String word) {
		return new ChatRequest(
				"ai-examples",
				List.of(
						new ChatRequest.Message("user", word)
				),
				0.7,
				0.9,
				"none",
				new ChatRequest.ResponseFormat(
						"json_schema",
						new ChatRequest.ResponseFormat.JsonSchema(
								"word_examples",
								new ChatRequest.ResponseFormat.JsonSchema.Schema(
										"object",
										Map.of(
												"word", new ChatRequest.ResponseFormat.JsonSchema.Schema.Property("string", null, null, null),
												"examples", new ChatRequest.ResponseFormat.JsonSchema.Schema.Property(
														"array",
														Map.of("type", "string"),
														3,
														3
												)
										),
										List.of("word", "examples")
								)
						)
				)
		);
	}
}
