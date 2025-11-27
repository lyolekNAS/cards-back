package org.sav.cardsback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.Word;

import java.util.List;

@Mapper(componentModel = "spring", uses = {WordStateMapper.class})
public interface WordMapper {

	@Mapping(target = "dictWordId", source = "dictWord.id")
	WordDto toDto(Word entity);

	List<WordDto> toDtoList(List<Word> entities);

	@Mapping(target = "dictWord", source = "dictWordId")
	Word toEntity(WordDto dto);

	List<Word> toEntityList(List<WordDto> dtos);
	default DictWord mapDictWord(long id) {
		if (id == 0) return null; // Dto може передати 0, тому підстрахуємо
		DictWord d = new DictWord();
		d.setId(id);
		return d;
	}

}
