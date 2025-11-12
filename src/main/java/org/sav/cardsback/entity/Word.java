package org.sav.cardsback.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Setter
@Getter
@ToString
public class Word {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;

	String english;

	String ukrainian;

	@Column
	String description;

	@Column
	Long userId;

	@Column
	Integer englishCnt;

	@Column
	Integer ukrainianCnt;

	@Column
	OffsetDateTime lastTrain;

	@Column
	OffsetDateTime nextTrain;

	@ManyToOne
	@JoinColumn(name = "state_id")
	WordState state;
}
