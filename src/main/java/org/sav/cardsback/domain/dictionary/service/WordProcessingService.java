package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.application.ai.OpenAIRequester;
import org.sav.cardsback.application.dictionary.DefinitionExtractor;
import org.sav.cardsback.application.dictionary.FormExtractor;
import org.sav.cardsback.application.dictionary.SynonymExtractorService;
import org.sav.cardsback.application.translatin.TranslationService;
import org.sav.cardsback.domain.dictionary.model.PartOfSpeech;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.domain.dictionary.model.mw.MWEntry;
import org.sav.cardsback.domain.dictionary.repository.DictTransRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictTrans;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordExamples;
import org.sav.cardsback.entity.UserDictWord;
import org.sav.cardsback.application.merriamwebster.MWClient;
import org.sav.cardsback.application.merriamwebster.SynonymExtractor;
import org.sav.cardsback.mapper.WordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordProcessingService {

	private final DictionaryService dictionaryService;
	private final DictTransRepository dictTransRepository;
	private final UserDictWordRepository userDictWordRepository;
	private final MWClient mwClient;
	private final FormExtractor formExtractor;
	private final DefinitionExtractor definitionExtractor;
	private final TranslationService translationService;
	private final SynonymExtractorService synonymExtractorService;
	private final LemmaResolverService lemmaResolverService;
	private final WordMapper wordMapper;
	private final OpenAIRequester openAIRequester;

	@Transactional
	public DictWord processWord(String word) {
		int cnt = 0;
		while (cnt++ < 2) {
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
			log.debug("entries: {}", entries);
			if (entries.isEmpty()) {
				dictWord.addState(WordStates.FAKE);
				dictWord = dictionaryService.save(dictWord);
				log.info("Word {} is FAKE!!!", dictWord.getWordText());
				return dictWord;
			}

			String mostFrequent = entries.stream()
					.map(e -> e.getMeta().getId())
					.collect(Collectors.groupingBy(s -> s, LinkedHashMap::new, Collectors.counting()))
					.entrySet()
					.stream()
					.max(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey)
					.orElse(entries.getFirst().getMeta().getId())
					.split(":", 2)[0];
			log.debug("mostFrequent: {}", mostFrequent);

			if (!mostFrequent.equalsIgnoreCase(word)) {
				word = mostFrequent;
				log.debug("changing word for: {}", word);
				dictWord = getDictWord(word);
				if (dictWord.hasState(WordStates.MERR_WEBSTER)) {
					log.debug("{} already processed as lemma {}", word, dictWord.getWordText());
					return dictWord;
				}
				continue;
			}

			prepareWord(dictWord, entries);

			dictWord = dictionaryService.save(dictWord);

			log.info("Processed '{}': forms={}, defs={}",
					word, dictWord.getForms().size(), dictWord.getDefinitions().size());
			return dictWord;
		}
		log.info(">>>>>>>>>>>>>>>>>>>>>ATTENTION<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return null;
	}

	public boolean isWordSuitable(Long userId, DictWord word){
		return !dictionaryService.existsByUserAndDictWord(userId, word.getId());
	}

	@Transactional(readOnly = true)
	public WordDto dtoFromDict(DictWord dw){
		if (dw == null) {
			return null;
		}

		return wordMapper.toDto(loadDetailedWord(dw));
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
		return dictionaryService.findWordToProcess(WordStates.MERR_WEBSTER.getId() | WordStates.FAKE.getId(), 0);
	}

	public Optional<DictWord> findWordWithoutExamples(){
		return dictionaryService.findWordToProcess(WordStates.WITH_EXAMPLES.getId() | WordStates.FAKE.getId(), WordStates.MERR_WEBSTER.getId());
	}

	public Optional<DictWord> findWordWithoutAiTranslations(){
		return dictionaryService.findWordToProcess(WordStates.AI_TRANSLATED.getId() | WordStates.FAKE.getId(), WordStates.MERR_WEBSTER.getId());
	}

	public long countWordsWithoutExamples() {
		return dictionaryService.countWordsToProcess(
				WordStates.WITH_EXAMPLES.getId() | WordStates.FAKE.getId(),
				WordStates.MERR_WEBSTER.getId()
		);
	}

	@Transactional
	public WordDto enrichWithAiTranslations(DictWord dw){
		DictWord detailed = loadDetailedWord(dw);

		List<DictTrans> translations = new ArrayList<>(translationService.getTranslations(detailed));
		dictTransRepository.deleteByLemmaId(detailed.getId());
		detailed.getTranslations().clear();
		for (DictTrans translation : translations) {
			translation.setLemma(detailed);
		}
		detailed.getTranslations().addAll(translations);

		detailed.addState(WordStates.AI_TRANSLATED);
		return dtoFromDict(dictionaryService.save(detailed));
	}

	public WordDto enrichWithExamples(DictWord dw){
		List<String> examples = openAIRequester.getExamples(dw.getWordText());
		if(examples.isEmpty())
			return null;
		List<DictWordExamples> dwEx = examples.stream()
				.map(s -> {
					DictWordExamples dwex = new DictWordExamples();
					dwex.setLemma(dw);
					dwex.setExample(s);
					return dwex;
				})
				.collect(Collectors.toCollection(ArrayList::new));
		dw.setExamples(dwEx);
		dw.addState(WordStates.WITH_EXAMPLES);
		return dtoFromDict(dictionaryService.save(dw));
	}

	private DictWord loadDetailedWord(DictWord dw) {
		DictWord detailed = dw;
		if (dw != null && dw.getId() != null) {
			detailed = dictionaryService.findById(dw.getId()).orElse(dw);
		}

		if (detailed.getDefinitions() != null) {
			detailed.getDefinitions().size();
		}
		if (detailed.getTranslations() != null) {
			detailed.getTranslations().size();
		}
		if (detailed.getExamples() != null) {
			detailed.getExamples().size();
		}
		if (detailed.getForms() != null) {
			detailed.getForms().size();
		}

		return detailed;
	}

	@Transactional
	public void enrichWithExamples(int n){
		for (int i = 0; i < n; i++) {
			Optional<DictWord> dw = findWordWithoutExamples();
			if (dw.isEmpty()) {
				break;
			}
			WordDto processed = enrichWithExamples(dw.get());
			log.debug(">>>> Examples mined: {}", processed);
		}
	}

	public WordDto enrichWithExamples(String word){
		Optional<DictWord> dw = dictionaryService.findByWordText(word);
		if(dw.isPresent() && dw.get().hasNoState(WordStates.WITH_EXAMPLES)) {
			log.debug("dw: {}", dw.get());
			return enrichWithExamples(dw.get());
		}
		return null;
	}

	private DictWord getDictWord(String word) {
		return lemmaResolverService.findLemmaOrSelf(word)
				.orElseGet(() -> {
					DictWord dw = new DictWord();
					dw.setWordText(word);
					return dictionaryService.save(dw);
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

	private void prepareWord(DictWord dictWord, List<MWEntry> entries){

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
		dictWord.addState(WordStates.AI_TRANSLATED);
	}
}
