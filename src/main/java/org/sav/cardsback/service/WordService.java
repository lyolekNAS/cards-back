package org.sav.cardsback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.repository.WordRepository;
import org.sav.fornas.dto.cards.TrainedWordDto;
import org.sav.fornas.dto.cards.WordLangDto;
import org.sav.fornas.dto.cards.WordStateDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
	private final WordRepository wordRepository;

	public List<Word> findAllByUserId(Object userId) {
		return wordRepository.findAllByUserId((Long) userId);
	}

	public Word save(Word word) {
		return wordRepository.save(word);
	}

	public Word findByUserIdAndEnglish(Object userId, String english) {
		return wordRepository.findByUserIdAndEnglish((Long) userId, english);
	}

	public Word findByIdAndUserId(Long id, Object userId) {
		return wordRepository.findByIdAndUserId(id, (Long) userId);
	}

	public void delete(Word word) {
		wordRepository.delete(word);
	}

	public List<Word> findAll() {
		return wordRepository.findAll();
	}

	public Word findWordToTrain(Object userId) {
		List<Word> words = wordRepository.findByWordToTrain((Long) userId);
		if (words.isEmpty()) {
			return null;
		}
		Random random = new Random();
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
			if(word.getEnglishCnt() == 10 && word.getUkrainianCnt() == 10){
				newState = new WordState(WordStateDto.DONE.getValue());
			} else {
				newState = new WordState(WordStateDto.STAGE_1.getValue());
			}
			word.setState(newState);
			word.setLastTrain(LocalDateTime.now());
			wordRepository.save(word);
			return true;
		}
		return false;
	}

//	private void setWordDone(Long id){
//		wordRepository.updateStateId(id, WordStateDto.LEARNED.getValue());
//
//	}
}
