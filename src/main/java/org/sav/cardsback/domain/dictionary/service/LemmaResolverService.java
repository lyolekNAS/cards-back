package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.entity.DictWord;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LemmaResolverService {

	private final DictWordFormRepository formRepository;
	private final DictionaryRepository wordRepository;

	public Optional<DictWord> findLemmaOrSelf(String wordText) {
		return formRepository.findByWordText(wordText)
				.map(form -> Optional.ofNullable(form.getLemma())
						.or(() -> wordRepository.findByWordText(wordText)))
				.orElseGet(() -> wordRepository.findByWordText(wordText));
	}
}

