package org.sav.cardsback.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

	@Column(length = 3000)
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

	@ManyToOne
	private DictWord dictWord;
}
