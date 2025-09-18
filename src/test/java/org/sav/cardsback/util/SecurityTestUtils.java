package org.sav.cardsback.util;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public class SecurityTestUtils {

	public static JwtRequestPostProcessor mockJwt(Long userId) {
		return jwt().jwt(jwt -> jwt.claim("userId", userId));
	}

}

