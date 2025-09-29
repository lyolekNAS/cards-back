package org.sav.cardsback.repository;

import org.sav.cardsback.entity.Word;
import org.sav.fornas.dto.cards.StatisticAttemptDto;
import org.sav.fornas.dto.cards.StatisticComonDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

	List<Word> findAllByUserId(Long userId);
	Word findByUserIdAndEnglish(Long userId, String w);
	Word findByIdAndUserId(Long id, Long userId);


	@Query("""
        SELECT w
            FROM Word w
            WHERE w.userId = :userId
                AND w.state.id not in (0, 10)
                AND (w.nextTrain IS NULL OR w.nextTrain <= CURRENT_TIMESTAMP)
            ORDER BY function('RAND')
        """)
	List<Word> findWordToTrain(@Param("userId") Long userId, Pageable pageable);

	@Query("""
        Select new org.sav.fornas.dto.cards.StatisticAttemptDto(ws.id, count(w.id), coalesce(sum(sl.attempt - w.englishCnt), 0), coalesce(sum(sl.attempt - w.ukrainianCnt), 0))
            From WordState ws
            Left Join Word w On w.state.id = ws.id And w.userId = :userId
            Left Join StateLimit sl on sl.stateId = w.state.id
            Where (w.nextTrain IS NULL OR w.nextTrain <= CURRENT_TIMESTAMP)
            Group By ws.id
        """)
	List<StatisticAttemptDto> getStatisticAttempt(@Param("userId") Long userId);

	@Query("""
        Select new org.sav.fornas.dto.cards.StatisticComonDto(ws.id, count(w.id))
            From WordState ws
            Left Join Word w On w.state.id = ws.id And w.userId = :userId
            Group By ws.id
        """)
	List<StatisticComonDto> getStatisticCommon(@Param("userId") Long userId);

	@Modifying
	@Query("UPDATE Word w SET w.englishCnt = :count, w.lastTrain = CURRENT_TIMESTAMP WHERE w.id = :id")
	void updateEnglishCnt(@Param("id") Long id, @Param("count") Integer count);

	@Modifying
	@Query("UPDATE Word w SET w.ukrainianCnt = :count, w.lastTrain = CURRENT_TIMESTAMP WHERE w.id = :id")
	void updateUkrainianCnt(@Param("id") Long id, @Param("count") Integer count);

	@Modifying
	@Query("UPDATE Word w SET w.state.id = :stateId WHERE w.id = :id")
	void updateState(@Param("id") Long id, @Param("stateId") Integer stateId);
}
