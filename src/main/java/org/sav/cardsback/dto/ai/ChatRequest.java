package org.sav.cardsback.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
}
