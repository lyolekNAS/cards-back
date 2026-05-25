package org.sav.cardsback.dto.ai.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatCompletionResponseTest {

	@Test
	void contentAs_whenValidJson_deserializesToTargetType() {
		ChatCompletionResponse response = new ChatCompletionResponse(
				"id-1",
				"chat.completion",
				1L,
				"gpt-x",
				"fp",
				List.of(
						new ChatCompletionResponse.Choice(
								0,
								new ChatCompletionResponse.MessageResponse(
										"assistant",
										"{\"word\":\"hello\",\"examples\":[\"e1\",\"e2\",\"e3\"]}"
								),
								"stop"
						)
				),
				new ChatCompletionResponse.Usage(1, 2, 3)
		);

		WordExamplesResponse parsed = response.contentAs(WordExamplesResponse.class);

		assertEquals("hello", parsed.word());
		assertEquals(List.of("e1", "e2", "e3"), parsed.examples());
	}

	@Test
	void contentAs_whenChoicesMissing_throwsIllegalStateException() {
		ChatCompletionResponse response = new ChatCompletionResponse(
				"id-1",
				"chat.completion",
				1L,
				"gpt-x",
				"fp",
				List.of(),
				new ChatCompletionResponse.Usage(1, 2, 3)
		);

		IllegalStateException ex = assertThrows(
				IllegalStateException.class,
				() -> response.contentAs(WordExamplesResponse.class)
		);

		assertEquals("No choices in AI response", ex.getMessage());
	}

	@Test
	void contentAs_whenJsonIsInvalid_throwsIllegalStateExceptionWithCause() {
		ChatCompletionResponse response = new ChatCompletionResponse(
				"id-1",
				"chat.completion",
				1L,
				"gpt-x",
				"fp",
				List.of(
						new ChatCompletionResponse.Choice(
								0,
								new ChatCompletionResponse.MessageResponse("assistant", "{not-json}"),
								"stop"
						)
				),
				new ChatCompletionResponse.Usage(1, 2, 3)
		);

		IllegalStateException ex = assertThrows(
				IllegalStateException.class,
				() -> response.contentAs(WordExamplesResponse.class)
		);

		assertTrue(ex.getMessage().contains("Failed to deserialize AI response content"));
		assertInstanceOf(Exception.class, ex.getCause());
	}
}
