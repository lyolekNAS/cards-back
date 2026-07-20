package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.UserDictWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserDictWordRepository extends JpaRepository<UserDictWord, Long> {
	Optional<UserDictWord> findByUserIdAndLemma_Id(Long userId, Long wordId);
	Long countByUserIdAndIsKnown(Long userId, boolean isKnown);
	Long countByUserIdAndIsUninteresting(Long userId, boolean isUninteresting);
	void deleteByUserIdAndLemma_Id(Long userId, Long wordId);

	@Query("""
        SELECT cast(count(1) as integer)
            FROM (
                SELECT w.dictWord.id AS wordId, SUM(dwf.freq) AS sm
                    FROM Word w
                    JOIN w.dictWord.forms dwf
                    WHERE w.userId = :userId
                    GROUP BY w.dictWord.id
                    HAVING SUM(dwf.freq) < :highBound
                        AND SUM(dwf.freq) >= :lowBound
                UNION
                SELECT udw.lemma.id AS wordId, SUM(dwf.freq) AS sm
                    FROM UserDictWord udw
                    JOIN udw.lemma.forms dwf
                    WHERE udw.userId = :userId
                    GROUP BY udw.lemma.id
                    HAVING SUM(dwf.freq) < :highBound
                        AND SUM(dwf.freq) >= :lowBound
            ) stat
        """)
	Integer getUserDictStats(@Param("lowBound") long lowBound,
	                         @Param("highBound") long highBound,
	                         @Param("userId") Long userId);
}
