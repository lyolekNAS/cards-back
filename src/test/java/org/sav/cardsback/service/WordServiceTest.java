package org.sav.cardsback.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.domain.dictionary.service.StateLimitService;
import org.sav.cardsback.domain.dictionary.service.WordService;
import org.sav.cardsback.domain.dictionary.service.WordStatisticsService;
import org.sav.cardsback.domain.dictionary.service.WordTrainingService;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordForm;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private DictWordFormRepository dictWordFormRepository;

    @Mock
    private UserDictWordRepository userDictWordRepository;

    @Mock
    private WordStatisticsService statisticsService;

    @Mock
    private WordTrainingService trainingService;

    @Mock
    private WordMapper wordMapper;

    @InjectMocks
    private WordService wordService;

    private Word testWord;
    private WordDto testWordDto;
    private StateLimitDto stateLimit;
    private DictWordForm testDictWordForm;
    private DictWord testDictWord;
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

        testWordDto = WordDto.builder()
                .id(1L)
                .userId(userId)
                .english("test")
                .ukrainian("тест")
                .englishCnt(5)
                .ukrainianCnt(5)
                .state(WordStateDto.STAGE_1)
                .build();

        stateLimit = new StateLimitDto();
        stateLimit.setState(WordStateDto.STAGE_1);
        stateLimit.setAttempt(10);   // ліміт збігся з testWord
        stateLimit.setDelay(0);      // щоб спрацював DONE

        testDictWord = new DictWord();
        testDictWord.setId(1L);
        testDictWord.setState(0);
        testDictWord.setWordText("test");


        testDictWordForm = new DictWordForm();
        testDictWordForm.setId(1L);
        testDictWordForm.setWordText("test");
        testDictWordForm.setLemma(testDictWord);

        testWord.setDictWord(testDictWord);

    }

    @Test
    void findAllByUserId_withEmptyState_callsFindAllByUserId() {
        Page<Word> page = new PageImpl<>(List.of(testWord));
        when(wordRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        Page<Word> result = wordService.findAllByUserId(userId, "", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(testWord, result.getContent().getFirst());
        verify(wordRepository).findAllByUserId(eq(userId), any(Pageable.class));
        verify(wordRepository, never()).findAllByUserIdAndState(any(), any(), any());
    }

    @Test
    void findAllByUserId_withNonEmptyState_callsFindAllByUserIdAndState() {
        Page<Word> page = new PageImpl<>(List.of(testWord));
        String stateName = "STAGE_1";
        WordState stateDto = WordStateDto.fromName(stateName);

        when(wordRepository.findAllByUserIdAndState(
                eq(userId),
                argThat(s -> s.getId().equals(stateDto.getId())),
                any(Pageable.class)
        )).thenReturn(page);

        Page<Word> result =
                wordService.findAllByUserId(userId, stateName, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(testWord, result.getContent().getFirst());

        verify(wordRepository).findAllByUserIdAndState(
                eq(userId),
                argThat(s -> s.getId().equals(stateDto.getId())),
                any(Pageable.class)
        );

        verify(wordRepository, never()).findAllByUserId(any(), any());
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
        when(dictWordFormRepository.findByWordText("test")).thenReturn(Optional.of(testDictWordForm));
        when(wordRepository.findByUserIdAndEnglish(userId, "test")).thenReturn(Optional.of(testWord));
        when(wordMapper.toDto(testWord)).thenReturn(testWordDto);

        WordDto result = wordService.findByUserIdAndEnglish(userId, "test");

        assertEquals(testWordDto, result);
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
        when(trainingService.findWordToTrain(userId)).thenReturn(testWord);

        Word result = wordService.findWordToTrain(userId);

        assertEquals(testWord, result);
        verify(trainingService).findWordToTrain(userId);
    }

    @Test
    void findWordToTrain_EmptyList_ReturnsNull() {
        when(trainingService.findWordToTrain(userId)).thenReturn(null);

        Word result = wordService.findWordToTrain(userId);

        assertNull(result);
        verify(trainingService).findWordToTrain(userId);
    }

    @Test
    void processTrainedWord_DelegatesToTrainingService() {
        TrainedWordDto dto = new TrainedWordDto();
        when(trainingService.processTrainedWord(dto, userId)).thenReturn(true);

        boolean result = wordService.processTrainedWord(dto, userId);

        assertTrue(result);
        verify(trainingService).processTrainedWord(dto, userId);
    }

    @Test
    void getStatistics_DelegatesToStatisticsService() {
        StatisticDto stat = new StatisticDto();
        when(statisticsService.getStatistics(userId)).thenReturn(stat);

        StatisticDto result = wordService.getStatistics(userId);

        assertEquals(stat, result);
        verify(statisticsService).getStatistics(userId);
    }
}
