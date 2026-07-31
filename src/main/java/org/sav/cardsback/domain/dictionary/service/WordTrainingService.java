package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordTrainingService {
    private final WordRepository wordRepository;
    private final StateLimitService stateLimitService;

    public Word findWordToTrain(Long userId) {
        return wordRepository.findWordToTrain(userId, PageRequest.of(0, 1)).stream().findFirst().orElse(null);
    }

    public List<String> getWordsForRetro(Long userId) {
        return wordRepository.getWordsForRetro(userId, PageRequest.of(0, 200));
    }

    @Transactional
    public boolean processTrainedWord(TrainedWordDto dto, Long userId) {
        Word word = wordRepository.findByIdAndUserId(dto.getId(), userId);
        if (word == null) {
            return false;
        }

        if (dto.isSuccess()) {
            handleSuccess(word, dto);
        } else {
            handleFailure(word);
        }
        word.setLastTrain(OffsetDateTime.now());
        return true;
    }

    @Transactional
    public int pickRandom5FromPause(Long userId) {
        List<Long> ids = wordRepository.findRandomIdsForUser(userId, PageRequest.of(0, 5));
        if (ids.isEmpty()) {
            return 0;
        }
        return wordRepository.updateStateTo1(ids);
    }

    private void handleSuccess(Word word, TrainedWordDto dto) {
        incrementCounter(word, dto.getLang());

        StateLimitDto stateLimit = stateLimitService.findById(word.getState().getId());
        boolean englishReady = word.getEnglishCnt() >= stateLimit.getAttempt();
        boolean ukrainianReady = word.getUkrainianCnt() >= stateLimit.getAttempt();

        if (englishReady && ukrainianReady) {
            moveToNextState(word, stateLimit);
        }
    }

    private void handleFailure(Word word) {
        word.setEnglishCnt(0);
        word.setUkrainianCnt(0);
        word.setState(new WordState(WordStateDto.STAGE_1.getId()));
    }

    private void incrementCounter(Word word, WordLangDto lang) {
        if (lang == WordLangDto.EN) {
            word.setEnglishCnt(word.getEnglishCnt() + 1);
        } else {
            word.setUkrainianCnt(word.getUkrainianCnt() + 1);
        }
    }

    private void moveToNextState(Word word, StateLimitDto stateLimit) {
        boolean hasDelay = stateLimit.getDelay() != 0;
        Integer nextStateId = hasDelay
                ? word.getState().getId() + 1
                : WordStateDto.DONE.getId();

        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime nextTrainDay = OffsetDateTime.now()
                .plusDays(stateLimit.getDelay())
                .toLocalDate()
                .atStartOfDay(zone)
                .toOffsetDateTime();

        word.setState(new WordState(nextStateId));
        word.setNextTrain(nextTrainDay);
        word.setEnglishCnt(0);
        word.setUkrainianCnt(0);
    }
}
