package org.sav.cardsback.controller;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.application.wordnik.WordnikRandomWordImporter;
import org.sav.cardsback.domain.dictionary.service.DictionaryService;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sav.cardsback.util.SecurityTestUtils.mockJwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DictionaryController.class)
class DictionaryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WordnikRandomWordImporter wordnikRandomWordImporter;

	@MockBean
	private WordProcessingService wordProcessingService;

	@MockBean
	private DictionaryService dictionaryService;

	@Test
	void findWordToSuggest_withoutLevel_defaultsToLevelOne() throws Exception {
		long userId = 42L;
		DictWord dictWord = new DictWord();
		dictWord.setId(11L);
		dictWord.setWordText("hello");

		WordDto dto = new WordDto();
		dto.setId(11L);
		dto.setEnglish("hello");

		when(dictionaryService.findWordToSuggest(1, userId)).thenReturn(Optional.of(dictWord));
		when(wordProcessingService.dtoFromDict(dictWord)).thenReturn(dto);

		mockMvc.perform(get("/api/dict/findWordToSuggest")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(11L))
				.andExpect(jsonPath("$.english").value("hello"));

		verify(dictionaryService).findWordToSuggest(1, userId);
		verify(wordProcessingService).dtoFromDict(dictWord);
	}

	@Test
	void findWordToSuggest_withLevelPassesRequestedLevel() throws Exception {
		long userId = 42L;
		DictWord dictWord = new DictWord();
		dictWord.setId(12L);
		dictWord.setWordText("world");

		WordDto dto = new WordDto();
		dto.setId(12L);
		dto.setEnglish("world");

		when(dictionaryService.findWordToSuggest(3, userId)).thenReturn(Optional.of(dictWord));
		when(wordProcessingService.dtoFromDict(dictWord)).thenReturn(dto);

		mockMvc.perform(get("/api/dict/findWordToSuggest/3")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(12L))
				.andExpect(jsonPath("$.english").value("world"));

		verify(dictionaryService).findWordToSuggest(3, userId);
		verify(wordProcessingService).dtoFromDict(dictWord);
	}

	@Test
	void findWordToSuggest_whenNoWordFound_returnsEmptyBody() throws Exception {
		long userId = 42L;
		when(dictionaryService.findWordToSuggest(1, userId)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/dict/findWordToSuggest")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(content().string(""));

		verify(dictionaryService).findWordToSuggest(1, userId);
	}
}
