package org.sav.cards.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sav.cards.entity.Word;
import org.sav.cards.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WordController.class)
@Import(WordControllerTest.TestConfig.class)
class WordControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WordRepository wordRepository;


	private Word word;

	@BeforeEach
	void setUp() {
		word.setUserId(1L);
		word.setId(1L);
		word.setEnglish("one");
		word.setUkrainian("один");
	}

	@Test
	void getAll() throws Exception {
		when(wordRepository.findAll()).thenReturn(List.of(word));
		this.mockMvc.perform(get("/api/word/all")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("[{\"user\":{\"Id\":1,\"username\":\"Іван\",")))
				.andExpect(content().string(containsString("\"eName\":\"one\",\"uName\":\"один\",")));

	}

	@Test
	void getByUser() throws Exception {
		when(wordRepository.findAllByUserId(1L)).thenReturn(List.of(word));
		this.mockMvc.perform(get("/api/word/user/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("[{\"user\":{\"Id\":1,\"username\":\"Іван\",")))
				.andExpect(content().string(containsString("\"eName\":\"one\",\"uName\":\"один\",")));

	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		public WordRepository woedRepository() {
			return Mockito.mock(WordRepository.class);
		}
	}
}
