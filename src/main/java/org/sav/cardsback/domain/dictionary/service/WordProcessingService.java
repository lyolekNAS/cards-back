package org.sav.cardsback.domain.dictionary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.application.dictionary.DefinitionExtractor;
import org.sav.cardsback.application.dictionary.FormExtractor;
import org.sav.cardsback.application.dictionary.SynonymExtractorService;
import org.sav.cardsback.application.translatin.TranslationService;
import org.sav.cardsback.domain.dictionary.model.PartOfSpeech;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.domain.dictionary.model.mw.MWEntry;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.infrastructure.merriamwebster.MWClient;
import org.sav.cardsback.infrastructure.merriamwebster.SynonymExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordProcessingService {

	private final DictionaryRepository dictionaryRepository;
	private final MWClient mwClient;
	private final FormExtractor formExtractor;
	private final DefinitionExtractor definitionExtractor;
	private final TranslationService translationService;
	private final SynonymExtractorService synonymExtractorService;
	private final LemmaResolverService lemmaResolverService;

	@Transactional
	public DictWord processWord(String word) throws JsonProcessingException {

		DictWord dictWord = getDictWord(word);
		if (dictWord.hasState(WordStates.MERR_WEBSTER)) {
			log.debug("{} already processed", word);
			return dictWord;
		}

		String queryWord = word;
		List<MWEntry> entries = mwClient.fetchWord(word).stream()
				.filter(e -> queryWord.equalsIgnoreCase(e.getMeta().getId().split(":", 2)[0]) || e.getMeta().getStems().contains(queryWord))
				.filter(e -> PartOfSpeech.isValid(e.getFl()))
				.toList();
		if(entries.isEmpty()){
			dictWord.addState(WordStates.FAKE);
			dictionaryRepository.save(dictWord);
			log.info("Word {} is FAKE!!!", dictWord.getWordText());
			return null;
		}

		String mostFrequent = entries.stream()
				.map(e -> e.getMeta().getId())
				.collect(Collectors.groupingBy(s -> s, LinkedHashMap::new, Collectors.counting()))
				.entrySet()
				.stream()
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse(entries.getFirst().getMeta().getId());

		if(!mostFrequent.equalsIgnoreCase(word)){
			word = entries.getFirst().getMeta().getId().split(":", 2)[0];
			log.debug("changing word for: {}", word);
			dictWord = getDictWord(word);
		}

		Set<String> stems = new HashSet<>();
		Set<String> syns = new HashSet<>();
		List<Map.Entry<String, String>> defs = new ArrayList<>();

		for (MWEntry e : entries) {
			if (hasSteams(e)) {
				stems.addAll(e.getMeta().getStems());
			}
			if (hasSyns(e)) {
				syns.addAll(SynonymExtractor.extractSynonymWords(e.getSyns()));
			}
			if (hasShortDefs(e)) {
				for (String def : e.getShortDef()) {
					defs.add(Map.entry(e.getFl(), def));
				}
			}
		}

		dictWord.setForms(formExtractor.createForms(stems, dictWord));
		synonymExtractorService.saveSynonyms(syns);
		dictWord.getDefinitions().addAll(definitionExtractor.createDefinitions(dictWord, defs));
		dictWord.getTranslations().addAll(translationService.getTranslations(dictWord));

		dictWord.addState(WordStates.MERR_WEBSTER);
		dictionaryRepository.save(dictWord);

		log.info("Processed '{}': forms={}, syns={}, defs={}",
				word, dictWord.getForms().size(), syns.size(), dictWord.getDefinitions().size());
		return dictWord;
	}

	private DictWord getDictWord(String word) {
		return lemmaResolverService.findLemmaOrSelf(word)
				.orElseGet(() -> {
					DictWord dw = new DictWord();
					dw.setWordText(word);
					dictionaryRepository.save(dw);
					return dw;
				});
	}


	private boolean hasSteams(MWEntry e){
		return e.getMeta() != null && e.getMeta().getStems() != null;
	}
	private boolean hasSyns(MWEntry e){
		return e.getSyns() != null;
	}
	private boolean hasShortDefs(MWEntry e){
		return e.getShortDef() != null && e.getFl() != null;
	}
}

