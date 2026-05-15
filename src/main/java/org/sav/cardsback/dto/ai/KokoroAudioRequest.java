package org.sav.cardsback.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KokoroAudioRequest {

	@Builder.Default
	private String model = "kokoro";

	@Builder.Default
	private String input = "string";

	@Builder.Default
	private String voice = "af_alloy";

	@Builder.Default
	@JsonProperty("response_format")
	private String responseFormat = "mp3";

	@Builder.Default
	@JsonProperty("download_format")
	private String downloadFormat = "mp3";

	@Builder.Default
	private double speed = 1;

	@Builder.Default
	private boolean stream = false;

	@Builder.Default
	@JsonProperty("return_download_link")
	private boolean returnDownloadLink = false;

	@Builder.Default
	@JsonProperty("lang_code")
	private String langCode = "a";

	@Builder.Default
	@JsonProperty("volume_multiplier")
	private double volumeMultiplier = 1;

	@Builder.Default
	@JsonProperty("normalization_options")
	private NormalizationOptions normalizationOptions =  new NormalizationOptions();

	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NormalizationOptions {

		@Builder.Default
		private boolean normalize = true;

		@Builder.Default
		@JsonProperty("unit_normalization")
		private boolean unitNormalization = false;

		@Builder.Default
		@JsonProperty("url_normalization")
		private boolean urlNormalization = true;

		@Builder.Default
		@JsonProperty("email_normalization")
		private boolean emailNormalization = true;

		@Builder.Default
		@JsonProperty("optional_pluralization_normalization")
		private boolean optionalPluralizationNormalization = true;

		@Builder.Default
		@JsonProperty("phone_normalization")
		private boolean phoneNormalization = true;

		@Builder.Default
		@JsonProperty("replace_remaining_symbols")
		private boolean replaceRemainingSymbols = true;
	}
}
