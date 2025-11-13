package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.DictWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictWord, Long> {
	Optional<DictWord> findByWordText(String text);


	@Modifying
	@Transactional
	@Query("DELETE FROM DictWord w WHERE w.wordText = :wordText")
	void deleteByWordText(String wordText);
}
