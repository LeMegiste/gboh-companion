package ch.megiste.gboh.command.unit;

public class Add1Hit extends Hit {

	public Add1Hit() {
		super(1);
	}

	@Override
	public String getKey() {
		return "+";
	}

}
