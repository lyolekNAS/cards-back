package org.sav.cardsback.dto.ai.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WordTranslationsResponse(

		@NotBlank
		@JsonProperty("word")
		String word,

		@NotEmpty
		@Size(min = 1, max = 5)
		@JsonProperty("translations")
		List<@NotBlank String> translations
) {
}
