package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.DictWordForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DictWordFormRepository extends JpaRepository<DictWordForm, Long> {
	Optional<DictWordForm> findByWordText(String wordText);



	@Query("""
        SELECT dwf
            FROM DictWordForm dwf
            WHERE dwf.wordText = :wordText
        """)
	Optional<DictWordForm> getFormByWordText(String wordText);
}
