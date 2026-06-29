package org.sav.cardsback.application.merriamwebster;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.entity.DictWord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class MWMiner {

	private final TaskScheduler scheduler;
	private final WordProcessingService service;


	@Value("${app-props.mv-delay}")
	private Long baseDelay;

	@PostConstruct
	public void start() {
		scheduleNext();
	}

	private void scheduleNext() {
		long delay = ThreadLocalRandom.current().nextLong(baseDelay);

		scheduler.schedule(this::run, Instant.now().plusMillis(delay));
	}

	private void run() {
		try {
			log.debug(">>> starting action");

			DictWord dw = service.findUnprocessedWord().orElseThrow();
			service.processWord(dw.getWordText());

		} catch (Exception e) {
			log.error("Error in miner", e);
		} finally {
			scheduleNext(); //плануємо наступний запуск
		}
	}
}
