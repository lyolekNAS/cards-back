package org.sav.cardsback.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController {

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/me")
	public Map<String, Object> meAdmin(@AuthenticationPrincipal Jwt jwt) {
		return jwt.getClaims();
	}

	@PreAuthorize("hasRole('JUNIOR')")
	@GetMapping("/junior/me")
	public Map<String, Object> meJunior(@AuthenticationPrincipal Jwt jwt) {
		return jwt.getClaims();
	}

	@GetMapping("/all/me")
	public Map<String, Object> meAll(@AuthenticationPrincipal Jwt jwt) {
		return jwt.getClaims();
	}
}
