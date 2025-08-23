package org.sav.cardsback.mapper;

import org.mapstruct.Mapper;
import org.sav.cardsback.entity.Word;
import org.sav.fornas.dto.cards.WordDto;

import java.util.List;

@Mapper(componentModel = "spring", uses = {WordStateMapper.class})
public interface WordMapper {

//	@Mapping(target = "stateId", source = "state")
	WordDto toDto(Word word);

	List<WordDto> toDtoList(List<Word> words);

//	@Mapping(target = "state", source = "stateId")
	Word toEntity(WordDto wordDto);
}
