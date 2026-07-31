package org.sav.cardsback.domain.dictionary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sav.cardsback.domain.dictionary.repository.DictionaryRepository;
import org.sav.cardsback.domain.dictionary.repository.UserDictWordRepository;
import org.sav.cardsback.domain.dictionary.repository.WordRepository;
import org.sav.cardsback.dto.*;
import org.sav.cardsback.entity.WordState;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordStatisticsService {
    private final WordRepository wordRepository;
    private final UserDictWordRepository userDictWordRepository;
    private final DictionaryRepository dictionaryRepository;
    private final DictionaryService dictionaryService;

    private record DictStatsCacheEntry(int value, long expiresAt) {}

    private final Map<String, DictStatsCacheEntry> dictStatsCache = new ConcurrentHashMap<>();
    private final Map<String, DictStatsCacheEntry> userStatsCache = new ConcurrentHashMap<>();
    private final long DICT_STATS_CACHE_TTL_MS = 24 * 60 * 60 * 1000L;
    private final long USER_STATS_CACHE_TTL_MS = 1 * 60 * 60 * 1000L;

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
}
