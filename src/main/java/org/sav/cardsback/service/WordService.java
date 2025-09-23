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
	public boolean processTrainedWord(TrainedWordDto trainedWordDto, Long userId) {
		Word word = wordRepository.findByIdAndUserId(trainedWordDto.getId(), userId);
		if (word != null) {
			int count = trainedWordDto.getLang() == WordLangDto.EN ? word.getEnglishCnt() : word.getUkrainianCnt();
			int newCount = trainedWordDto.isSuccess() ? count + 1 : count/2;
			newCount = Math.min(newCount, 10);
			newCount = Math.max(newCount, 0);
			log.debug(">>> count:{} newCount:{}", count, newCount);
			if(trainedWordDto.getLang() == WordLangDto.EN) {
				word.setEnglishCnt(newCount);
			} else {
				word.setUkrainianCnt(newCount);
			}
			WordState newState;
			StateLimitDto stateLimit = stateLimitService.findById(word.getState().getId());
			if(Objects.equals(word.getEnglishCnt(), stateLimit.getAttempt()) && Objects.equals(word.getUkrainianCnt(), stateLimit.getAttempt())){
				newState = new WordState(stateLimit.getDelay() != 0 ? word.getState().getId() + 1 : WordStateDto.DONE.getId());
				word.setNextTrain(LocalDateTime.now().plusDays(stateLimit.getDelay()));
				word.setEnglishCnt(0);
				word.setUkrainianCnt(0);
			} else {
				newState = new WordState(WordStateDto.STAGE_1.getId());
			}
			word.setState(newState);
			word.setLastTrain(LocalDateTime.now());
			wordRepository.save(word);
			return true;
		}
		return false;
	}
}
