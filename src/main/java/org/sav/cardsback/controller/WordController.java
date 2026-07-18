package org.sav.cardsback.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.sav.cardsback.domain.dictionary.service.DictionaryService;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.domain.dictionary.service.WordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/word")
@Slf4j
@RequiredArgsConstructor
public class WordController {
	private final WordService wordService;
	private final WordMapper wordMapper;
	private final DictionaryService dictionaryService;
	private static final String CLAIM_USER_ID = "userId";
	private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);


	@GetMapping("/all")
	public ResponseEntity<List<Word>> getAll() {
		return ResponseEntity.ok(wordService.findAll());
	}


	@GetMapping(value = "/user/all")
	public ResponseEntity<WordsPageDto<WordDto>> getAllByUser(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "") String state,
			@AuthenticationPrincipal Jwt jwt) {

		Long userId = getUserId(jwt);
		log.debug(">>>>>> getAllByUser for {}", userId);

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		return ResponseEntity.ok(toWordsPageDto(wordService.findAllByUserId(userId, state, pageable)));
	}

	//ToDo: додати перевірку на власність слова
	@PostMapping("/save")
	public ResponseEntity<WordDto> addWord(@AuthenticationPrincipal Jwt jwt, @RequestBody WordDto wordDto) {
		Long userId = getUserId(jwt);
		log.debug(">>>>>> addWord({})", wordDto);
		wordDto.setDescription(POLICY.sanitize(wordDto.getDescription()));
		Word word = wordMapper.toEntity(wordDto);
		word.setUserId(userId);
		log.debug(">>>>>> word {}", word);
		Word saved = wordService.save(word);
		log.debug(">>>>>> saved {}", saved);
		return ResponseEntity.ok(wordMapper.toDto(saved));
	}

	@GetMapping("/find")
	public ResponseEntity<WordDto> findWord(@AuthenticationPrincipal Jwt jwt, @RequestParam("w") String w){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> findWord {} for user {}", w, userId);
		WordDto wordDto = enrichWordDto(wordService.findByUserIdAndEnglish(userId, w), true);
		log.debug(">>>>>> wordDto={}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@GetMapping("/find-card")
	public ResponseEntity<WordDto> findWordCard(@RequestParam("w") String w){
		log.debug(">>>>>> findWordCard {}", w);
		WordDto wordDto = wordService.getWordFromDict(w);
		log.debug(">>>>>> wordDto={}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@GetMapping("/{id}")
	@Operation(operationId = "getWordById")
	public ResponseEntity<WordDto> getById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> getById {} for user {}", id, userId);
		Word word = wordService.findByIdAndUserId(id, userId);
		if(word == null){
			return ResponseEntity.ok().build();
		}
		WordDto wordDto = enrichWordDto(wordMapper.toDto(word), true);
		log.debug(">>>>>> getById={}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteWord(@AuthenticationPrincipal Jwt jwt, @RequestParam("id") Long id){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> deleteWord {} for user {}", id, userId);
		Word word = wordService.findByIdAndUserId(id, userId);
		wordService.delete(word);
		return ResponseEntity.ok("deleted");
	}

	@GetMapping("/train")
	public ResponseEntity<WordDto> findWordToTrain(@AuthenticationPrincipal Jwt jwt){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> findWordToTrain for user {}", userId);
		Word word = wordService.findWordToTrain(userId);
		if(word == null){
			return ResponseEntity.ok().build();
		}
		WordDto wordDto = enrichWordDto(wordMapper.toDto(word), false);
		wordDto.setLang(selectTrainingLang(word));
		log.debug(">>>>>> found word {}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@GetMapping("/retro")
	public ResponseEntity<List<String>> getWordsForRetro(@AuthenticationPrincipal Jwt jwt){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> findWordForRetro for user {}", userId);
		List<String> words = wordService.getWordsForRetro(userId);
		if(words == null){
			return ResponseEntity.ok().build();
		}
		log.debug(">>>>>> found {} words", words.size());
		return ResponseEntity.ok(words);
	}

	@GetMapping("/statistic")
	public ResponseEntity<StatisticDto> getStatistic(@AuthenticationPrincipal Jwt jwt){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> getStatistic for user {}", userId);
		return ResponseEntity.ok(wordService.getStatistics(userId));
	}

	@GetMapping("/dict-statistic")
	public ResponseEntity<List<StatisticDictionaryDto>> getDictStatistic(@AuthenticationPrincipal Jwt jwt){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> getDictStatistic for user {}", userId);
		return ResponseEntity.ok(wordService.getDoctStatistics(userId));
	}

	@PostMapping("/trained")
	public ResponseEntity<String> processTrainedWord(@AuthenticationPrincipal Jwt jwt, @RequestBody TrainedWordDto trainedWordDto){
		Long userId = getUserId(jwt);
		log.debug(">>>>>> processTrainedWord for user {}", userId);
		log.debug(">>>>>> trainedWord {}", trainedWordDto);
		boolean resp = wordService.processTrainedWord(trainedWordDto, userId);
		return resp ? ResponseEntity.ok("trained") : ResponseEntity.badRequest().body("error");
	}

	@PostMapping("/pick5Paused")
	public int pickRandom5FromPause(@AuthenticationPrincipal Jwt jwt){
		return wordService.pickRandom5FromPause(getUserId(jwt));
	}

	private Long getUserId(Jwt jwt) {
		return jwt.getClaim(CLAIM_USER_ID);
	}

	private WordDto enrichWordDto(WordDto wordDto, boolean randomLang) {
		wordDto.setExamples(dictionaryService.getExamples(wordDto.getDictWordId()));
		wordDto.setLang(randomLang ? randomLang() : null);
		return wordDto;
	}

	private WordLangDto randomLang() {
		return ThreadLocalRandom.current().nextBoolean() ? WordLangDto.EN : WordLangDto.UA;
	}

	private WordLangDto selectTrainingLang(Word word) {
		return word.getEnglishCnt() < word.getUkrainianCnt() ? WordLangDto.EN : WordLangDto.UA;
	}

	private WordsPageDto<WordDto> toWordsPageDto(Page<Word> words) {
		return new WordsPageDto<>(
				words.getContent().stream().map(wordMapper::toDto).toList(),
				words.getNumber(),
				words.getSize(),
				words.getTotalElements(),
				words.getTotalPages(),
				words.isFirst(),
				words.isLast()
		);
	}
}
