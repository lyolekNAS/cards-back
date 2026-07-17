package org.sav.cardsback.domain.dictionary.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.application.ai.OpenAIRequester;
import org.sav.cardsback.application.dictionary.DefinitionExtractor;
import org.sav.cardsback.application.dictionary.FormExtractor;
import org.sav.cardsback.application.dictionary.SynonymExtractorService;
import org.sav.cardsback.application.merriamwebster.MWClient;
import org.sav.cardsback.application.translatin.AITranslator;
import org.sav.cardsback.application.translatin.GoogleTranslator;
import org.sav.cardsback.application.translatin.TranslationService;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.domain.dictionary.repository.DictTransRepository;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.UserDictWord;
import org.sav.cardsback.mapper.WordMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordProcessingServiceTest {

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private DictTransRepository dictTransRepository;

	@Mock
	private UserDictWordRepository userDictWordRepository;

	@Mock
	private DictWordFormRepository dictWordFormRepository;

	@Mock
	private DictionaryRepository wordRepository;

	@Mock
	private MWClient mwClient;

	@Mock
	private FormExtractor formExtractor;

	@Mock
	private DefinitionExtractor definitionExtractor;

	@Mock
	private TranslationService translationService;

	@Mock
	private SynonymExtractorService synonymExtractorService;

	@Mock
	private LemmaResolverService lemmaResolverService;

	@Mock
	private WordMapper wordMapper;

	@Mock
	private OpenAIRequester openAIRequester;

	@Mock
	private EntityManager entityManager;

	@Mock
	private GoogleTranslator googleTranslator;

	@Mock
	private AITranslator aiTranslator;

	@InjectMocks
	private WordProcessingService wordProcessingService;

	@Test
	void dtoFromDict_whenNull_returnsNull() {
		assertNull(wordProcessingService.dtoFromDict(null));
		verifyNoInteractions(dictionaryService, wordMapper);
	}

	@Test
	void dtoFromDict_loadsDetailedWordAndMapsDto() {
		DictWord source = new DictWord();
		source.setId(7L);
		source.setWordText("hello");

		DictWord detailed = new DictWord();
		detailed.setId(7L);
		detailed.setWordText("hello");

		WordDto dto = new WordDto();
		dto.setEnglish("hello");

		when(dictionaryService.findById(7L)).thenReturn(Optional.of(detailed));
		when(wordMapper.toDto(detailed)).thenReturn(dto);

		WordDto result = wordProcessingService.dtoFromDict(source);

		assertEquals(dto, result);
		verify(dictionaryService).findById(7L);
		verify(wordMapper).toDto(detailed);
	}

	@Test
	void findUnprocessedWord_delegatesToDictionaryService() {
		DictWord word = new DictWord();
		word.setWordText("test");

		when(dictionaryService.findWordToProcess(WordStates.MERR_WEBSTER.getId() | WordStates.FAKE.getId(), 0))
				.thenReturn(Optional.of(word));

		Optional<DictWord> result = wordProcessingService.findUnprocessedWord();

		assertTrue(result.isPresent());
		assertEquals("test", result.get().getWordText());
		verify(dictionaryService).findWordToProcess(WordStates.MERR_WEBSTER.getId() | WordStates.FAKE.getId(), 0);
	}

	@Test
	void setMarkOnWord_knownCreatesNewMark() {
		long userId = 12L;
		long wordId = 34L;
		when(userDictWordRepository.findByUserIdAndLemma_Id(userId, wordId)).thenReturn(Optional.empty());

		wordProcessingService.setMarkOnWord(wordId, "KNOWN", userId);

		ArgumentCaptor<UserDictWord> captor = ArgumentCaptor.forClass(UserDictWord.class);
		verify(userDictWordRepository).save(captor.capture());
		UserDictWord saved = captor.getValue();
		assertEquals(userId, saved.getUserId());
		assertEquals(wordId, saved.getLemma().getId());
		assertTrue(saved.isKnown());
		assertFalse(saved.isUninteresting());
	}
}
