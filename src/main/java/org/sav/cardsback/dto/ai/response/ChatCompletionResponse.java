package org.sav.cardsback.dto.ai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public record ChatCompletionResponse(
		String id,
		String object,
		long created,
		String model,

		@JsonProperty("system_fingerprint")
		String systemFingerprint,

		List<Choice> choices,
		Usage usage
) {
	private String content() {
		if (choices == null || choices.isEmpty()) {
			throw new IllegalStateException("No choices in AI response");
		}
		return choices.getFirst().message().content();
	}

	public <T> T contentAs(Class<T> targetType) {
		try {
			return new ObjectMapper().readValue(content(), targetType);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(
					"Failed to deserialize AI response content to " + targetType.getSimpleName(),
					e
			);
		}
	}
}
