package org.sav.cardsback.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.sav.cardsback.dto.WordDto;
import org.sav.cardsback.entity.DictWord;
import org.sav.cardsback.entity.DictWordForm;
import org.sav.cardsback.entity.Word;

import java.util.List;

@Mapper(componentModel = "spring", uses = {WordStateMapper.class})
public interface WordMapper {

	@Mapping(target = "dictWordId", source = "dictWord.id")
	@Mapping(
			target = "dictWordFreqSum",
			expression = "java(sumDictWordFreq(entity.getDictWord()))"
	)
	@Mapping(
			target = "rarity",
			expression = "java(calcRarity(entity.getDictWord()))"
	)
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

	default long sumDictWordFreq(DictWord dictWord) {
		if (dictWord == null || dictWord.getForms() == null) {
			return 0L;
		}

		return dictWord.getForms()
				.stream()
				.mapToLong(DictWordForm::getFreq)
				.sum();
	}

	default int calcRarity(DictWord dictWord) {
		long f = sumDictWordFreq(dictWord);

		if (f > 30_000_000) return 1;
		if (f > 10_000_000) return 2;
		if (f > 4_000_000)  return 3;
		if (f > 1_500_000)  return 4;
		return 5;
	}

}
