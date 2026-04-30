package org.sav.cardsback.dto.ai;

import java.util.List;
import java.util.Map;

public record Schema(
		String type,
		Map<String, Property> properties,
		List<String> required
) {
}
