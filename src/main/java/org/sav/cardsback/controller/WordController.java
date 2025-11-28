package org.sav.cardsback.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.domain.dictionary.service.WordService;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/word")
@Slf4j
@RequiredArgsConstructor
public class WordController {
	private final WordService wordService;
	private final WordMapper wordMapper;
	private static final String CLAIM_USER_ID = "userId";

	PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);


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

		log.debug(">>>>>> getAllByUser for {}", jwt.getClaim(CLAIM_USER_ID).toString());

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
		Page<Word> words = wordService.findAllByUserId(jwt.getClaim(CLAIM_USER_ID), state, pageable);
		WordsPageDto<WordDto> dto = new WordsPageDto<>(
				words.getContent().stream().map(wordMapper::toDto).toList(),
				words.getNumber(),
				words.getSize(),
				words.getTotalElements(),
				words.getTotalPages(),
				words.isFirst(),
				words.isLast()
		);

		return ResponseEntity.ok(dto);
	}

	//ToDo: додати перевірку на власність слова
	@PostMapping("/save")
	public ResponseEntity<WordDto> addWord(@AuthenticationPrincipal Jwt jwt, @RequestBody WordDto wordDto) {
		log.debug(">>>>>> addWord({})", wordDto);
		wordDto.setDescription(policy.sanitize(wordDto.getDescription()));
		Word word = wordMapper.toEntity(wordDto);
		word.setUserId(jwt.getClaim(CLAIM_USER_ID));
		log.debug(">>>>>> word {}", word);
		Word saved = wordService.save(word);
		log.debug(">>>>>> saved {}", saved);
		return ResponseEntity.ok(wordMapper.toDto(saved));
	}

	@GetMapping("/find")
	public ResponseEntity<WordDto> findWord(@AuthenticationPrincipal Jwt jwt, @RequestParam("w") String w){
		log.debug(">>>>>> findWord {} for user {}", w, jwt.getClaim(CLAIM_USER_ID).toString());
		WordDto wordDto = wordService.findByUserIdAndEnglish(jwt.getClaim(CLAIM_USER_ID), w);
		log.debug(">>>>>> wordDto={}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteWord(@AuthenticationPrincipal Jwt jwt, @RequestParam("id") Long id){
		log.debug(">>>>>> deleteWord {} for user {}", id, jwt.getClaim(CLAIM_USER_ID).toString());
		Word word = wordService.findByIdAndUserId(id, jwt.getClaim(CLAIM_USER_ID));
		wordService.delete(word);
		return ResponseEntity.ok("deleted");
	}

	@GetMapping("/train")
	public ResponseEntity<WordDto> findWordToTrain(@AuthenticationPrincipal Jwt jwt){
		log.debug(">>>>>> findWordToTrain for user {}", jwt.getClaim(CLAIM_USER_ID).toString());
		Word word = wordService.findWordToTrain(jwt.getClaim(CLAIM_USER_ID));
		if(word == null){
			return ResponseEntity.ok().build();
		}
		WordDto wordDto = wordMapper.toDto(word);
		if (word.getEnglishCnt() < word.getUkrainianCnt()) {
			wordDto.setLang(WordLangDto.EN);
		} else {
			wordDto.setLang(WordLangDto.UA);
		}
		log.debug(">>>>>> found word {}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@GetMapping("/statistic")
	public ResponseEntity<StatisticDto> getStatistic(@AuthenticationPrincipal Jwt jwt){
		log.debug(">>>>>> getStatistic for user {}", jwt.getClaim(CLAIM_USER_ID).toString());
		return ResponseEntity.ok(wordService.getStatistics(jwt.getClaim(CLAIM_USER_ID)));
	}

	@PostMapping("/trained")
	public ResponseEntity<String> processTrainedWord(@AuthenticationPrincipal Jwt jwt, @RequestBody TrainedWordDto trainedWordDto){
		log.debug(">>>>>> processTrainedWord for user {}", jwt.getClaim(CLAIM_USER_ID).toString());
		log.debug(">>>>>> trainedWord {}", trainedWordDto);
		boolean resp = wordService.processTrainedWord(trainedWordDto, jwt.getClaim(CLAIM_USER_ID));
		return resp ? ResponseEntity.ok("trained") : ResponseEntity.badRequest().body("error");
	}

	@PostMapping("/pick5Paused")
	public int pickRandom5FromPause(@AuthenticationPrincipal Jwt jwt){
		return wordService.pickRandom5FromPause(jwt.getClaim(CLAIM_USER_ID));
	}
}
