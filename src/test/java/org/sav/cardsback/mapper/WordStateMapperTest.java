
package org.sav.cardsback.mapper;

import org.junit.jupiter.api.Test;
import org.sav.cardsback.entity.WordState;
import org.sav.fornas.dto.cards.WordStateDto;

import static org.junit.jupiter.api.Assertions.*;

class WordStateMapperTest {

    private final WordStateMapper mapper = new WordStateMapperImpl();

    @Test
    void toDto_nullEntity_returnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_validEntity_returnsCorrectEnum() {
        WordState entity = new WordState(WordStateDto.PAUSED.getValue());
        WordStateDto dto = mapper.toDto(entity);

        assertEquals(WordStateDto.PAUSED, dto);
    }

    @Test
    void toEntity_nullDto_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_validDto_returnsCorrectEntity() {
        WordStateDto dto = WordStateDto.STAGE_1;
        WordState entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getValue(), entity.getId());
    }
}
