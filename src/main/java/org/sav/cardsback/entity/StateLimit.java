package org.sav.cardsback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StateLimit {


	@Id
	@Column(name="state_id")
	Integer stateId;

	Integer attempt;

	Integer delay;

	String color;

}
