package org.sav.cardsback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sav.cardsback.dto.StatisticDto;
import org.sav.cardsback.dto.TrainedWordDto;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.dto.WordLangDto;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.domain.dictionary.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
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
		word.setLastTrain(OffsetDateTime.now());
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
	void getAllByUser_emptyState_returnsWordsPage() throws Exception {
		Page<Word> page = new PageImpl<>(List.of(word));

		when(wordService.findAllByUserId(eq(userId), eq(""), any(Pageable.class)))
				.thenReturn(page);

		when(wordMapper.toDto(word)).thenReturn(wordDto);

		mockMvc.perform(get("/api/word/user/all")
						.param("state", "")
						.param("page", "0")
						.param("size", "10")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1L))
				.andExpect(jsonPath("$.content[0].english").value("hello"));

		verify(wordService).findAllByUserId(eq(userId), eq(""), any(Pageable.class));
		verify(wordMapper).toDto(word);
	}

	@Test
	void getAllByUser_withState_returnsWordsPage() throws Exception {
		Page<Word> page = new PageImpl<>(List.of(word));
		String state = "STAGE_1";
		when(wordService.findAllByUserId(eq(userId), eq(state), any(Pageable.class))).thenReturn(page);

		when(wordMapper.toDto(word)).thenReturn(wordDto);

		mockMvc.perform(get("/api/word/user/all")
						.param("state", state)
						.param("page", "0")
						.param("size", "10")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1L))
				.andExpect(jsonPath("$.content[0].english").value("hello"));

		verify(wordService).findAllByUserId(eq(userId), eq(state), any(Pageable.class));
		verify(wordMapper).toDto(word);
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
		when(wordService.findByUserIdAndEnglish(userId, "hello")).thenReturn(wordDto);

		mockMvc.perform(get("/api/word/find")
						.param("w", "hello")
						.with(mockJwt(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.english").value("hello"));

		verify(wordService).findByUserIdAndEnglish(userId, "hello");
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

	@Test
	void getStatistic() throws Exception {
		StatisticDto statisticDto = new StatisticDto();
		when(wordService.getStatistics(userId)).thenReturn(statisticDto);

		mockMvc.perform(get("/api/word/statistic")
					.with(mockJwt(userId)))
				.andExpect(status().isOk());

		verify(wordService).getStatistics(userId);
	}
}