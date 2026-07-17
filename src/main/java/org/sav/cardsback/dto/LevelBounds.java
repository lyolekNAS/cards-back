package org.sav.cardsback.dto;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
public enum LevelBounds {
	ZERO(Long.MAX_VALUE, 0),
	FIRST(27_200_000, 1),
	SECOND(7_300_000, 2),
	THIRD(2_400_000, 3),
	FOURTH(800_000, 4),
	FIFTH(0, 5);
	private long bound;
	private final int level;
	LevelBounds(long bound,  int level) {
		this.bound = bound;
		this.level = level;
	}

	public static void setBounds(long first, long second, long third, long fourth, long fifth) {
		FIRST.bound = first;
		SECOND.bound = second;
		THIRD.bound = third;
		FOURTH.bound = fourth;
		FIFTH.bound = fifth;
	}

	public static LevelBounds  getBounds(int level){
		return switch (level) {
			case 0 -> ZERO;
			case 1 -> FIRST;
			case 2 -> SECOND;
			case 3 -> THIRD;
			case 4 -> FOURTH;
			case 5 -> FIFTH;
			default -> throw  new IllegalArgumentException("Level bounds out of range");
		};
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
