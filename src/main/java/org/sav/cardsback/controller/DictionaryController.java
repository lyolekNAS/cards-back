package org.sav.cardsback.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.application.wordnik.WordnikRandomWordImporter;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/dict")
@Slf4j
@RequiredArgsConstructor
public class DictionaryController {

	private final WordnikRandomWordImporter wordnikRandomWordImporter;
	private final WordProcessingService wordProcessingService;

	@GetMapping("/getNewWords")
	public ResponseEntity<List<WordDto>> getNewWords(@AuthenticationPrincipal Jwt jwt){
		List<WordDto> words = wordnikRandomWordImporter.importRandomWords(jwt.getClaim("userId"));
		return ResponseEntity.ok(words);
	}

	@GetMapping("/getNewWord")
	public ResponseEntity<WordDto> getNewWord(@AuthenticationPrincipal Jwt jwt){
		DictWord dw = null;
		while(dw == null) {
			dw = wordProcessingService.findUnprocessedWord();
			dw = wordProcessingService.processWord(dw.getWordText());
		}
		return ResponseEntity.ok(wordProcessingService.dtoFromDict(dw));
	}

	@GetMapping("/getWord/{word}")
	public ResponseEntity<DictWord> getWord(@PathVariable("word") String word, @AuthenticationPrincipal Jwt jwt){
		DictWord words = wordProcessingService.processWord(word);
		return ResponseEntity.ok(words);
	}

	@PostMapping("/setMark")
	public void setMarkOnWord(@RequestParam Long wordId, @RequestParam String mark, @AuthenticationPrincipal Jwt jwt){
		wordProcessingService.setMarkOnWord(wordId, mark, jwt.getClaim("userId"));
	}
}
