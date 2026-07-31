package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.application.ai.OpenAIRequester;
import org.sav.cardsback.application.dictionary.DefinitionExtractor;
import org.sav.cardsback.application.dictionary.FormExtractor;
import org.sav.cardsback.application.dictionary.SynonymExtractorService;
import org.sav.cardsback.application.translatin.AITranslator;
import org.sav.cardsback.application.translatin.GoogleTranslator;
import org.sav.cardsback.application.translatin.TranslationService;
import org.sav.cardsback.domain.dictionary.model.PartOfSpeech;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.domain.dictionary.model.mw.MWEntry;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.*;
import org.sav.cardsback.application.merriamwebster.MWClient;
import org.sav.cardsback.application.merriamwebster.SynonymExtractor;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordProcessingService {

	private static final int MAX_PROCESSING_ATTEMPTS = 2;

	private final DictionaryService dictionaryService;
	private final UserDictWordRepository userDictWordRepository;
	private final DictWordFormRepository dictWordFormRepository;
	private final DictionaryRepository wordRepository;
	private final MWClient mwClient;
	private final FormExtractor formExtractor;
	private final DefinitionExtractor definitionExtractor;
	private final TranslationService translationService;
	private final SynonymExtractorService synonymExtractorService;
	private final LemmaResolverService lemmaResolverService;
	private final WordMapper wordMapper;
	private final OpenAIRequester openAIRequester;
	private final GoogleTranslator googleTranslator;
	private final AITranslator aiTranslator;

	@Transactional
	public DictWord processWord(String word) {
		String currentWord = word;
		int attempt = 0;
		while (attempt++ < MAX_PROCESSING_ATTEMPTS) {
			DictWord dictWord = getDictWord(currentWord);
			if (dictWord.hasState(WordStates.MERR_WEBSTER) || dictWord.hasState(WordStates.FAKE)) {
				log.debug("{} already processed", currentWord);
				return dictWord;
			}

			List<MWEntry> entries = fetchValidEntries(currentWord);
			if (entries.isEmpty()) {
				return markAsFake(dictWord);
			}

			String mostFrequentLemma = determineMostFrequentLemma(entries);

			if (!mostFrequentLemma.equalsIgnoreCase(currentWord)) {
				currentWord = mostFrequentLemma;
				log.debug("changing word for: {}", currentWord);
				dictWord = getDictWord(currentWord);
				if (dictWord.hasState(WordStates.MERR_WEBSTER)) {
					handleExistingLemma(dictWord, word);
					return dictWord;
				}
				continue;
			}

			prepareWord(dictWord, entries);
			dictWord = dictionaryService.save(dictWord);

			log.info("Processed '{}': forms={}, defs={}",
					currentWord, dictWord.getForms().size(), dictWord.getDefinitions().size());
			return dictWord;
		}
		log.info(">>>>>>>>>>>>>>>>>>>>>ATTENTION<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return null;
	}

	private List<MWEntry> fetchValidEntries(String word) {
		List<MWEntry> entries = mwClient.fetchWord(word).stream()
				.filter(e -> word.equalsIgnoreCase(e.getMeta().getId().split(":", 2)[0]) || e.getMeta().getStems().contains(word))
				.filter(e -> PartOfSpeech.isValid(e.getFl()))
				.toList();
		log.debug("entries: {}", entries);
		return entries;
	}

	private DictWord markAsFake(DictWord dictWord) {
		dictWord.addState(WordStates.FAKE);
		DictWord saved = dictionaryService.save(dictWord);
		log.info("Word {} is FAKE!!!", saved.getWordText());
		return saved;
	}

	private String determineMostFrequentLemma(List<MWEntry> entries) {
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
		return mostFrequent;
	}

	private void handleExistingLemma(DictWord dictWord, String originWord) {
		log.debug("{} already processed as lemma {}", originWord, dictWord.getWordText());
		if (dictWord.getForms().stream().noneMatch(f -> f.getWordText().equals(originWord))) {
			log.debug("adding lemma {}", originWord);
			DictWordForm originForm = dictWordFormRepository.findByWordText(originWord)
					.orElseGet(() -> {
						DictWordForm dwf = new DictWordForm();
						dwf.setWordText(originWord);
						return dwf;
					});
			originForm.setLemma(dictWord);

			log.info("new form: {} for {} with {}", originForm.getWordText(), originForm.getLemma().getWordText(), originForm.getFreq());

			dictWord.getForms().add(originForm);
		}
		DictWord originDictWord = dictionaryService.findByWordText(originWord).orElseThrow(NoSuchElementException::new);
		dictionaryService.resetWord(originDictWord.getId());
		wordRepository.deleteByWordText(originWord);
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

		List<DictTrans> translations = new ArrayList<>(detailed.getTranslations());
		translations.addAll(
				translationService.getTranslations(detailed, aiTranslator).stream()
						.filter(dt -> translations.stream().noneMatch(dtt -> dtt.getWordText().equals(dt.getWordText())))
						.toList()
		);
		detailed.getTranslations().clear();
		for (DictTrans translation : translations) {
			translation.setLemma(detailed);
		}
		detailed.getTranslations().addAll(translations);

		detailed.addState(WordStates.AI_TRANSLATED);
		return dtoFromDict(dictionaryService.save(detailed));
	}

	@Transactional
	public WordDto enrichWithAiTranslations(String word){
		Optional<DictWord> dw = dictionaryService.findByWordText(word);
		if(dw.isPresent() && dw.get().hasNoState(WordStates.AI_TRANSLATED)) {
			return enrichWithAiTranslations(dw.get());
		}
		return null;
	}

	@Transactional
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

	@Transactional
	public void enrichWithExamples(){
		Optional<DictWord> dw = findWordWithoutExamples();
		if (dw.isEmpty()) {
			log.debug(">>>> MineExamples is resting");
			return;
		}
		log.debug(">>>> mineExamples for {}", dw.get().getWordText());
		WordDto processed = enrichWithExamples(dw.get());
		log.debug(">>>> Examples mined: {}", processed);
	}

	@Transactional
	public WordDto enrichWithExamples(String word){
		Optional<DictWord> dw = dictionaryService.findByWordText(word);
		if(dw.isPresent() && dw.get().hasNoState(WordStates.WITH_EXAMPLES)) {
			return enrichWithExamples(dw.get());
		}
		return null;
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
					stems.addAll(e.getMeta().getStems().stream().map(StringTools::normalize).toList());
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
		dictWord.getTranslations().addAll(translationService.getTranslations(dictWord, googleTranslator));

		dictWord.addState(WordStates.MERR_WEBSTER);
	}
}

