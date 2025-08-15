package org.sav.cards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
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