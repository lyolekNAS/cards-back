package org.sav.cardsback.application.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.dto.ai.ChatRequest;
import org.sav.cardsback.dto.ai.KokoroAudioRequest;
import org.sav.cardsback.dto.ai.WordExamplesRequestFactory;
import org.sav.cardsback.dto.ai.response.ChatCompletionResponse;
import org.sav.cardsback.dto.ai.response.WordExamplesResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIRequester {

	private final RestTemplate localAIRestTemplate;
	private final RestTemplate kokoroRestTemplate;
	private static final String AI_URI = "/v1/chat/completions";
	private static final String SPEECH_URI = "/v1/audio/speech";

	public List<String> getExamples(String w){
		ChatRequest cr = WordExamplesRequestFactory.create(w);
		ChatCompletionResponse ccr = localAIRestTemplate.postForObject(AI_URI, cr, ChatCompletionResponse.class);
		log.debug("Response: {}", ccr);
		if (ccr == null || ccr.choices().isEmpty()) {
			throw new IllegalStateException("Empty AI response");
		}
		return ccr.contentAs(WordExamplesResponse.class).examples();
	}

	public byte[] getSpeech(String w){
		KokoroAudioRequest  kar = KokoroAudioRequest.builder()
				.input(w)
				.build();

//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(kar);
//			log.info("Відправка запиту до Kokoro: \n{}", json);
//		}catch (JsonProcessingException e) {
//			log.error("Error", e);
//		}

		byte[] resp = kokoroRestTemplate.postForObject(SPEECH_URI, kar, byte[].class);
		if (resp == null || resp.length == 0) {
			throw new IllegalStateException("Empty response");
		}
		log.debug("Response size: {}", resp.length);
		return resp;
	}

//	public List<String> getExamples(String w){
//		ChatRequest cr = WordExamplesRequestFactory.create(w);
//		log.debug("ChatRequest: {}", cr);
//		String resp = localAIRestTemplate.postForObject(AI_URI, cr, String.class);
//		log.debug("Response: {}", resp);
//		return List.of();
//	}
}
