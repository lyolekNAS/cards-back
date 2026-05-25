package org.sav.cardsback.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sav.cardsback.domain.dictionary.repository.StateLimitRepository;
import org.sav.cardsback.domain.dictionary.service.StateLimitService;
import org.sav.cardsback.dto.StateLimitDto;
import org.sav.cardsback.dto.WordStateDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StateLimitServiceTest {

	@Mock
	private StateLimitRepository stateLimitRepository;

	@InjectMocks
	private StateLimitService stateLimitService;

	@Test
	void findAll_returnsDtosFromRepository() {
		StateLimitDto dto = StateLimitDto.builder()
				.state(WordStateDto.STAGE_1)
				.attempt(10)
				.delay(30)
				.color("#fff")
				.build();
		when(stateLimitRepository.findAllStateLimitDtos()).thenReturn(List.of(dto));

		List<StateLimitDto> result = stateLimitService.findAll();

		assertEquals(1, result.size());
		assertEquals(dto, result.getFirst());
		verify(stateLimitRepository).findAllStateLimitDtos();
	}

	@Test
	void findById_whenExists_returnsDto() {
		StateLimitDto dto = StateLimitDto.builder()
				.state(WordStateDto.STAGE_2)
				.attempt(15)
				.delay(60)
				.color("#000")
				.build();
		when(stateLimitRepository.findByStateId(2)).thenReturn(Optional.of(dto));

		StateLimitDto result = stateLimitService.findById(2);

		assertEquals(dto, result);
		verify(stateLimitRepository).findByStateId(2);
	}

	@Test
	void findById_whenAbsent_throwsNoSuchElementException() {
		when(stateLimitRepository.findByStateId(99)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> stateLimitService.findById(99));
		verify(stateLimitRepository).findByStateId(99);
	}
}
