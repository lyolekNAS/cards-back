package org.sav.cardsback.repository;

import org.sav.cardsback.entity.Word;
import org.sav.fornas.dto.cards.WordDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

	List<Word> findAllByUserId(Long userId);
	Word findByUserIdAndEnglish(Long userId, String w);
	Word findByIdAndUserId(Long id, Long userId);


	@Query("SELECT w FROM Word w WHERE w.userId = :userId AND w.state.id not in (0, 10) AND (w.nextTrain IS NULL OR w.nextTrain <= CURRENT_TIMESTAMP)")
	List<Word> findWordToTrain(
			@Param("userId") Long userId
	);

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
