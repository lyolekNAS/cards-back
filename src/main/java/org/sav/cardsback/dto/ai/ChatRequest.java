package org.sav.cardsback.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ChatRequest(
		String model,
		List<Message> messages,
		Double temperature,

		@JsonProperty("top_p")
		Double topP,

		@JsonProperty("reasoning_effort")
		String reasoningEffort,

		@JsonProperty("response_format")
		ResponseFormat responseFormat
) {

	public static record Message(
			String role,
			String content
	) {
	}
	public static record ResponseFormat(
			String type,

			@JsonProperty("json_schema")
			JsonSchema jsonSchema
	) {

		public static record JsonSchema(
				String name,
				Schema schema
		) {

			public static record Schema(
					String type,
					Map<String, Property> properties,
					List<String> required
			) {

				public static record Property(
						String type,
						Map<String, Object> items,
						Integer minItems,
						Integer maxItems
				) {
				}
			}
		}
	}
}
