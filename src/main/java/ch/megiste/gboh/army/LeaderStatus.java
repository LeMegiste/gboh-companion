package ch.megiste.gboh.army;

import java.io.Serializable;

public class LeaderStatus implements Serializable {
	public boolean finished=false;
	public int nbActivations=0;
	public int nbOrdersGiven=0;
	public boolean eliteUsed;
	public boolean didTrump = false;
	public boolean present = true;
}