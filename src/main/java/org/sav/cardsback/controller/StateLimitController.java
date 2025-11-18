package org.sav.cardsback.controller;

import org.sav.cardsback.dto.StateLimitDto;
import org.sav.cardsback.dto.WordStateDto;
import org.sav.cardsback.domain.dictionary.service.StateLimitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/state")
public class StateLimitController {

	private final StateLimitService stateLimitService;

	public StateLimitController(StateLimitService stateLimitService) {
		this.stateLimitService = stateLimitService;
	}

	@GetMapping("/all")
	public ResponseEntity<List<StateLimitDto>> getAll() {
		return ResponseEntity.ok(stateLimitService.findAll());
	}

	@GetMapping("/id/{state}")
	public ResponseEntity<StateLimitDto> getById(@PathVariable WordStateDto state) {
		return ResponseEntity.ok(stateLimitService.findById(state.getId()));
	}
}
