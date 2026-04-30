package org.sav.cardsback.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Choice(
		int index,
		MessageResponse message,

		@JsonProperty("finish_reason")
		String finishReason
) {
}
