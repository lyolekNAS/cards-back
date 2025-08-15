package org.sav.cards.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cards.entity.Word;
import org.sav.cards.mapper.WordMapper;
import org.sav.cards.service.WordService;
import org.sav.fornas.dto.cards.TrainedWordDto;
import org.sav.fornas.dto.cards.WordDto;
import org.sav.fornas.dto.cards.WordLangDto;
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


	@GetMapping("/all")
	public ResponseEntity<List<Word>> getAll() {
		return ResponseEntity.ok(wordService.findAll());
	}


	@GetMapping(value = "/user/all")
	public ResponseEntity<List<WordDto>> getAllByUser(@AuthenticationPrincipal Jwt jwt) {
		log.debug("getAllByUser for {}", jwt.getClaim("userId").toString());
		List<Word> words = wordService.findAllByUserId(jwt.getClaim("userId"));
		return ResponseEntity.ok(wordMapper.toDtoList(words));
	}

	@PostMapping("/save")
	public ResponseEntity<WordDto> addWord(@AuthenticationPrincipal Jwt jwt, @RequestBody WordDto wordDto) {
		log.debug("Adding word for user {}", jwt.getClaim("userId").toString());

		Word word = wordMapper.toEntity(wordDto);
		word.setUserId(jwt.getClaim("userId"));
		Word saved = wordService.save(word);
		return ResponseEntity.ok(wordMapper.toDto(saved));
	}

	@GetMapping("/find")
	public ResponseEntity<WordDto> findWord(@AuthenticationPrincipal Jwt jwt, @RequestParam("w") String w){
		log.debug("findWord {} for user {}", w, jwt.getClaim("userId").toString());
		Word word = wordService.findByUserIdAndEnglish(jwt.getClaim("userId"), w);
		return ResponseEntity.ok(wordMapper.toDto(word));
	}

	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteWord(@AuthenticationPrincipal Jwt jwt, @RequestParam("id") Long id){
		log.debug("deleteWord {} for user {}", id, jwt.getClaim("userId").toString());
		Word word = wordService.findByIdAndUserId(id, jwt.getClaim("userId"));
		wordService.delete(word);
		return ResponseEntity.ok("deleted");
	}

	@GetMapping("/train")
	public ResponseEntity<WordDto> findWordToTrain(@AuthenticationPrincipal Jwt jwt){
		log.debug(">>> JWT is {}", jwt.getClaims());
		log.debug(">>> JWT протухне {}", jwt.getExpiresAt());
		log.debug(">>> findWordToTrain for user {}", jwt.getClaim("userId").toString());
		Word word = wordService.findWordToTrain(jwt.getClaim("userId"));
		WordDto wordDto = wordMapper.toDto(word);
		if (word.getEnglishCnt() < word.getUkrainianCnt()) {
			wordDto.setLang(WordLangDto.EN);
		} else {
			wordDto.setLang(WordLangDto.UA);
		}
		log.debug(">>> found word {}", wordDto);
		return ResponseEntity.ok(wordDto);
	}

	@PostMapping("/trained")
	public ResponseEntity<String> processTrainedWord(@AuthenticationPrincipal Jwt jwt, @RequestBody TrainedWordDto trainedWordDto){
		log.debug(">>> processTrainedWord for user {}", jwt.getClaim("userId").toString());
		log.debug(">>> trainedWord {}", trainedWordDto);
		boolean resp = wordService.processTrainedWord(trainedWordDto, jwt.getClaim("userId"));
		return resp ? ResponseEntity.ok("trained") : ResponseEntity.badRequest().body("error");
	}


}
