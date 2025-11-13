package org.sav.cardsback.application.translatin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.model.mymemory.MatchItem;
import org.sav.cardsback.domain.dictionary.model.mymemory.MyMemoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyMemoryTranslator implements ITranslator {

	private final RestTemplate restTemplate;

	@Value("${app-props.url.my-memory}")
	private String baseURL;
	@Value("${app-props.email}")
	private String email;


	@Override
	public List<String> processWord(String word){
		MyMemoryResponse resp = translate(word);
		if(resp == null){
			return new ArrayList<>();
		}
		return resp.getMatches().stream()
				.filter(mi -> mi.getSegment().equalsIgnoreCase(word))
				.map(MatchItem::getTranslation)
				.filter(w -> !w.equalsIgnoreCase(word))
				.toList();
	}

	private MyMemoryResponse translate(String text) {
		String url = UriComponentsBuilder.fromHttpUrl(baseURL)
				.queryParam("q", text)
				.queryParam("langpair",  "en|uk")
				.queryParam("de", email)
				.toUriString()
				.replace("%7C", "|");
		return restTemplate.getForObject(url, MyMemoryResponse.class);
	}
}
