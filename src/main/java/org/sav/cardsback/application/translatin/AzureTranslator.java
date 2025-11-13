package org.sav.cardsback.application.translatin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.model.azure.AzureRequest;
import org.sav.cardsback.domain.dictionary.model.azure.AzureResponse;
import org.sav.cardsback.domain.dictionary.model.azure.AzureTranslation;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AzureTranslator implements ITranslator{

	private final RestTemplate azureRestTemplate;

	@Override
	public List<String> processWord(String word){
		AzureResponse ar = translate(word);
		return ar.getTranslations().stream()
				.map(AzureTranslation::getDisplayTarget)
				.filter(w -> !w.equalsIgnoreCase(word))
				.toList();
	}

	private AzureResponse translate(String text) {
		AzureRequest ar = new AzureRequest();
		ar.setText(text);
		AzureResponse[] aResp = azureRestTemplate.postForObject("/dictionary/lookup?api-version=3.0&from=en&to=uk", List.of(ar), AzureResponse[].class);
		return aResp != null ? aResp[0] : new AzureResponse();
	}
}
