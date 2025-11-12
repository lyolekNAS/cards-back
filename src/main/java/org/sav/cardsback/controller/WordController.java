package org.sav.cardsback.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.dto.StatisticDto;
import org.sav.cardsback.dto.TrainedWordDto;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.dto.WordLangDto;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.service.WordService;
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


	@GetMapping("/all")
	public ResponseEntity<List<Word>> getAll() {
		return ResponseEntity.ok(wordService.findAll());
	}


	@GetMapping(value = "/user/all")
	public ResponseEntity<List<WordDto>> getAllByUser(@AuthenticationPrincipal Jwt jwt) {
		log.debug(">>>>>> getAllByUser for {}", jwt.getClaim(CLAIM_USER_ID).toString());
		List<Word> words = wordService.findAllByUserId(jwt.getClaim(CLAIM_USER_ID));
		return ResponseEntity.ok(wordMapper.toDtoList(words));
	}

	//ToDo: додати перевірку на власність слова
	@PostMapping("/save")
	public ResponseEntity<WordDto> addWord(@AuthenticationPrincipal Jwt jwt, @RequestBody WordDto wordDto) {
		log.debug(">>>>>> addWord({})", wordDto);
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
		Word word = wordService.findByUserIdAndEnglish(jwt.getClaim(CLAIM_USER_ID), w);
		WordDto wordDto = wordMapper.toDto(word);
		log.debug(">>>>>> word={}", word);
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
}
