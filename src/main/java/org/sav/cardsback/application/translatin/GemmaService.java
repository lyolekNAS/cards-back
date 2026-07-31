package org.sav.cardsback.application.translatin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.configuration.option.SystemPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GemmaService {

    private final ChatClient geminiChatClient;

    public String callModel(String model, SystemPrompt systemPrompt, Map<String, Object> promptVariables, String userPrompt, GoogleGenAiThinkingLevel thinkingLevel) {
        ChatResponse response = geminiChatClient.prompt()
                .system(systemPrompt.prompt().render(promptVariables))
                .user(userPrompt)
                .options(
                        GoogleGenAiChatOptions.builder()
                                .temperature(0D)
                                .model(model)
                                .thinkingLevel(thinkingLevel)
                                .responseMimeType("application/json")
                                .build()
                )
                .call()
                .chatResponse();

        if (response == null || response.getResults().isEmpty()) {
            log.warn("No results from chat response for model='{}'.", model);
            return null;
        }

        return response.getResults().stream()
                .map(g -> g.getOutput().getText())
                .filter(text -> text != null && !text.isBlank())
                .reduce((a, b) -> b)
                .orElse(null);
    }
}
