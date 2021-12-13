package ch.megiste.gboh.army;

import java.util.Objects;

import com.google.common.collect.ComparisonChain;

import ch.megiste.gboh.command.leader.LogLeader;

public class Leader implements Comparable<Leader> {
	private  String code;
	private String name;
	private int initiative;
	private int range;
	private LeaderStatus status = new LeaderStatus();

	public Leader(final String code, final String name, final int initiative, final int range) {
		this.code = code;
		this.name = name;
		this.initiative = initiative;
		this.range = range;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getInitiative() {
		return initiative;
	}

	public int getRange() {
		return range;
	}

	@Override
	public int compareTo(final Leader other) {
		return ComparisonChain.start().compare(initiative,other.initiative).compare(code,other.code).result();

	}

	public LeaderStatus getStatus() {
		return status;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Leader)) {
			return false;
		}
		final Leader leader = (Leader) o;
		return initiative == leader.initiative && range == leader.range && Objects.equals(code, leader.code) && Objects
				.equals(name, leader.name) ;
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, name, initiative, range);
	}

	public boolean isFinished() {
		return status.finished;
	}

	public int getNbActivations(){
		return status.nbActivations;
	}

	@Override
	public String toString() {
		return LogLeader.logLeader(this);
	}

	public boolean didTrump() {
		return status.didTrump;
	}

	public boolean usedElite() {
		return status.eliteUsed;
	}

	public void setStatus(final LeaderStatus after) {
		this.status=after;
	}
}
