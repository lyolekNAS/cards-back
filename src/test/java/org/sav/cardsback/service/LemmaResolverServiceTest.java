package org.sav.cardsback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.service.LemmaResolverService;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordForm;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LemmaResolverServiceTest {

	@Mock
	private DictWordFormRepository formRepository;

	@Mock
	private DictionaryRepository wordRepository;

	@InjectMocks
	private LemmaResolverService lemmaResolverService;

	@Test
	void findLemmaOrSelf_whenFormHasLemma_returnsLemma() {
		DictWord lemma = new DictWord();
		lemma.setWordText("go");

		DictWordForm form = new DictWordForm();
		form.setWordText("went");
		form.setLemma(lemma);

		when(formRepository.findByWordText("went")).thenReturn(Optional.of(form));

		Optional<DictWord> result = lemmaResolverService.findLemmaOrSelf("went");

		assertTrue(result.isPresent());
		assertEquals("go", result.get().getWordText());
		verify(wordRepository, never()).findByWordText("went");
	}

	@Test
	void findLemmaOrSelf_whenFormHasNoLemma_fallsBackToWordRepository() {
		DictWordForm form = new DictWordForm();
		form.setWordText("went");
		form.setLemma(null);

		DictWord selfWord = new DictWord();
		selfWord.setWordText("went");

		when(formRepository.findByWordText("went")).thenReturn(Optional.of(form));
		when(wordRepository.findByWordText("went")).thenReturn(Optional.of(selfWord));

		Optional<DictWord> result = lemmaResolverService.findLemmaOrSelf("went");

		assertTrue(result.isPresent());
		assertEquals("went", result.get().getWordText());
		verify(wordRepository).findByWordText("went");
	}

	@Test
	void findLemmaOrSelf_whenFormAbsentAndWordAbsent_returnsEmpty() {
		when(formRepository.findByWordText("unknown")).thenReturn(Optional.empty());
		when(wordRepository.findByWordText("unknown")).thenReturn(Optional.empty());

		Optional<DictWord> result = lemmaResolverService.findLemmaOrSelf("unknown");

		assertTrue(result.isEmpty());
		verify(wordRepository).findByWordText("unknown");
	}
}
