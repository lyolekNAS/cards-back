package org.sav.cardsback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.repository.WordRepository;
import org.sav.fornas.dto.cards.StateLimitDto;
import org.sav.fornas.dto.cards.TrainedWordDto;
import org.sav.fornas.dto.cards.WordLangDto;
import org.sav.fornas.dto.cards.WordStateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
	private final WordRepository wordRepository;
	private final StateLimitService stateLimitService;

	private final Random random = new Random();

	public List<Word> findAllByUserId(Long userId) {
		return wordRepository.findAllByUserId(userId);
	}

	public Word save(Word word) {
		return wordRepository.save(word);
	}

	public Word findByUserIdAndEnglish(Long userId, String english) {
		return wordRepository.findByUserIdAndEnglish(userId, english);
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
		List<Word> words = wordRepository.findWordToTrain(userId);
		if (words.isEmpty()) {
			return null;
		}
		return words.get(random.nextInt(words.size()));
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

		word.setLastTrain(LocalDateTime.now());
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
		word.setNextTrain(LocalDateTime.now().plusDays(stateLimit.getDelay()));
		word.setEnglishCnt(0);
		word.setUkrainianCnt(0);
	}
}
