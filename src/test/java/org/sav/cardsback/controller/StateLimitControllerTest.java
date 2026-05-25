package org.sav.cardsback.controller;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.domain.dictionary.service.StateLimitService;
import org.sav.cardsback.dto.StateLimitDto;
import org.sav.cardsback.dto.WordStateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sav.cardsback.util.SecurityTestUtils.mockJwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StateLimitController.class)
class StateLimitControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private StateLimitService stateLimitService;

	@Test
	void getAll_returnsStateLimits() throws Exception {
		StateLimitDto dto1 = StateLimitDto.builder()
				.state(WordStateDto.STAGE_1)
				.attempt(10)
				.delay(5)
				.color("#111111")
				.build();
		StateLimitDto dto2 = StateLimitDto.builder()
				.state(WordStateDto.STAGE_2)
				.attempt(15)
				.delay(10)
				.color("#222222")
				.build();
		when(stateLimitService.findAll()).thenReturn(List.of(dto1, dto2));

		mockMvc.perform(get("/api/state/all").with(mockJwt(1L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].state").value("STAGE_1"))
				.andExpect(jsonPath("$[0].attempt").value(10))
				.andExpect(jsonPath("$[1].state").value("STAGE_2"))
				.andExpect(jsonPath("$[1].delay").value(10));

		verify(stateLimitService).findAll();
	}

	@Test
	void getById_withValidState_returnsStateLimit() throws Exception {
		StateLimitDto dto = StateLimitDto.builder()
				.state(WordStateDto.STAGE_3)
				.attempt(20)
				.delay(15)
				.color("#333333")
				.build();
		when(stateLimitService.findById(3)).thenReturn(dto);

		mockMvc.perform(get("/api/state/id/STAGE_3").with(mockJwt(1L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state").value("STAGE_3"))
				.andExpect(jsonPath("$.attempt").value(20))
				.andExpect(jsonPath("$.delay").value(15));

		verify(stateLimitService).findById(3);
	}

	@Test
	void getById_withInvalidState_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/state/id/UNKNOWN").with(mockJwt(1L)))
				.andExpect(status().isBadRequest());
	}
}
