package org.sav.cards.mapper;

import org.mapstruct.Mapper;
import org.sav.cards.entity.Word;
import org.sav.fornas.dto.cards.WordDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WordMapper {

	WordDto toDto(Word word);

	List<WordDto> toDtoList(List<Word> words);
	Word toEntity(WordDto wordDto);
}
