package org.sav.cardsback.domain.dictionary.repository;

import org.sav.cardsback.entity.UserDictWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDictWordRepository extends JpaRepository<UserDictWord, Long> {
	Optional<UserDictWord> findByUserIdAndLemma_Id(Long userId, Long wordId);
}

