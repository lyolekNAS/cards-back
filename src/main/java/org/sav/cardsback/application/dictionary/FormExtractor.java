package org.sav.cardsback.application.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordForm;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FormExtractor {

	private final DictWordFormRepository formRepository;
	private final DictionaryRepository wordRepository;

	@Transactional
	public List<DictWordForm> createForms(Set<String> stems, DictWord dictWord) {
		stems.forEach(stem -> {
			if(!stem.equalsIgnoreCase(dictWord.getWordText())) {
				wordRepository.deleteByWordText(stem);
				log.debug("Deleted lemma candidate from DictWord: {}", stem);
			}
		});

		List<DictWordForm> forms = stems.stream()
				.filter(w -> !w.contains(" "))
				.map(String::toLowerCase)
				.distinct()
				.map(w -> {
					DictWordForm dwf = formRepository.getFormByWordText(w)
							.orElseGet(() -> {
								DictWordForm f = new DictWordForm();
								f.setWordText(w);
								return f;
							});
					dwf.setLemma(dictWord);
					return dwf;
				})
				.collect(Collectors.toCollection(ArrayList::new));

		log.debug("Created/linked forms: {}",
				forms.stream().map(DictWordForm::getWordText).toList());

		return forms;
	}
}
