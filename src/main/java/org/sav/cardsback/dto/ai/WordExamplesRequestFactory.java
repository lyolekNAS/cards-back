package org.sav.cardsback.dto.ai;

import java.util.List;
import java.util.Map;

public class WordExamplesRequestFactory {

	public static ChatRequest create(String word) {
		return new ChatRequest(
				"ai-examples",
				List.of(
						new Message("user", word)
				),
				0.7,
				0.9,
				"none",
				new ResponseFormat(
						"json_schema",
						new JsonSchema(
								"word_examples",
								new Schema(
										"object",
										Map.of(
												"word", new Property("string", null, null, null),
												"examples", new Property(
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
