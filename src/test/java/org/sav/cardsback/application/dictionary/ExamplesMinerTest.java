package org.sav.cardsback.application.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.application.dictionary.ExamplesMiner;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

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
	void run_whenWordsWithoutExamplesLessOrEqualPacketSize_skipsEnrichAndSchedulesNext() {
		when(wordProcessingService.countWordsWithoutExamples()).thenReturn(100L);

		ReflectionTestUtils.invokeMethod(examplesMiner, "run");

		verify(wordProcessingService).countWordsWithoutExamples();
		verify(wordProcessingService, never()).enrichWithExamples(anyInt());
		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	void run_whenWordsWithoutExamplesGreaterThanPacketSize_enrichesAndSchedulesNext() {
		when(wordProcessingService.countWordsWithoutExamples()).thenReturn(101L);

		ReflectionTestUtils.invokeMethod(examplesMiner, "run");

		verify(wordProcessingService).enrichWithExamples(100);
		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	void run_whenEnrichThrows_stillSchedulesNext() {
		when(wordProcessingService.countWordsWithoutExamples()).thenReturn(101L);
		doThrow(new RuntimeException("boom")).when(wordProcessingService).enrichWithExamples(100);

		assertThrows(RuntimeException.class, () -> ReflectionTestUtils.invokeMethod(examplesMiner, "run"));
		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}
}
