package org.sav.cardsback.application.merriamwebster;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.application.merriamwebster.MWMiner;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.entity.DictWord;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MWMinerTest {

	@Mock
	private TaskScheduler scheduler;

	@Mock
	private WordProcessingService wordProcessingService;

	private MWMiner mwMiner;

	@BeforeEach
	void setUp() {
		mwMiner = new MWMiner(scheduler, wordProcessingService);
	}

	@Test
	void start_schedulesNextRun() {
		ReflectionTestUtils.invokeMethod(mwMiner, "start");

		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	void run_whenWordExists_processesWordAndSchedulesNext() {
		DictWord dw = new DictWord();
		dw.setWordText("test");
		when(wordProcessingService.findUnprocessedWord()).thenReturn(Optional.of(dw));

		ReflectionTestUtils.invokeMethod(mwMiner, "run");

		verify(wordProcessingService).processWord("test");
		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	void run_whenNoWordFound_doesNotProcessAndStillSchedulesNext() {
		when(wordProcessingService.findUnprocessedWord()).thenReturn(Optional.empty());

		ReflectionTestUtils.invokeMethod(mwMiner, "run");

		verify(wordProcessingService, never()).processWord(any(String.class));
		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}

	@Test
	void run_whenProcessThrows_stillSchedulesNext() {
		DictWord dw = new DictWord();
		dw.setWordText("test");
		when(wordProcessingService.findUnprocessedWord()).thenReturn(Optional.of(dw));
		doThrow(new RuntimeException("boom")).when(wordProcessingService).processWord("test");

		ReflectionTestUtils.invokeMethod(mwMiner, "run");

		verify(scheduler).schedule(any(Runnable.class), any(Instant.class));
	}
}
