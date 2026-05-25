package org.sav.cardsback.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StateLimitDtoTest {

	@Test
	void constructorWithStateId_mapsStateAndFields() {
		StateLimitDto dto = new StateLimitDto(1, 10, 30, "#fff");

		assertEquals(WordStateDto.STAGE_1, dto.getState());
		assertEquals(10, dto.getAttempt());
		assertEquals(30, dto.getDelay());
		assertEquals("#fff", dto.getColor());
	}

	@Test
	void constructorWithInvalidStateId_throwsIllegalArgumentException() {
		assertThrows(IllegalArgumentException.class, () -> new StateLimitDto(999, 1, 1, "#000"));
	}

	@Test
	void builder_setsAllFields() {
		StateLimitDto dto = StateLimitDto.builder()
				.state(WordStateDto.STAGE_2)
				.attempt(20)
				.delay(60)
				.color("#222")
				.build();

		assertEquals(WordStateDto.STAGE_2, dto.getState());
		assertEquals(20, dto.getAttempt());
		assertEquals(60, dto.getDelay());
		assertEquals("#222", dto.getColor());
	}
}
