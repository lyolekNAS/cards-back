package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.DictWord;
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
    select case when count(udw) > 0 or count(w) > 0 then true else false end
        from UserDictWord udw
        left join Word w
            on w.userId = :userId
            and w.dictWord.id = :dictWordId
        where udw.userId = :userId
            and udw.lemma.id = :dictWordId
    """)
	boolean existsByUserAndDictWord(@Param("userId") Long userId, @Param("dictWordId") Long dictWordId);

	@Query(value = """
        SELECT *
            FROM dict_word dw
            WHERE (dw.state & :state) = 0
            ORDER BY RAND()
            LIMIT 1
        """, nativeQuery = true)
	DictWord findWordToProcess(@Param("state") Integer state);
}
