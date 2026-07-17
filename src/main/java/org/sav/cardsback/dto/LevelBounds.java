package org.sav.cardsback.dto;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
public enum LevelBounds {
	FIRST(27_200_000),
	SECOND(7_300_000),
	THIRD(2_400_000),
	FOURTH(800_000),
	FIFTH(0);
	private long bound;
	LevelBounds(long bound) {
		this.bound = bound;
	}

	public static void setBounds(long first, long second, long third, long fourth, long fifth) {
		FIRST.bound = first;
		SECOND.bound = second;
		THIRD.bound = third;
		FOURTH.bound = fourth;
		FIFTH.bound = fifth;
	}
}


@Component
@Getter
 class LevelBoundsConfig {

	@Value("${app-props.level-bounds.first}")
	private long first;

	@Value("${app-props.level-bounds.second}")
	private long second;

	@Value("${app-props.level-bounds.third}")
	private long third;

	@Value("${app-props.level-bounds.fourth}")
	private long fourth;

	@Value("${app-props.level-bounds.fifth}")
	private long fifth;

	@PostConstruct
	private void init() {
		LevelBounds.setBounds(first, second, third, fourth, fifth);
	}
}
