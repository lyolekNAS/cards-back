package org.sav.cardsback.application.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.dto.WordDto;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class ExamplesMiner {

	private final WordProcessingService wordProcessingService;

	@Scheduled(cron = "0 * 23,0-6 * * *", zone = "Europe/Kyiv")
	public void mineExamples(){
		WordDto w = wordProcessingService.enrichWithExamples();
		log.debug("Examples mined: {}", w);
	}
}
