package org.sav.cardsback.dto.ai;

import java.util.List;
import java.util.Map;

public class WordTranslationsRequestFactory {

	public static ChatRequest create(String word) {
		return new ChatRequest(
				"ai-translator",
				List.of(
						new Message("user", word)
				),
				0.3,
				0.9,
				"none",
				new ResponseFormat(
						"json_schema",
						new JsonSchema(
								"word_translations",
								new Schema(
										"object",
										Map.of(
												"word", new Property("string", null, null, null),
												"translations", new Property(
														"array",
														Map.of("type", "string"),
														1,
														5
												)
										),
										new String[]{"word", "translations"}
								)
						)
				)
		);
	}
}
