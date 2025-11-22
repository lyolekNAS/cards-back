package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.model.WordStates;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
	private final WordRepository wordRepository;
	private final StateLimitService stateLimitService;
	private final UserDictWordRepository userDictWordRepository;
	private final WordProcessingService wordProcessingService;
	private final WordMapper wordMapper;

	private final Random random = new Random();

	public Page<Word> findAllByUserId(Long userId, String state, Pageable pageable) {
		if(state.isEmpty()) {
			return wordRepository.findAllByUserId(userId, pageable);
		} else {
			return wordRepository.findAllByUserIdAndState(userId, WordStateDto.fromName(state), pageable);
		}
	}

	public Word save(Word word) {
		return wordRepository.save(word);
	}

	public WordDto findByUserIdAndEnglish(Long userId, String english) {
		WordDto wordDto;
		Optional<Word> word = wordRepository.findByUserIdAndEnglish(userId, english);
		if(word.isPresent()) {
			wordDto = wordMapper.toDto(word.get());
		} else {
			wordDto = getWordFromDict(english);
		}
		return wordDto;
	}

	public Word findByIdAndUserId(Long id, Long userId) {
		return wordRepository.findByIdAndUserId(id, userId);
	}

	public void delete(Word word) {
		wordRepository.delete(word);
	}

	public List<Word> findAll() {
		return wordRepository.findAll();
	}

	public Word findWordToTrain(Long userId) {
		return wordRepository.findWordToTrain(userId, PageRequest.of(0, 1)).stream().findFirst().orElse(null);
	}

	public StatisticDto getStatistics(Long userId) {
		StatisticDto stat = new StatisticDto();
		stat.setStatisticsAttemptDto(wordRepository.getStatisticAttempt(userId));
		stat.setStatisticsComonDto(wordRepository.getStatisticCommon(userId));
		stat.setTotalCommonCount(stat.getStatisticsComonDto().stream().mapToLong(StatisticComonDto::getCount).sum());
		stat.setTotalAttemptCount(stat.getStatisticsAttemptDto().stream().mapToLong(StatisticAttemptDto::getCount).sum());
		stat.setTotalAttemptSum(stat.getStatisticsAttemptDto().stream().mapToLong(s -> s.getUkrainianCnt() + s.getEnglishCnt()).sum());
		stat.setTotalKnown(userDictWordRepository.countByUserIdAndIsKnown(userId, true));
		stat.setTotalUninteresting(userDictWordRepository.countByUserIdAndIsUninteresting(userId, true));
		return stat;
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
		wordRepository.save(word);
		return true;
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

		word.setState(new WordState(nextStateId));
		word.setNextTrain(OffsetDateTime.now().plusDays(stateLimit.getDelay()));
		word.setEnglishCnt(0);
		word.setUkrainianCnt(0);
	}

	private WordDto getWordFromDict(String word){
		DictWord dw = wordProcessingService.processWord(word);
		return wordProcessingService.dtoFromDict(dw);
	}
}
