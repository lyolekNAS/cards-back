package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.DictWord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictWord, Long> {
	Optional<DictWord> findByWordText(String text);


	@Modifying
	@Transactional
	@Query("DELETE FROM DictWord w WHERE w.wordText = :wordText And w.state = 0")
	void deleteByWordText(String wordText);

	@Query("""
    select
        (exists (
            select 1 from UserDictWord udw
            where udw.userId = :userId
              and udw.lemma.id = :dictWordId
        )
        or
        exists (
            select 1 from Word w
            where w.userId = :userId
              and w.dictWord.id = :dictWordId
        ))
    """)
	boolean existsByUserAndDictWord(@Param("userId") Long userId, @Param("dictWordId") Long dictWordId);

	@Query("""
        SELECT dw
            FROM DictWord dw
            WHERE bitand(dw.state, cast(:forbidden as integer)) = 0
                    AND bitand(dw.state, cast(:required as integer)) = cast(:required as integer)
            ORDER BY function('RAND')
        """)
	List<DictWord> findWordToProcessInternal(@Param("forbidden") Integer forbidden, @Param("required") Integer required, Pageable pageable);

	default Optional<DictWord> findWordToProcess(Integer forbidden, Integer required) {
		return findWordToProcessInternal(forbidden, required, PageRequest.of(0, 1))
				.stream()
				.findFirst();
	}

	@Query("""
        SELECT dw
            FROM DictWord dw
            JOIN dw.forms dwf
            WHERE bitand(dw.state, 16) = 16
                AND NOT EXISTS (
                    SELECT 1
                        FROM Word w
                        WHERE w.userId = :userId
                            AND w.dictWord.id = dw.id
                )
                AND NOT EXISTS (
                    SELECT 1
                        FROM UserDictWord udw
                        WHERE udw.userId = :userId
                            AND udw.lemma.id = dw.id
                )
            GROUP BY dw.id, dw.wordText, dw.state
            HAVING SUM(dwf.freq) < :highBound
                AND SUM(dwf.freq) >= :lowBound
            ORDER BY function('RAND')
        """)
	List<DictWord> findWordToSuggestInternal(@Param("lowBound") long lowBound, @Param("highBound") long highBound, @Param("userId") long userId, Pageable pageable);



	default Optional<DictWord> findWordToSuggest(long lowBound, long highBound, long userId) {
		return findWordToSuggestInternal(lowBound, highBound, userId, PageRequest.of(0, 1))
				.stream()
				.findFirst();
	}

	@Query("""
        SELECT COUNT(dw)
            FROM DictWord dw
            WHERE bitand(dw.state, cast(:forbidden as integer)) = 0
                    AND bitand(dw.state, cast(:required as integer)) = cast(:required as integer)
        """)
	long countWordsToProcess(@Param("forbidden") Integer forbidden, @Param("required") Integer required);

	@Query("""
        SELECT cast(count(1) as integer)
            FROM (
                SELECT dw.wordText AS wordText, SUM(dwf.freq) AS sm
                    FROM DictWord dw
                    LEFT JOIN dw.forms dwf
                    WHERE bitand(dw.state, 16) = 16
                    GROUP BY dw.wordText
                    HAVING SUM(dwf.freq) < :highBound
                        AND SUM(dwf.freq) >= :lowBound
            ) stat
        """)
	Integer getDictStats(@Param("lowBound") long lowBound, @Param("highBound") long highBound);
}
