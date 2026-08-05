package org.sav.cardsback.application.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExamplesMinerTest {

	@Mock
	private TaskScheduler scheduler;

	@Mock
	private WordProcessingService wordProcessingService;

	private ExamplesMiner examplesMiner;

	@BeforeEach
	void setUp() {
		examplesMiner = new ExamplesMiner(scheduler, wordProcessingService);
		ReflectionTestUtils.setField(examplesMiner, "CRON", "47 * * * * *");
		ReflectionTestUtils.setField(examplesMiner, "cronExpression", CronExpression.parse("47 * * * * *"));
	}

	@Test
	void start_schedulesNextRun() {
		ReflectionTestUtils.invokeMethod(examplesMiner, "start");

		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	void run_whenExamplesMinerRuns_callsEnrichExamples() {
		ReflectionTestUtils.invokeMethod(examplesMiner, "run");

		verify(wordProcessingService).enrichWithExamples();
		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}
}