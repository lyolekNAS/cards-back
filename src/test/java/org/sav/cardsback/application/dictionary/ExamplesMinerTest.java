package org.sav.cardsback.application.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import org.sav.cardsback.entity.DictWord;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
