package org.sav.cardsback.dto.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TranslationResponseTest {

	@Test
	void deserializesNestedStructure() throws Exception {
		String json = """
				{
				  "data": {
				    "translations": [
				      {"translatedText": "привіт"},
				      {"translatedText": "вітаю"}
				    ]
				  }
				}
				""";

		TranslationResponse response = new ObjectMapper().readValue(json, TranslationResponse.class);

		assertNotNull(response.getData());
		assertEquals(2, response.getData().getTranslations().size());
		assertEquals("привіт", response.getData().getTranslations().get(0).getTranslatedText());
		assertEquals("вітаю", response.getData().getTranslations().get(1).getTranslatedText());
	}

	@Test
	void gettersAndSetters_workForNestedTypes() {
		TranslationResponse.Translation t = new TranslationResponse.Translation();
		t.setTranslatedText("hello");

		TranslationResponse.Data data = new TranslationResponse.Data();
		data.setTranslations(List.of(t));

		TranslationResponse response = new TranslationResponse();
		response.setData(data);

		assertEquals("hello", response.getData().getTranslations().getFirst().getTranslatedText());
	}
}
