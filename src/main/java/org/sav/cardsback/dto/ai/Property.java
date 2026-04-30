package org.sav.cardsback.dto.ai;

import java.util.Map;

public record Property(
		String type,
		Map<String, Object> items,
		Integer minItems,
		Integer maxItems
) {
}
