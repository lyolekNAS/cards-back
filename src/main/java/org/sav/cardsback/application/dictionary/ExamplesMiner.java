package org.sav.cardsback.application.dictionary;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.springframework.beans.factory.annotation.Value;
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
public class ExamplesMiner {

	@Value("${app-props.cron.examples:47 * * * * *}")
	private String CRON;
	private static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");

	private final TaskScheduler scheduler;
	private final WordProcessingService wordProcessingService;
	private CronExpression cronExpression;

	@PostConstruct
	public void start() {
		this.cronExpression = CronExpression.parse(CRON);
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
			log.debug(">>>> Starting mineExamples");
			wordProcessingService.enrichWithExamples();
		} catch (Exception e) {
			log.error("Error during mineExamples", e);
		} finally {
			scheduleNext();
		}
	}
}
