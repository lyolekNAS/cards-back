package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictWordFormRepository;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.UserDictWord;
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
	private final StateLimitService stateLimitService;
	private final UserDictWordRepository userDictWordRepository;
	private final DictionaryRepository dictionaryRepository;
	private final WordProcessingService wordProcessingService;
	private final WordMapper wordMapper;
	private final DictionaryService dictionaryService;

	private final Random random = new Random();

	private record DictStatsCacheEntry(int value, long expiresAt) {
	}

	private final Map<String, DictStatsCacheEntry> dictStatsCache = new ConcurrentHashMap<>();
	private final Map<String, DictStatsCacheEntry> userStatsCache = new ConcurrentHashMap<>();
	private final long DICT_STATS_CACHE_TTL_MS = 24 * 60 * 60 * 1000L; // 1 day
	private final long USER_STATS_CACHE_TTL_MS = 1 * 60 * 60 * 1000L; // 1 hour

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

	public Word findWordToTrain(Long userId) {
		return wordRepository.findWordToTrain(userId, PageRequest.of(0, 1)).stream().findFirst().orElse(null);
	}

	public List<String> getWordsForRetro(Long userId) {
		return wordRepository.getWordsForRetro(userId, PageRequest.of(0, 200));
	}

	public StatisticDto getStatistics(Long userId) {
		StatisticDto stat = new StatisticDto();
		stat.setStatisticsAttemptDto(wordRepository.getStatisticAttempt(userId));
		stat.setStatisticsComonDto(wordRepository.getStatisticCommon(userId));
		stat.setTotalCommonCount(stat.getStatisticsComonDto().stream().filter(sa -> sa.getStateId() != WordStateDto.PAUSED.getId() && sa.getStateId() != WordStateDto.DONE.getId()).mapToLong(StatisticComonDto::getCount).sum());
		stat.setTotalAttemptCount(stat.getStatisticsAttemptDto().stream().filter(sa -> sa.getStateId() != WordStateDto.PAUSED.getId() && sa.getStateId() != WordStateDto.DONE.getId()).mapToLong(StatisticAttemptDto::getCount).sum());
		stat.setTotalAttemptSum(stat.getStatisticsAttemptDto().stream().mapToLong(s -> s.getUkrainianCnt() + s.getEnglishCnt()).sum());
		stat.setTotalKnown(userDictWordRepository.countByUserIdAndIsKnown(userId, true));
		stat.setTotalUninteresting(userDictWordRepository.countByUserIdAndIsUninteresting(userId, true));
		return stat;
	}

	public List<StatisticDictionaryDto> getDoctStatistics(Long userId){
		List<StatisticDictionaryDto> stats = new ArrayList<>();
		for(int level = 1; level <= 5; level++){
			LevelBoundsDto lb = dictionaryService.getLevelBounds(level);
			StatisticDictionaryDto stat = new StatisticDictionaryDto();
			stat.setLevel(level);
			stat.setInComonCount(getDictStatsCached(lb.lowBound(), lb.highBound()));
			stat.setInUserCount(getUserStatsCached(userId, lb.lowBound(), lb.highBound()));
			stats.add(stat);
		}
		return stats;
	}

	private Integer getDictStatsCached(long low, long high) {
		String key = low + ":" + high;
		long now = System.currentTimeMillis();
		DictStatsCacheEntry entry = dictStatsCache.get(key);
		if (entry != null && entry.expiresAt > now) {
			return entry.value;
		}
		Integer value = dictionaryRepository.getDictStats(low, high);
		if (value == null) value = 0;
		dictStatsCache.put(key, new DictStatsCacheEntry(value, now + DICT_STATS_CACHE_TTL_MS));
		return value;
	}

	private Integer getUserStatsCached(long userId, long low, long high) {
		String key = userId + ":" + low + ":" + high;
		long now = System.currentTimeMillis();
		DictStatsCacheEntry entry = userStatsCache.get(key);
		if (entry != null && entry.expiresAt > now) {
			return entry.value;
		}
		Integer value = userDictWordRepository.getUserDictStats(low, high, userId);
		if (value == null) value = 0;
		userStatsCache.put(key, new DictStatsCacheEntry(value, now + USER_STATS_CACHE_TTL_MS));
		return value;
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

		List<Long> ids = wordRepository.findRandomIdsForUser(
				userId,
				PageRequest.of(0, 5)
		);

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

	public WordDto getWordFromDict(String word){
		DictWord dw = wordProcessingService.processWord(word);
		return wordProcessingService.dtoFromDict(dw);
	}
}
