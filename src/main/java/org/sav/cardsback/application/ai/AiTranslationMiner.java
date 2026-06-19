package org.sav.cardsback.application.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class AiTranslationMiner {
	private static final String CRON = "17 0/10 * * * *";
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
			log.debug(">>>> Starting AiTranslationMiner");
			wordProcessingService.findWordWithoutAiTranslations()
					.ifPresentOrElse(
							wordProcessingService::enrichWithAiTranslations,
							() -> log.debug(">>>> AiTranslationMiner on the rest")
					);
		} finally {
			scheduleNext();
		}
	}
}
