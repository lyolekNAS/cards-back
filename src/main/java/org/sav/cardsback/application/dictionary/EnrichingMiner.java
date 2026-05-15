package org.sav.cardsback.application.dictionary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.dto.WordDto;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class EnrichingMiner {

	private final WordProcessingService wordProcessingService;

	@Transactional
	@Scheduled(cron = "0 * 0-6,22-23 * * *", zone = "Europe/Kyiv")
//	@Scheduled(cron = "0 * 9-10 * * *", zone = "Europe/Kyiv")
	public void mineExamples(){
		log.debug(">>>> Starting mineExamples");
		WordDto w = wordProcessingService.enrichWithExamples();
		log.debug(">>>> Examples mined: {}", w);
	}

	@Transactional
	@Scheduled(cron = "0 * 7-21 * * *", zone = "Europe/Kyiv")
//	@Scheduled(cron = "0 * 9-10 * * *", zone = "Europe/Kyiv")
	public void mineSpeech(){
		log.debug(">>>> Starting mineSpeech");
		String path = wordProcessingService.enrichWithSpeech();
		log.debug(">>>> Speech mined: {}", path);
	}
}
