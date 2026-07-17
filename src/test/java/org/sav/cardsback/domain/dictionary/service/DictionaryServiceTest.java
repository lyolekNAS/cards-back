package org.sav.cardsback.domain.dictionary.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.dto.LevelBounds;
import org.sav.cardsback.entity.DictWord;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

	@Mock
	private DictionaryRepository dictionaryRepository;

	@InjectMocks
	private DictionaryService dictionaryService;

	@Test
	void findWordToSuggest_levelZeroFallsBackToLevelOne() {
		DictWord word = new DictWord();
		word.setWordText("alpha");

		when(dictionaryRepository.findWordToSuggest(
				LevelBounds.FIRST.getBound(),
				LevelBounds.ZERO.getBound(),
				99L
		)).thenReturn(Optional.of(word));

		Optional<DictWord> result = dictionaryService.findWordToSuggest(0, 99L);

		assertTrue(result.isPresent());
		assertEquals("alpha", result.get().getWordText());
		verify(dictionaryRepository).findWordToSuggest(
				LevelBounds.FIRST.getBound(),
				LevelBounds.ZERO.getBound(),
				99L
		);
	}

	@Test
	void findWordToSuggest_levelFiveUsesLowestBand() {
		DictWord word = new DictWord();
		word.setWordText("omega");

		when(dictionaryRepository.findWordToSuggest(
				LevelBounds.FIFTH.getBound(),
				LevelBounds.FOURTH.getBound(),
				7L
		)).thenReturn(Optional.of(word));

		Optional<DictWord> result = dictionaryService.findWordToSuggest(5, 7L);

		assertTrue(result.isPresent());
		assertEquals("omega", result.get().getWordText());
		verify(dictionaryRepository).findWordToSuggest(
				LevelBounds.FIFTH.getBound(),
				LevelBounds.FOURTH.getBound(),
				7L
		);
	}
}
