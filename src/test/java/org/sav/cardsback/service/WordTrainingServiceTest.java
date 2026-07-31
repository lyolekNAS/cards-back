package org.sav.cardsback.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.sav.cardsback.domain.dictionary.service.StateLimitService;
import org.sav.cardsback.domain.dictionary.service.WordTrainingService;
import org.sav.cardsback.dto.StateLimitDto;
import org.sav.cardsback.dto.TrainedWordDto;
import org.sav.cardsback.dto.WordLangDto;
import org.sav.cardsback.dto.WordStateDto;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordTrainingServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private StateLimitService stateLimitService;

    @InjectMocks
    private WordTrainingService wordTrainingService;

    private final Long userId = 1L;
    private Word word;

    @BeforeEach
    void setUp() {
        word = new Word();
        word.setId(10L);
        word.setUserId(userId);
        word.setEnglish("test");
        word.setState(new WordState(WordStateDto.STAGE_1.getId()));
        word.setEnglishCnt(0);
        word.setUkrainianCnt(0);
    }

    @Test
    void findWordToTrain_returnsWordWhenPresent() {
        when(wordRepository.findWordToTrain(eq(userId), any(Pageable.class))).thenReturn(List.of(word));

        Word result = wordTrainingService.findWordToTrain(userId);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(wordRepository).findWordToTrain(eq(userId), any(Pageable.class));
    }

    @Test
    void findWordToTrain_returnsNullWhenEmpty() {
        when(wordRepository.findWordToTrain(eq(userId), any(Pageable.class))).thenReturn(Collections.emptyList());

        Word result = wordTrainingService.findWordToTrain(userId);

        assertNull(result);
    }

    @Test
    void getWordsForRetro_returnsList() {
        List<String> retroWords = List.of("word1", "word2");
        when(wordRepository.getWordsForRetro(eq(userId), any(Pageable.class))).thenReturn(retroWords);

        List<String> result = wordTrainingService.getWordsForRetro(userId);

        assertEquals(retroWords, result);
        verify(wordRepository).getWordsForRetro(eq(userId), any(Pageable.class));
    }

    @Test
    void processTrainedWord_returnsFalseWhenWordNotFound() {
        when(wordRepository.findByIdAndUserId(99L, userId)).thenReturn(null);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(99L);
        dto.setSuccess(true);

        boolean result = wordTrainingService.processTrainedWord(dto, userId);

        assertFalse(result);
        verify(wordRepository).findByIdAndUserId(99L, userId);
        verifyNoInteractions(stateLimitService);
    }

    @Test
    void processTrainedWord_handlesSuccessWithoutMovingToNextState() {
        when(wordRepository.findByIdAndUserId(10L, userId)).thenReturn(word);

        StateLimitDto limitDto = StateLimitDto.builder()
                .state(WordStateDto.STAGE_1)
                .attempt(2)
                .delay(1)
                .build();

        when(stateLimitService.findById(WordStateDto.STAGE_1.getId())).thenReturn(limitDto);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(10L);
        dto.setSuccess(true);
        dto.setLang(WordLangDto.EN);

        boolean result = wordTrainingService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(1, word.getEnglishCnt());
        assertEquals(0, word.getUkrainianCnt());
        assertEquals(WordStateDto.STAGE_1.getId(), word.getState().getId()); // Not ready yet (need 2 attempts)
        assertNotNull(word.getLastTrain());
    }

    @Test
    void processTrainedWord_handlesSuccessMovingToNextStateWithDelay() {
        word.setEnglishCnt(1);
        word.setUkrainianCnt(2);
        when(wordRepository.findByIdAndUserId(10L, userId)).thenReturn(word);

        StateLimitDto limitDto = StateLimitDto.builder()
                .state(WordStateDto.STAGE_1)
                .attempt(2)
                .delay(3) // has delay -> next state ID + 1
                .build();

        when(stateLimitService.findById(WordStateDto.STAGE_1.getId())).thenReturn(limitDto);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(10L);
        dto.setSuccess(true);
        dto.setLang(WordLangDto.EN); // English count becomes 2 (reaches limit)

        boolean result = wordTrainingService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(WordStateDto.STAGE_1.getId() + 1, word.getState().getId());
        assertNotNull(word.getNextTrain());
        assertEquals(0, word.getEnglishCnt());
        assertEquals(0, word.getUkrainianCnt());
    }

    @Test
    void processTrainedWord_handlesSuccessMovingToDoneWithoutDelay() {
        word.setEnglishCnt(1);
        word.setUkrainianCnt(2);
        when(wordRepository.findByIdAndUserId(10L, userId)).thenReturn(word);

        StateLimitDto limitDto = StateLimitDto.builder()
                .state(WordStateDto.STAGE_1)
                .attempt(2)
                .delay(0) // no delay -> DONE state
                .build();

        when(stateLimitService.findById(WordStateDto.STAGE_1.getId())).thenReturn(limitDto);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(10L);
        dto.setSuccess(true);
        dto.setLang(WordLangDto.EN);

        boolean result = wordTrainingService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(WordStateDto.DONE.getId(), word.getState().getId());
        assertNotNull(word.getNextTrain());
        assertEquals(0, word.getEnglishCnt());
        assertEquals(0, word.getUkrainianCnt());
    }

    @Test
    void processTrainedWord_handlesFailure() {
        word.setEnglishCnt(2);
        word.setUkrainianCnt(2);
        word.setState(new WordState(WordStateDto.STAGE_3.getId()));

        when(wordRepository.findByIdAndUserId(10L, userId)).thenReturn(word);

        TrainedWordDto dto = new TrainedWordDto();
        dto.setId(10L);
        dto.setSuccess(false);

        boolean result = wordTrainingService.processTrainedWord(dto, userId);

        assertTrue(result);
        assertEquals(0, word.getEnglishCnt());
        assertEquals(0, word.getUkrainianCnt());
        assertEquals(WordStateDto.STAGE_1.getId(), word.getState().getId());
        assertNotNull(word.getLastTrain());
    }

    @Test
    void pickRandom5FromPause_returnsZeroWhenNoIdsFound() {
        when(wordRepository.findRandomIdsForUser(eq(userId), any(Pageable.class))).thenReturn(Collections.emptyList());

        int updatedCount = wordTrainingService.pickRandom5FromPause(userId);

        assertEquals(0, updatedCount);
        verify(wordRepository, never()).updateStateTo1(any());
    }

    @Test
    void pickRandom5FromPause_updatesStateAndReturnsCount() {
        List<Long> ids = List.of(1L, 2L, 3L);
        when(wordRepository.findRandomIdsForUser(eq(userId), any(Pageable.class))).thenReturn(ids);
        when(wordRepository.updateStateTo1(ids)).thenReturn(3);

        int updatedCount = wordTrainingService.pickRandom5FromPause(userId);

        assertEquals(3, updatedCount);
        verify(wordRepository).updateStateTo1(ids);
    }
}
