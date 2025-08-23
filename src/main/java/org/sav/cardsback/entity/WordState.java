package org.sav.cardsback.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
public class WordState {

	public WordState(Integer id) {
		this.id = id;
	}

	@Id
	Integer id;

	@Column
	String name;
}