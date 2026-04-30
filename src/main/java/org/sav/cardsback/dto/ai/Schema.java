package org.sav.cardsback.dto.ai;

import java.util.Map;

public record Schema(
		String type,
		Map<String, Property> properties,
		String[] required
) {
}
