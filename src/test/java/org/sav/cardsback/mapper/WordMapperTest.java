package org.sav.cardsback.mapper;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.entity.Word;
import org.sav.cardsback.entity.WordState;
import org.sav.fornas.dto.cards.WordDto;
import org.sav.fornas.dto.cards.WordStateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WordMapperTest {

    @Autowired
    private WordMapper mapper;

    @Test
    void toDto_mapsEntityToDto() {
        Word word = new Word();
        word.setId(1L);
        word.setEnglish("hello");
        word.setUkrainian("привіт");
        word.setUserId(42L);
        word.setState(new WordState(WordStateDto.STAGE_1.getValue()));

        WordDto dto = mapper.toDto(word);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("hello", dto.getEnglish());
        assertEquals("привіт", dto.getUkrainian());
        assertEquals(WordStateDto.STAGE_1, dto.getState());
    }

    @Test
    void toEntity_mapsDtoToEntity() {
        WordDto dto = new WordDto();
        dto.setId(2L);
        dto.setEnglish("world");
        dto.setUkrainian("світ");
        dto.setState(WordStateDto.STAGE_1);

        Word word = mapper.toEntity(dto);

        assertNotNull(word);
        assertEquals(2L, word.getId());
        assertEquals("world", word.getEnglish());
        assertEquals("світ", word.getUkrainian());
        assertNotNull(word.getState());
        assertEquals(WordStateDto.STAGE_1.getValue(), word.getState().getId());
    }

    @Test
    void toDtoList_mapsList() {
        Word word1 = new Word();
        Word word2 = new Word();
        word1.setEnglish("hi");
        word2.setEnglish("bye");

        List<WordDto> dtos = mapper.toDtoList(List.of(word1, word2));

        assertEquals(2, dtos.size());
        assertEquals("hi", dtos.get(0).getEnglish());
        assertEquals("bye", dtos.get(1).getEnglish());
    }
}
