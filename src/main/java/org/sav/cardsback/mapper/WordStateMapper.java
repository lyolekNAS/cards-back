package org.sav.cardsback.mapper;

import org.mapstruct.Mapper;
import org.sav.cardsback.entity.WordState;
import org.sav.fornas.dto.cards.WordStateDto;

@Mapper(componentModel = "spring")
public interface WordStateMapper {

	default WordStateDto toDto(WordState entity) {
		if (entity == null) return null;
		for (WordStateDto dto : WordStateDto.values()) {
			if (dto.getValue() == entity.getId()) {
				return dto;
			}
		}
		return null;
	}

	default WordState toEntity(WordStateDto dto) {
		if (dto == null) return null;
		return new WordState(dto.getValue());
	}

}
