package org.sav.cardsback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.sav.cardsback.domain.dictionary.service.DictionaryService;
import org.sav.cardsback.domain.dictionary.service.WordStatisticsService;
import org.sav.cardsback.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordStatisticsServiceTest {

	@Mock
	private WordRepository wordRepository;

	@Mock
	private UserDictWordRepository userDictWordRepository;

	@Mock
	private DictionaryRepository dictionaryRepository;

	@Mock
	private DictionaryService dictionaryService;

	@InjectMocks
	private WordStatisticsService wordStatisticsService;

	private final Long userId = 1L;

	@Test
	void getStatistics_calculatesTotalsCorrectly() {
		StatisticAttemptDto attempt1 = new StatisticAttemptDto();
		attempt1.setStateId(WordStateDto.STAGE_1.getId());
		attempt1.setCount(10L);
		attempt1.setEnglishCnt(3L);
		attempt1.setUkrainianCnt(7L);

		StatisticAttemptDto attemptPaused = new StatisticAttemptDto();
		attemptPaused.setStateId(WordStateDto.PAUSED.getId());
		attemptPaused.setCount(5L);
		attemptPaused.setEnglishCnt(2L);
		attemptPaused.setUkrainianCnt(3L);

		List<StatisticAttemptDto> attemptDtos = List.of(attempt1, attemptPaused);

		StatisticComonDto common1 = new StatisticComonDto(WordStateDto.STAGE_1.getId(), 10L);
		StatisticComonDto commonDone = new StatisticComonDto(WordStateDto.DONE.getId(), 4L);
		List<StatisticComonDto> commonDtos = List.of(common1, commonDone);

		when(wordRepository.getStatisticAttempt(userId)).thenReturn(attemptDtos);
		when(wordRepository.getStatisticCommon(userId)).thenReturn(commonDtos);
		when(userDictWordRepository.countByUserIdAndIsKnown(userId, true)).thenReturn(15L);
		when(userDictWordRepository.countByUserIdAndIsUninteresting(userId, true)).thenReturn(3L);

		StatisticDto result = wordStatisticsService.getStatistics(userId);

		assertEquals(attemptDtos, result.getStatisticsAttemptDto());
		assertEquals(commonDtos, result.getStatisticsComonDto());
		// Total common count filters out PAUSED (0) and DONE (10)
		assertEquals(10L, result.getTotalCommonCount());
		// Total attempt count filters out PAUSED (0) and DONE (10)
		assertEquals(10L, result.getTotalAttemptCount());
		// Total attempt sum sums englishCnt + ukrainianCnt for all attempts: (3+7) + (2+3) = 15
		assertEquals(15L, result.getTotalAttemptSum());
		assertEquals(15L, result.getTotalKnown());
		assertEquals(3L, result.getTotalUninteresting());

		verify(wordRepository).getStatisticAttempt(userId);
		verify(wordRepository).getStatisticCommon(userId);
		verify(userDictWordRepository).countByUserIdAndIsKnown(userId, true);
		verify(userDictWordRepository).countByUserIdAndIsUninteresting(userId, true);
	}

	@Test
	void getDoctStatistics_fetchesForAllLevelsAndCaches() {
		LevelBoundsDto lb1 = new LevelBoundsDto(1, 1L, 100L);
		LevelBoundsDto lb2 = new LevelBoundsDto(2, 101L, 200L);
		LevelBoundsDto lb3 = new LevelBoundsDto(3, 201L, 300L);
		LevelBoundsDto lb4 = new LevelBoundsDto(4, 301L, 400L);
		LevelBoundsDto lb5 = new LevelBoundsDto(5, 401L, 500L);

		when(dictionaryService.getLevelBounds(1)).thenReturn(lb1);
		when(dictionaryService.getLevelBounds(2)).thenReturn(lb2);
		when(dictionaryService.getLevelBounds(3)).thenReturn(lb3);
		when(dictionaryService.getLevelBounds(4)).thenReturn(lb4);
		when(dictionaryService.getLevelBounds(5)).thenReturn(lb5);

		when(dictionaryRepository.getDictStats(101L, 200L)).thenReturn(null); // Should default to 0
		when(userDictWordRepository.getUserDictStats(101L, 200L, userId)).thenReturn(null); // Should default to 0

		when(dictionaryRepository.getDictStats(1L, 100L)).thenReturn(50);
		when(userDictWordRepository.getUserDictStats(1L, 100L, userId)).thenReturn(10);

		when(dictionaryRepository.getDictStats(201L, 300L)).thenReturn(40);
		when(userDictWordRepository.getUserDictStats(201L, 300L, userId)).thenReturn(20);

		when(dictionaryRepository.getDictStats(301L, 400L)).thenReturn(30);
		when(userDictWordRepository.getUserDictStats(301L, 400L, userId)).thenReturn(15);

		when(dictionaryRepository.getDictStats(401L, 500L)).thenReturn(25);
		when(userDictWordRepository.getUserDictStats(401L, 500L, userId)).thenReturn(12);

		List<StatisticDictionaryDto> result1 = wordStatisticsService.getDoctStatistics(userId);

		assertEquals(5, result1.size());
		assertEquals(1, result1.get(0).getLevel());
		assertEquals(50, result1.get(0).getInComonCount());
		assertEquals(10, result1.get(0).getInUserCount());

		assertEquals(2, result1.get(1).getLevel());
		assertEquals(0, result1.get(1).getInComonCount());
		assertEquals(0, result1.get(1).getInUserCount());

		// Call second time to test caching behavior
		List<StatisticDictionaryDto> result2 = wordStatisticsService.getDoctStatistics(userId);
		assertEquals(5, result2.size());

		// Verify repository was called only once per bounds due to cache
		verify(dictionaryRepository, times(1)).getDictStats(1L, 100L);
		verify(userDictWordRepository, times(1)).getUserDictStats(1L, 100L, userId);
	}
}
