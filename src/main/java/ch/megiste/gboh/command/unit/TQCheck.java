package ch.megiste.gboh.command.unit;

public class TQCheck extends AbstractTqCheck {
	public TQCheck() {
		super("Runs a TQ check and inflicts hits above the TQ");
	}

	@Override
	public String getKey() {
		return "TQ";
	}

}
