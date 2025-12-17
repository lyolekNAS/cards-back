package org.sav.cardsback.application.merriamwebster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.entity.DictWord;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class MWMiner {

	private final WordProcessingService wordProcessingService;

	@Scheduled(fixedRate = 1)
	public void mineNewWord(){
		log.debug(">>> starting action");
		DictWord dw = wordProcessingService.findUnprocessedWord().orElseThrow();
		log.debug(">>> processing word {}", dw.getWordText());
		dw = wordProcessingService.processWord(dw.getWordText());
		log.debug(">>> processed word {}", dw.getWordText());
		try {
			long randomDelay = ThreadLocalRandom.current().nextLong(1_200_000);
			Thread.sleep(randomDelay);
		} catch (InterruptedException e) {
			log.error("Error on making delay", e);
			Thread.currentThread().interrupt();
		}
	}
}
