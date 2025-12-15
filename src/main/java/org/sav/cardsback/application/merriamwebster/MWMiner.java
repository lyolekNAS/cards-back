package org.sav.cardsback.application.merriamwebster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.entity.DictWord;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class MWMiner {

	private final WordProcessingService wordProcessingService;

	@Scheduled(fixedRate = 1)
	public void mineNewWord(){
		log.debug(">>> starting action");
		Optional<DictWord> dw = wordProcessingService.findUnprocessedWord();
		log.debug(">>> processing word {}", dw.orElseThrow().getWordText());
		dw = wordProcessingService.processWord(dw.orElseThrow().getWordText());
		dw.ifPresent(dictWord -> log.debug(">>> processed word {}", dictWord.getWordText()));
		try {
			long randomDelay = ThreadLocalRandom.current().nextLong(1_200_000);
			Thread.sleep(randomDelay);
		} catch (InterruptedException e) {
			log.error("Error on making delay", e);
			Thread.currentThread().interrupt();
		}
	}
}
