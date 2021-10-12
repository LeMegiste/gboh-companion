package ch.megiste.gboh.command.unit;

public class TQCheck1 extends AbstractTqCheck {
	public TQCheck1() {
		super("Runs a TQ check and inflicts hits above the TQ, with a minimum of 1");
		minHits = 1;
	}

	@Override
	public String getKey() {
		return "TQ1";
	}

}
