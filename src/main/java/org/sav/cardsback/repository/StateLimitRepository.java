package org.sav.cardsback.repository;

import org.sav.cardsback.entity.StateLimit;
import org.sav.fornas.dto.cards.StateLimitDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StateLimitRepository extends JpaRepository<StateLimit, Integer> {

	@Query("SELECT new org.sav.fornas.dto.cards.StateLimitDto(s.stateId, s.attempt, s.delay, s.color) FROM StateLimit s")
	public List<StateLimitDto> findAllStateLimitDtos();

	@Query("SELECT new org.sav.fornas.dto.cards.StateLimitDto(s.stateId, s.attempt, s.delay, s.color) FROM StateLimit s WHERE s.stateId = :id")
	public Optional<StateLimitDto> findByStateId(@Param("id") Integer id);
}
