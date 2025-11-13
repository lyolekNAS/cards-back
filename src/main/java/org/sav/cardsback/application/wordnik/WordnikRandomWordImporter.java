package org.sav.cardsback.application.wordnik;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.model.PartOfSpeech;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.entity.DictWord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WordnikRandomWordImporter {

	private final WordProcessingService wordProcessingService;
	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Value("${wordnik_api_key}")
	private String apiKey;

	private static final String API_URL =
			"https://api.wordnik.com/v4/words.json/randomWords" +
					"?hasDictionaryDef=true" +
					"&minCorpusCount=5000" +
					"&maxCorpusCount=50000" +
					"&minDictionaryCount=1" +
					"&maxDictionaryCount=-1" +
					"&minLength=1" +
					"&maxLength=-1" +
					"&limit=10" +
					"&api_key=%s" +
					"&includePartOfSpeech=%s";

	@Transactional
	public List<DictWord> importRandomWords() {
		String url = String.format(API_URL, apiKey, PartOfSpeech.getAllDisplayNamesLowercase());
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			List<Map<String, Object>> data = objectMapper.readValue(
					response.getBody(), new TypeReference<>() {}
			);

			List<DictWord> result = new ArrayList<>();

			for (Map<String, Object> entry : data) {
				String word = ((String) entry.get("word")).toLowerCase();

				if (word.contains(" ")) {
					log.debug("Skipping multi-word entry: {}", word);
					continue;
				}

				DictWord processed = wordProcessingService.processWord(word);
				if(processed != null) {
					result.add(processed);
					log.debug("Processed word: {}", processed.getWordText());
				}
			}

			return result;
		} catch (Exception e) {
			log.error("Error calling API: {}", e.getMessage(), e);
			return List.of();
		}
	}
}