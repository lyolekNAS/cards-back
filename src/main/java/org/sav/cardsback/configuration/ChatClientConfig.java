package org.sav.cardsback.configuration;


import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ChatClientConfig {

	@Bean
	public ChatClient geminiChatClient(GoogleGenAiChatModel baseChatModel) {
		return ChatClient.builder(baseChatModel)
				.defaultAdvisors(new SimpleLoggerAdvisor())
				.build();
	}

}
