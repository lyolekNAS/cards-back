package org.sav.cardsback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.service.WordProcessingService;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.DictTrans;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.repository.WordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
	private final WordRepository wordRepository;
	private final StateLimitService stateLimitService;
	private final WordProcessingService wordProcessingService;
	private final WordMapper wordMapper;

	private final Random random = new Random();

	public List<Word> findAllByUserId(Long userId) {
		return wordRepository.findAllByUserId(userId);
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
		return dw == null ? null : WordDto.builder()
				.english(dw.getWordText())
				.description(
						dw.getDefinitions().stream()
								.map(dwd -> dwd.getPartOfSpeach() + ": " + dwd.getDefinitionText())
								.collect(Collectors.joining("\n")))
				.ukrainian(
						dw.getTranslations().stream()
								.map(DictTrans::getWordText)
								.collect(Collectors.joining(", "))
				)
				.build();
	}
}
