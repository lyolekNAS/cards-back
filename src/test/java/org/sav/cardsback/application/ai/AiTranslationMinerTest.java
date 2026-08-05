package org.sav.cardsback.application.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.entity.DictWord;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTranslationMinerTest {

    @Mock
    private TaskScheduler scheduler;

    @Mock
    private WordProcessingService wordProcessingService;

    @InjectMocks
    private AiTranslationMiner aiTranslationMiner;

    @BeforeEach
    void setUp() {
        // Set cron property
        ReflectionTestUtils.setField(aiTranslationMiner, "CRON", "0 0 * * * *");
        // Initialize cronExpression directly
        ReflectionTestUtils.setField(aiTranslationMiner, "cronExpression", CronExpression.parse("0 0 * * * *"));
    }

    @Test
    void start_SchedulesNextTask() {
        aiTranslationMiner.start();
        verify(scheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void run_WhenWordFound_EnrichesAndSchedulesNext() {
        DictWord dictWord = new DictWord();
        dictWord.setWordText("test");
        when(wordProcessingService.findWordWithoutAiTranslations()).thenReturn(Optional.of(dictWord));

        // Use reflection to call the private run method
        ReflectionTestUtils.invokeMethod(aiTranslationMiner, "run");

        verify(wordProcessingService).enrichWithAiTranslations(dictWord);
        verify(scheduler, atLeastOnce()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void run_WhenNoWordFound_OnlySchedulesNext() {
        when(wordProcessingService.findWordWithoutAiTranslations()).thenReturn(Optional.empty());

        // Use reflection to call the private run method
        ReflectionTestUtils.invokeMethod(aiTranslationMiner, "run");

        verify(wordProcessingService, never()).enrichWithAiTranslations(any(DictWord.class));
        verify(scheduler, atLeastOnce()).schedule(any(Runnable.class), any(Instant.class));
    }
}
