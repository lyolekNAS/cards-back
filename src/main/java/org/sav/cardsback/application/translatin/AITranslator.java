package org.sav.cardsback.application.translatin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.configuration.option.SystemPrompt;
import org.sav.cardsback.entity.DictWord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AITranslator implements ITranslator{


	public final ChatClient geminiChatClient;

	@Override
	public List<String> processWord(DictWord dictWord) {

		return translate(dictWord).translations();
	}

	private AITranslationResponse translate(DictWord dictWord) {

		String word = dictWord.getWordText();

		String def = dictWord.getDefinitions().stream()
				.map(dwd -> dwd.getPartOfSpeach() + ": " + dwd.getDefinitionText())
				.collect(Collectors.joining("\n"));

		AITranslationResponse transOptions = geminiChatClient.prompt()
				.system(
						SystemPrompt.TRANSLATION.prompt().render(
								Map.of("definitions", def)
						)
				)
				.user(word)
				.options(GoogleGenAiChatOptions.builder().temperature(0.1).build())
				.call()
				.entity(AITranslationResponse.class);


		List<String> evaluatedTrans = new ArrayList<>();
		record EvaluationResponse(boolean answer) {}

		BeanOutputConverter<EvaluationResponse> converter =
				new BeanOutputConverter<>(EvaluationResponse.class);
		String format = converter.getFormat();

		List<String> candidates = transOptions == null || transOptions.translations() == null
				? List.of()
				: transOptions.translations();

		if (candidates.isEmpty()) {
			log.warn("No translation candidates returned for word='{}'. Returning empty list.", word);
			return new AITranslationResponse(word, List.of());
		}

		for (String trans : candidates) {
			ChatResponse response = geminiChatClient.prompt()
					.system(SystemPrompt.EVAL_TRANSLATION.prompt()
							.render(
									Map.of(
											"format", format,
											"definitions", def
									)
							)
					)
					.user(word + " - " + trans)
					.options(
							GoogleGenAiChatOptions.builder()
									.temperature(0.1)
									.model("gemma-4-31b-it")
									.thinkingLevel(GoogleGenAiThinkingLevel.MINIMAL)
									.build()
					)
					.call()
					.chatResponse();

			String json = null;
			if (response == null || response.getResults().isEmpty()) {
				log.warn("No results from chat response for word='{}', candidate translation='{}'. Skipping evaluation.", word, trans);
			} else {
				json = response.getResults().stream()
						.map(g -> g.getOutput().getText())
						.filter(text -> text != null && !text.isBlank())
						.reduce((a, b) -> b)
						.orElse(null);

				if (json == null) {
					log.warn("No non-blank text found in chat results for word='{}', candidate='{}'. Skipping.", word, trans);
				}
			}

			if (json == null) {
				// skip this translation candidate if we couldn't obtain a JSON payload
				continue;
			}

			try {
				EvaluationResponse eval = converter.convert(json);
				if (eval.answer()) {
					evaluatedTrans.add(trans);
				}
			} catch (Exception ex) {
				log.error("Failed to convert evaluation response JSON for word='{}', candidate='{}'. Error: {}", word, trans, ex.getMessage(), ex);
				// continue with next candidate rather than failing the whole translation flow
			}
		}
		log.info("transOptions before evaluation: {}", transOptions);

		return new AITranslationResponse(word, evaluatedTrans);
	}

	public record AITranslationResponse(
			String word,
			List<String> translations
	) {
	}


}
