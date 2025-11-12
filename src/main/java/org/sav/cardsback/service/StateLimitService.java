package org.sav.cardsback.service;

import lombok.RequiredArgsConstructor;
import org.sav.cardsback.dto.StateLimitDto;
import org.sav.cardsback.repository.StateLimitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StateLimitService {

    private final StateLimitRepository stateLimitRepository;

    public List<StateLimitDto> findAll() {
        return stateLimitRepository.findAllStateLimitDtos();
    }

    public StateLimitDto findById(Integer id) {
        return stateLimitRepository.findByStateId(id).orElseThrow();
    }
}