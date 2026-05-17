package org.sav.cardsback.dto.ai;

import java.util.List;
import java.util.Map;

public class WordTranslationsRequestFactory {

	public static ChatRequest create(String word) {
		return new ChatRequest(
				"ai-translator",
				List.of(
						new ChatRequest.Message("user", word)
				),
				0.3,
				0.9,
				"none",
				new ChatRequest.ResponseFormat(
						"json_schema",
						new ChatRequest.JsonSchema(
								"word_translations",
								new ChatRequest.Schema(
										"object",
										Map.of(
												"word", new ChatRequest.Property("string", null, null, null),
												"examples", new ChatRequest.Property(
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
