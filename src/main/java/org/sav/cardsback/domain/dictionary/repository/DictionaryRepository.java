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
            WHERE bitand(dw.state, :forbidden) = 0
                    AND bitand(dw.state, :required) = :required
            ORDER BY function('RAND')
        """)
	List<DictWord> findWordToProcessInternal(@Param("forbidden") Integer forbidden, @Param("required") Integer required, Pageable pageable);

	default Optional<DictWord> findWordToProcess(Integer forbidden, Integer required) {
		return findWordToProcessInternal(forbidden, required, PageRequest.of(0, 1))
				.stream()
				.findFirst();
	}

	@Query("""
        SELECT COUNT(dw)
            FROM DictWord dw
            WHERE bitand(dw.state, :forbidden) = 0
                    AND bitand(dw.state, :required) = :required
        """)
	long countWordsToProcess(@Param("forbidden") Integer forbidden, @Param("required") Integer required);
}
