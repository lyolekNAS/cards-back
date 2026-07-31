package org.sav.cardsback.application.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.application.translatin.GemmaService;
import org.sav.cardsback.configuration.option.SystemPrompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIRequester {

	private final GemmaService gemmaService;

	public List<String> getExamples(String word){

		record ExampleResponse(List<String> examples) {}
		BeanOutputConverter<ExampleResponse> examplesConverter =
				new BeanOutputConverter<>(ExampleResponse.class);

		String respJson = gemmaService.callModel(
				"gemma-4-31b-it",
				SystemPrompt.EXAMPLES,
				Map.of("format", examplesConverter.getFormat()),
				word,
				GoogleGenAiThinkingLevel.HIGH
		);

		ExampleResponse examplesResp = examplesConverter.convert(respJson);

		log.debug("Response: {}", examplesResp);
		if (examplesResp.examples().isEmpty()) {
			throw new IllegalStateException("Empty AI response");
		}
		return examplesResp.examples();
	}
}
