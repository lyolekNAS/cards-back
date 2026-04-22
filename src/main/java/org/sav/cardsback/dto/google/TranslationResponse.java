package org.sav.cardsback.dto.google;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TranslationResponse {
	private Data data;

	@Getter
	@Setter
	public static class Data {
		private List<Translation> translations;
	}

	@Getter
	@Setter
	public static class Translation {
		private String translatedText;
	}
}
