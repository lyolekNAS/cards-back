package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.DictTrans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DictTransRepository extends JpaRepository<DictTrans, Long> {

	@Modifying
	@Query("delete from DictTrans dt where dt.lemma.id = :lemmaId")
	void deleteByLemmaId(@Param("lemmaId") Long lemmaId);
}
