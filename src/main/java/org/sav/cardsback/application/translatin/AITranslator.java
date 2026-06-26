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

		BeanOutputConverter<AITranslationResponse> transConverter =
				new BeanOutputConverter<>(AITranslationResponse.class);
		String transFormat = transConverter.getFormat();

		ChatResponse transResponse = geminiChatClient.prompt()
				.system(
						SystemPrompt.TRANSLATION.prompt().render(
								Map.of(
										"format", transFormat,
										"definitions", def
								)
						)
				)
				.user(word)
				.options(
						GoogleGenAiChatOptions.builder()
								.temperature(0D)
								.model("gemma-4-31b-it")
								.thinkingLevel(GoogleGenAiThinkingLevel.HIGH)
								.responseMimeType("application/json")
								.build()
				)
				.call()
				.chatResponse();


		String transJson = null;
		if (transResponse == null || transResponse.getResults().isEmpty()) {
			log.warn("No results from chat response for word='{}'. Skipping evaluation.", word);
		} else {
			transJson = transResponse.getResults().stream()
					.map(g -> g.getOutput().getText())
					.filter(text -> text != null && !text.isBlank())
					.reduce((a, b) -> b)
					.orElse(null);
		}

		AITranslationResponse transOptions = transConverter.convert(transJson);


		List<String> evaluatedTrans = new ArrayList<>();
		record EvaluationResponse(List<String> evaluatedTranslation) {}

		BeanOutputConverter<EvaluationResponse> evalConverter =
				new BeanOutputConverter<>(EvaluationResponse.class);
		String evalFormat = evalConverter.getFormat();

		List<String> candidates = transOptions == null || transOptions.translations() == null
				? List.of()
				: transOptions.translations();

		if (candidates.isEmpty()) {
			log.warn("No translation candidates returned for word='{}'. Returning empty list.", word);
			return new AITranslationResponse(word, List.of());
		}

		ChatResponse evalResponse = geminiChatClient.prompt()
				.system(SystemPrompt.EVAL_TRANSLATION.prompt()
						.render(
								Map.of(
										"format", evalFormat,
										"definitions", def
								)
						)
				)
				.user("word - " + word + "\n\nCandidates:\n" + String.join("\n", candidates))
				.options(
						GoogleGenAiChatOptions.builder()
								.temperature(0D)
								.model("gemma-4-26b-a4b-it")
								.thinkingLevel(GoogleGenAiThinkingLevel.MINIMAL)
								.build()
				)
				.call()
				.chatResponse();

		String evalJson = null;
		if (evalResponse == null || evalResponse.getResults().isEmpty()) {
			log.warn("No results from chat response for word='{}', candidates='{}'. Skipping evaluation.", word, String.join(", ", candidates));
		} else {
			evalJson = evalResponse.getResults().stream()
					.map(g -> g.getOutput().getText())
					.filter(text -> text != null && !text.isBlank())
					.reduce((a, b) -> b)
					.orElse(null);
		}

		try {
			if (evalJson != null) {
				EvaluationResponse evals = evalConverter.convert(evalJson);
				evaluatedTrans.addAll(evals.evaluatedTranslation());
			}
		} catch (Exception ex) {
			log.error("Failed to convert evaluation response JSON for word='{}', candidates='{}'. Error: {}", word, String.join(", ", candidates), ex.getMessage(), ex);
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
