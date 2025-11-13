package org.sav.cardsback.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.application.wordnik.WordnikRandomWordImporter;
import org.sav.cardsback.entity.DictWord;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@Slf4j
@RequiredArgsConstructor
public class DictionaryController {

	private final WordnikRandomWordImporter wordnikRandomWordImporter;

	@GetMapping("/get-new")
	public ResponseEntity<List<DictWord>> getNewWords(@AuthenticationPrincipal Jwt jwt){
		List<DictWord> words = wordnikRandomWordImporter.importRandomWords();
		return ResponseEntity.ok(words);
	}
}
