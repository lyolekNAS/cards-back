package org.sav.cardsback.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Value("${app-props.url.azure}")
	private String azureBaseURL;
	@Value("${trans-azure-key}")
	private String azureApiKey;

	@Value("${app-props.google.api-key}")
	private String googleApiKey;
	@Value("${app-props.google.translate-url}")
	private String googleBaseUrl;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public RestTemplate azureRestTemplate(RestTemplateBuilder builder) {
		return builder
				.rootUri(azureBaseURL)
				.additionalInterceptors((request, body, execution) -> {
					request.getHeaders().add("Ocp-Apim-Subscription-Key", azureApiKey);
					request.getHeaders().add("Ocp-Apim-Subscription-Region", "northeurope");
					return execution.execute(request, body);
				})
				.build();
	}

	@Bean
	public RestTemplate gTranslateRestTemplate(RestTemplateBuilder builder) {
		return builder
				.rootUri(googleBaseUrl)
				.additionalInterceptors((request, body, execution) -> {
					request.getHeaders().add("X-goog-api-key", googleApiKey);
					return execution.execute(request, body);
				})
				.build();
	}
}
