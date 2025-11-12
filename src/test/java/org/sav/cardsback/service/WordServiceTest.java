package org.sav.cardsback.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.repository.WordRepository;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private StateLimitService stateLimitService;

    @InjectMocks
    private WordService wordService;

    private Word testWord;
    private StateLimitDto stateLimit;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        testWord = new Word();
        testWord.setId(1L);
        testWord.setUserId(userId);
        testWord.setEnglish("test");
        testWord.setUkrainian("тест");
        testWord.setEnglishCnt(5);
        testWord.setUkrainianCnt(3);
        testWord.setState(new WordState(WordStateDto.STAGE_1.getId()));


        stateLimit = new StateLimitDto();
        stateLimit.setState(WordStateDto.STAGE_1);
        stateLimit.setAttempt(10);   // ліміт збігся з testWord
        stateLimit.setDelay(0);      // щоб спрацював DONE
    }

    @Test
    void findAllByUserId_ReturnsWords() {
        List<Word> words = Collections.singletonList(testWord);
        when(wordRepository.findAllByUserId(userId)).thenReturn(words);

        List<Word> result = wordService.findAllByUserId(userId);

        assertEquals(words, result);
        verify(wordRepository).findAllByUserId(userId);
    }

    @Test
    void save_ReturnsWord() {
        when(wordRepository.save(testWord)).thenReturn(testWord);

        Word result = wordService.save(testWord);

        assertEquals(testWord, result);
        verify(wordRepository).save(testWord);
    }

    @Test
    void findByUserIdAndEnglish_ReturnsWord() {
        when(wordRepository.findByUserIdAndEnglish(userId, "test")).thenReturn(testWord);

        Word result = wordService.findByUserIdAndEnglish(userId, "test");

        assertEquals(testWord, result);
        verify(wordRepository).findByUserIdAndEnglish(userId, "test");
    }

    @Test
    void findByIdAndUserId_ReturnsWord() {
        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(testWord);

        Word result = wordService.findByIdAndUserId(1L, userId);

        assertEquals(testWord, result);
        verify(wordRepository).findByIdAndUserId(1L, userId);
    }

    @Test
    void delete_CallsRepository() {
        wordService.delete(testWord);

        verify(wordRepository).delete(testWord);
    }

    @Test
    void findAll_ReturnsAllWords() {
        List<Word> words = Collections.singletonList(testWord);
        when(wordRepository.findAll()).thenReturn(words);

        List<Word> result = wordService.findAll();

        assertEquals(words, result);
        verify(wordRepository).findAll();
    }

    @Test
    void findWordToTrain_ReturnsRandomWord() {
        List<Word> words = Collections.singletonList(testWord);
        when(wordRepository.findWordToTrain(userId, PageRequest.of(0, 1))).thenReturn(words);

        Word result = wordService.findWordToTrain(userId);

        assertEquals(testWord, result);
        verify(wordRepository).findWordToTrain(userId, PageRequest.of(0, 1));
    }

    @Test
    void findWordToTrain_EmptyList_ReturnsNull() {
        when(wordRepository.findWordToTrain(userId, PageRequest.of(0, 1))).thenReturn(Collections.emptyList());

        Word result = wordService.findWordToTrain(userId);

        assertNull(result);
        verify(wordRepository).findWordToTrain(userId, PageRequest.of(0, 1));
    }

    @Test
    void processTrainedWord_Success_EnglishLang_IncreasesCount() {
        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(1L);
        dto.setLang(WordLangDto.EN);
        dto.setSuccess(true);

        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(testWord);
        when(stateLimitService.findById(testWord.getState().getId())).thenReturn(stateLimit);
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        boolean result = wordService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(6, testWord.getEnglishCnt());
        assertNotNull(testWord.getLastTrain());
        verify(wordRepository).save(testWord);
    }

    @Test
    void processTrainedWord_Success_UkrainianLang_IncreasesCount() {
        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(1L);
        dto.setLang(WordLangDto.UA);
        dto.setSuccess(true);

        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(testWord);
        when(stateLimitService.findById(testWord.getState().getId())).thenReturn(stateLimit);
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        boolean result = wordService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(4, testWord.getUkrainianCnt());
        assertNotNull(testWord.getLastTrain());
        verify(wordRepository).save(testWord);
    }

    @Test
    void processTrainedWord_Failure_ResetsCountsAndState() {
        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(1L);
        dto.setLang(WordLangDto.EN);
        dto.setSuccess(false);

        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(testWord);
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        boolean result = wordService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(0, testWord.getEnglishCnt());
        assertEquals(0, testWord.getUkrainianCnt());
        assertEquals(WordStateDto.STAGE_1.getId(), testWord.getState().getId());
        assertNotNull(testWord.getLastTrain());
        verify(wordRepository).save(testWord);
    }

    @Test
    void processTrainedWord_WordNotFound_ReturnsFalse() {
        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(1L);

        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(null);

        boolean result = wordService.processTrainedWord(dto, userId);

        assertFalse(result);
        verify(wordRepository, never()).save(any());
    }

    @Test
    void processTrainedWord_BothCountsReachLimit_AdvancesState() {
        testWord.setEnglishCnt(9);
        testWord.setUkrainianCnt(10);
        stateLimit.setDelay(7);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(1L);
        dto.setLang(WordLangDto.EN);
        dto.setSuccess(true);

        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(testWord);
        when(stateLimitService.findById(testWord.getState().getId())).thenReturn(stateLimit);
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        wordService.processTrainedWord(dto, userId);

        assertEquals(WordStateDto.STAGE_1.getId() + 1, testWord.getState().getId());
        assertEquals(0, testWord.getEnglishCnt());
        assertEquals(0, testWord.getUkrainianCnt());
        assertNotNull(testWord.getNextTrain());
        verify(wordRepository).save(testWord);
    }

    @Test
    void processTrainedWord_BothCountsReachLimit_NoDelay_SetsDoneState() {
        testWord.setEnglishCnt(9);
        testWord.setUkrainianCnt(10);
        stateLimit.setDelay(0);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(1L);
        dto.setLang(WordLangDto.EN);
        dto.setSuccess(true);

        when(wordRepository.findByIdAndUserId(1L, userId)).thenReturn(testWord);
        when(stateLimitService.findById(testWord.getState().getId())).thenReturn(stateLimit);
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        wordService.processTrainedWord(dto, userId);

        assertEquals(WordStateDto.DONE.getId(), testWord.getState().getId());
        assertEquals(0, testWord.getEnglishCnt());
        assertEquals(0, testWord.getUkrainianCnt());
        assertNotNull(testWord.getNextTrain());
        verify(wordRepository).save(testWord);
    }

    @Test
    void getStatistics() {
        List<StatisticAttemptDto> attemptDtos = List.of();
        List<StatisticComonDto> commonDtos = List.of();
        
        when(wordRepository.getStatisticAttempt(userId)).thenReturn(attemptDtos);
        when(wordRepository.getStatisticCommon(userId)).thenReturn(commonDtos);

        StatisticDto result = wordService.getStatistics(userId);

        assertNotNull(result);
        assertEquals(attemptDtos, result.getStatisticsAttemptDto());
        assertEquals(commonDtos, result.getStatisticsComonDto());
        
        verify(wordRepository).getStatisticAttempt(userId);
        verify(wordRepository).getStatisticCommon(userId);
    }
}