package org.sav.cardsback.domain.dictionary.model.azure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AzureRequest {
	@JsonProperty("Text")
	private String text;
}
