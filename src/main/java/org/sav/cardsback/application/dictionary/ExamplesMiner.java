package org.sav.cardsback.application.dictionary;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class ExamplesMiner {
	private static final String CRON = "0 * 0-5,22-23 * * *";
	private static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");

	private final TaskScheduler scheduler;
	private final WordProcessingService wordProcessingService;
	private final CronExpression cronExpression = CronExpression.parse(CRON);

	@PostConstruct
	public void start() {
		scheduleNext();
	}

	private void scheduleNext() {
		ZonedDateTime nextRun = cronExpression.next(ZonedDateTime.now(ZONE));
		if (nextRun != null) {
			scheduler.schedule(this::run, nextRun.toInstant());
		}
	}

	private void run() {
		try {
			int wordsPacketSize = 100;
			log.debug(">>>> Starting mineExamples");
			long wordsWithoutExamples = wordProcessingService.countWordsWithoutExamples();
			if (wordsWithoutExamples <= wordsPacketSize) {
				log.debug(">>>> Skip mineExamples, words without examples: {}", wordsWithoutExamples);
				return;
			}
			wordProcessingService.enrichWithExamples(wordsPacketSize);
		} finally {
			scheduleNext();
		}
	}
}
