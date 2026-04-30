package org.sav.cardsback.dto.ai.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WordExamplesResponse(

		@NotBlank
		@JsonProperty("word")
		String word,

		@NotEmpty
		@Size(min = 3, max = 3)
		@JsonProperty("examples")
		List<@NotBlank String> examples
) {
}
