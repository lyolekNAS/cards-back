package org.sav.cardsback.application.translatin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.configuration.option.SystemPrompt;
import org.sav.cardsback.entity.DictWord;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AITranslator implements ITranslator {

    private final GemmaService gemmaService;

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

        String transJson = gemmaService.callModel(
                "gemma-4-31b-it",
                SystemPrompt.TRANSLATION,
                Map.of("format", transConverter.getFormat(), "definitions", def),
                word,
                GoogleGenAiThinkingLevel.HIGH
        );

        AITranslationResponse transOptions = transConverter.convert(transJson);

        List<String> evaluatedTrans = new ArrayList<>();
        record EvaluationResponse(List<String> evaluatedTranslation) {}
        BeanOutputConverter<EvaluationResponse> evalConverter = new BeanOutputConverter<>(EvaluationResponse.class);

        List<String> candidates = transOptions == null || transOptions.translations() == null ? List.of() : transOptions.translations();

        if (candidates.isEmpty()) {
            log.warn("No translation candidates returned for word='{}'.", word);
            return new AITranslationResponse(word, List.of());
        }

        String evalJson = gemmaService.callModel(
                "gemma-4-26b-a4b-it",
                SystemPrompt.EVAL_TRANSLATION,
                Map.of("format", evalConverter.getFormat(), "definitions", def),
                "word - " + word + "\n\nCandidates:\n" + String.join("\n", candidates),
                GoogleGenAiThinkingLevel.MINIMAL
        );

        try {
            if (evalJson != null) {
                EvaluationResponse evals = evalConverter.convert(evalJson);
                evaluatedTrans.addAll(evals.evaluatedTranslation());
            }
        } catch (Exception ex) {
            log.error("Failed to convert evaluation response JSON for word='{}', candidates='{}'. Error: {}", word, candidates, ex.getMessage(), ex);
        }

        log.info("transOptions before evaluation: {}", transOptions);
        log.info("evaluatedTrans: {}", evaluatedTrans);

        return new AITranslationResponse(word, evaluatedTrans);
    }


	public record AITranslationResponse(
			String word,
			List<String> translations
	) {
	}


}
