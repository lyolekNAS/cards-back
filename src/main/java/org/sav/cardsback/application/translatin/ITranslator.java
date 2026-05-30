package org.sav.cardsback.application.translatin;

import org.sav.cardsback.entity.DictWord;

import java.util.List;

public interface ITranslator {

	List<String> processWord(DictWord dictWord);
}
