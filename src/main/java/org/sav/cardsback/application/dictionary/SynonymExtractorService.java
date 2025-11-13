package org.sav.cardsback.application.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.entity.DictWord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SynonymExtractorService {

	private final DictionaryRepository dictionaryRepository;

	public void saveSynonyms(Set<String> words) {
		List<DictWord> newSynonyms = words.stream()
				.map(w -> dictionaryRepository.findByWordText(w)
						.orElseGet(() -> {
							DictWord dw = new DictWord();
							dw.setWordText(w);
							return dw;
						}))
				.filter(dw -> dw.getId() == null)
				.toList();

		if (!newSynonyms.isEmpty()) {
			dictionaryRepository.saveAll(newSynonyms);
			log.debug("Saved synonyms: {}", newSynonyms.stream().map(DictWord::getWordText).toList());
		}
	}
}

