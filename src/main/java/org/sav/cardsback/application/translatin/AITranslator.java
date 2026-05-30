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

		for(String trans: transOptions.translations()){
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

			String json = response.getResults().stream()
					.map(g -> g.getOutput().getText())
					.filter(text -> text != null && !text.isBlank())
					.reduce((a, b) -> b)
					.orElseThrow();

			EvaluationResponse eval =
					converter.convert(json);
			if(eval.answer()){
				evaluatedTrans.add(trans);
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
