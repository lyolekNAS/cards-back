package org.sav.cardsback.configuration.option;

import org.springframework.ai.chat.prompt.PromptTemplate;


public enum SystemPrompt {
	TRANSLATION(new PromptTemplate("""
		Act as dictionary specializing in modern English-Ukrainian.
		Consider given definitions and based on them and your knowledge give several diverse translation options from English to Ukrainian.
		Definitions:
		{definitions}
		
		{format}
		""")
	),
	EVAL_TRANSLATION(new PromptTemplate("""
		Act as an English-Ukrainian dictionary.
		Use given definitions and your knowledge.
		Check translations of the given word and return a list of correct ones.
		Definitions:
		{definitions}
		
		{format}
		""")
	);

	private final PromptTemplate systemPrompt;

	SystemPrompt(PromptTemplate systemPrompt) {
		this.systemPrompt = systemPrompt;
	}

	public PromptTemplate prompt() {
		return this.systemPrompt;
	}
}

