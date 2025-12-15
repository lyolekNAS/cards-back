package org.sav.cardsback.domain.dictionary.service;

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
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictTrans;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.UserDictWord;
import org.sav.cardsback.application.merriamwebster.MWClient;
import org.sav.cardsback.application.merriamwebster.SynonymExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordProcessingService {

	private final DictionaryRepository dictionaryRepository;
	private final UserDictWordRepository userDictWordRepository;
	private final MWClient mwClient;
	private final FormExtractor formExtractor;
	private final DefinitionExtractor definitionExtractor;
	private final TranslationService translationService;
	private final SynonymExtractorService synonymExtractorService;
	private final LemmaResolverService lemmaResolverService;

	@Transactional
	public Optional<DictWord> processWord(String word) {

		DictWord dictWord = getDictWord(word);
		if (dictWord.hasState(WordStates.MERR_WEBSTER)) {
			log.debug("{} already processed", word);
			return Optional.of(dictWord);
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
			return Optional.empty();
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
			if(e.getMeta().getId().split(":", 2)[0].equalsIgnoreCase(dictWord.getWordText())) {
				if (hasSteams(e)) {
					stems.addAll(e.getMeta().getStems());
				}
				if (hasShortDefs(e)) {
					for (String def : e.getShortDef()) {
						defs.add(Map.entry(e.getFl(), def));
					}
				}
			}
			if (hasSyns(e)) {
				syns.addAll(SynonymExtractor.extractSynonymWords(e.getSyns()));
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
		return Optional.of(dictWord);
	}

	public boolean isWordSuitable(Long userId, DictWord word){
		return !dictionaryRepository.existsByUserAndDictWord(userId, word.getId());
	}

	public WordDto dtoFromDict(DictWord dw){
		return dw == null ? null : WordDto.builder()
				.dictWordId(dw.getId())
				.english(dw.getWordText())
				.description(
						dw.getDefinitions().stream()
								.map(dwd -> dwd.getPartOfSpeach() + ": " + dwd.getDefinitionText())
								.collect(Collectors.joining("\n")))
				.ukrainian(
						dw.getTranslations().stream()
								.map(DictTrans::getWordText)
								.collect(Collectors.joining(", "))
				)
				.build();

	}

	public void setMarkOnWord (Long wordId, String mark, Long userId){
		UserDictWord udw = userDictWordRepository.findByUserIdAndLemma_Id(userId, wordId)
				.orElseGet(() -> {
					UserDictWord u = new UserDictWord();
					u.setUserId(userId);
					u.setLemma(new DictWord());
					u.getLemma().setId(wordId);
					return u;
				});

		switch (mark) {
			case "KNOWN":
				udw.setKnown(true);
				udw.setUninteresting(false);
				break;
			case "SKIP":
				udw.setUninteresting(true);
				udw.setKnown(false);
				break;
			default:
				throw new IllegalArgumentException("Unknown mark:" + mark);
		}
		userDictWordRepository.save(udw);
	}

	public Optional<DictWord> findUnprocessedWord(){
		return dictionaryRepository.findWordToProcess(WordStates.MERR_WEBSTER.getId());
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

