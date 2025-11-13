package org.sav.cardsback.domain.dictionary.model.mymemory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class ResponseData {
	@JsonProperty("translatedText")
	private String translatedText;

	@JsonProperty("match")
	private double match;
}
