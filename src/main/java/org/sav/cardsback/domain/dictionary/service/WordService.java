package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.cardsback.mapper.WordMapper;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {
	private final WordRepository wordRepository;
	private final DictWordFormRepository dictWordFormRepository;
	private final UserDictWordRepository userDictWordRepository;
	private final WordMapper wordMapper;
	private final WordProcessingService wordProcessingService;
	private final WordStatisticsService statisticsService;
	private final WordTrainingService trainingService;

	// ==================== User Flashcard Management ====================

	public Page<Word> findAllByUserId(Long userId, String state, Pageable pageable) {
		if(state.isEmpty()) {
			return wordRepository.findAllByUserId(userId, pageable);
		} else {
			return wordRepository.findAllByUserIdAndState(userId, WordStateDto.fromName(state), pageable);
		}
	}

	@Transactional
	public Word save(Word word) {
		userDictWordRepository.deleteByUserIdAndLemma_Id(word.getUserId(), word.getDictWord().getId());
		return wordRepository.save(word);
	}

	public WordDto findByUserIdAndEnglish(Long userId, String english) {
		WordDto wordDto;
		english = dictWordFormRepository.findByWordText(english)
				.map(f -> f.getLemma() != null ? f.getLemma().getWordText() : null)
				.orElse(english);
		Optional<Word> word = wordRepository.findByUserIdAndEnglish(userId, english);
		if(word.isPresent()) {
			wordDto = wordMapper.toDto(word.get());
		} else {
			wordDto = getWordFromDict(english);
			userDictWordRepository.findByUserIdAndLemma_Id(userId, wordDto.getDictWordId()).ifPresent(udw -> {
				wordDto.setKnown(udw.isKnown());
				wordDto.setUninteresting(udw.isUninteresting());
			});
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

	// ==================== Training & Spaced Repetition ====================

	public Word findWordToTrain(Long userId) {
		return trainingService.findWordToTrain(userId);
	}

	public List<String> getWordsForRetro(Long userId) {
		return trainingService.getWordsForRetro(userId);
	}

	@Transactional
	public boolean processTrainedWord(TrainedWordDto dto, Long userId) {
		return trainingService.processTrainedWord(dto, userId);
	}

	@Transactional
	public int pickRandom5FromPause(Long userId) {
		return trainingService.pickRandom5FromPause(userId);
	}

	// ==================== Statistics & Caching ====================

	public StatisticDto getStatistics(Long userId) {
		return statisticsService.getStatistics(userId);
	}

	public List<StatisticDictionaryDto> getDoctStatistics(Long userId){
		return statisticsService.getDoctStatistics(userId);
	}

	// ==================== External/Dictionary Integration Bridge ====================

	public WordDto getWordFromDict(String word){
		DictWord dw = wordProcessingService.processWord(word);
		return wordProcessingService.dtoFromDict(dw);
	}

}
