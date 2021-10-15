package ch.megiste.gboh.command.unit;

public class TQCheck01 extends AbstractTqCheck {
	public TQCheck01() {
		super("Runs a TQ check and inflicts 1 hit if missed");
		minHits = 0;
		maxHits = 1;
	}

	@Override
	public String getKey() {
		return "TQ01";
	}

}
