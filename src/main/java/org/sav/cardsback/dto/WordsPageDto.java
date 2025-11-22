package org.sav.cardsback.dto;

import java.util.List;

public record WordsPageDto<T>(
		List<WordDto> content,
		int number,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last
) {}
