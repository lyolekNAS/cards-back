package org.sav.cardsback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.service.WordService;
import org.sav.fornas.dto.cards.TrainedWordDto;
import org.sav.fornas.dto.cards.WordDto;
import org.sav.fornas.dto.cards.WordLangDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.sav.cardsback.util.SecurityTestUtils.mockJwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WordController.class)
class WordControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private WordService wordService;

	@MockBean
	private WordMapper wordMapper;

	@Autowired
	private ObjectMapper objectMapper;

	private Word word;
	private WordDto wordDto;
	private final Long userId = 123L;

	@BeforeEach
	void setUp() {
		word = new Word();
		word.setId(1L);
		word.setUserId(userId);
		word.setEnglish("hello");
		word.setUkrainian("привіт");
		word.setEnglishCnt(5);
		word.setUkrainianCnt(3);
		word.setLastTrain(LocalDateTime.now());
		word.setState(new WordState());

		wordDto = new WordDto();
		wordDto.setId(1L);
		wordDto.setEnglish("hello");
		wordDto.setUkrainian("привіт");
	}


	@Test
	void getAll_returnsWords() throws Exception {
		when(wordService.findAll()).thenReturn(List.of(word));

		mockMvc.perform(get("/api/word/all").with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].english").value("hello"));
	}

	@Test
	void getAllByUser() throws Exception {
		when(wordService.findAllByUserId(userId)).thenReturn(List.of(word));
		when(wordMapper.toDtoList(List.of(word))).thenReturn(List.of(wordDto));

		mockMvc.perform(get("/api/word/user/all")
						.with(mockJwt(userId))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].english").value("hello"));

		verify(wordService).findAllByUserId(userId);
		verify(wordMapper).toDtoList(List.of(word));
	}

	@Test
	void addWord() throws Exception {
		when(wordMapper.toEntity(any(WordDto.class))).thenReturn(word);
		when(wordService.save(any(Word.class))).thenReturn(word);
		when(wordMapper.toDto(word)).thenReturn(wordDto);

		mockMvc.perform(post("/api/word/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(wordDto))
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.english").value("hello"));

		ArgumentCaptor<Word> wordCaptor = ArgumentCaptor.forClass(Word.class);
		verify(wordService).save(wordCaptor.capture());
		assertEquals(userId, wordCaptor.getValue().getUserId());
	}

	@Test
	void findWord() throws Exception {
		when(wordService.findByUserIdAndEnglish(userId, "hello")).thenReturn(word);
		when(wordMapper.toDto(word)).thenReturn(wordDto);

		mockMvc.perform(get("/api/word/find")
						.param("w", "hello")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.english").value("hello"));

		verify(wordService).findByUserIdAndEnglish(userId, "hello");
		verify(wordMapper).toDto(word);
	}

	@Test
	void deleteWord() throws Exception {
		when(wordService.findByIdAndUserId(1L, userId)).thenReturn(word);

		mockMvc.perform(delete("/api/word/delete")
						.param("id", "1")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(content().string("deleted"));

		verify(wordService).findByIdAndUserId(1L, userId);
		verify(wordService).delete(word);
	}

	@Test
	void findWordToTrain_withWord() throws Exception {
		when(wordService.findWordToTrain(userId)).thenReturn(word);
		when(wordMapper.toDto(word)).thenReturn(wordDto);

		mockMvc.perform(get("/api/word/train").with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.english").value("hello"))
				.andExpect(jsonPath("$.lang").value("UA"));

		verify(wordService).findWordToTrain(userId);
		verify(wordMapper).toDto(word);
	}

	@Test
	void findWordToTrain_noWord() throws Exception {
		when(wordService.findWordToTrain(userId)).thenReturn(null);

		mockMvc.perform(get("/api/word/train").with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(content().string(""));

		verify(wordService).findWordToTrain(userId);
		verifyNoInteractions(wordMapper);
	}

	@Test
	void processTrainedWord_success() throws Exception {
		TrainedWordDto trainedWordDto = new TrainedWordDto();
		trainedWordDto.setId(1L);
		trainedWordDto.setLang(WordLangDto.EN);
		trainedWordDto.setSuccess(true);

		when(wordService.processTrainedWord(any(TrainedWordDto.class), eq(userId))).thenReturn(true);

		mockMvc.perform(post("/api/word/trained")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(trainedWordDto))
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(content().string("trained"));

		verify(wordService).processTrainedWord(any(TrainedWordDto.class), eq(userId));
	}

	@Test
	void processTrainedWord_wordNotFound() throws Exception {
		TrainedWordDto trainedWordDto = new TrainedWordDto();
		trainedWordDto.setId(1L);
		trainedWordDto.setLang(WordLangDto.EN);
		trainedWordDto.setSuccess(false);

		when(wordService.processTrainedWord(any(TrainedWordDto.class), eq(userId))).thenReturn(false);

		mockMvc.perform(post("/api/word/trained")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(trainedWordDto))
						.with(mockJwt(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("error"));

		verify(wordService).processTrainedWord(any(TrainedWordDto.class), eq(userId));
	}
}