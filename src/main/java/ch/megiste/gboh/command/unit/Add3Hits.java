package ch.megiste.gboh.command.unit;

public class Add3Hits extends Hit {

	public Add3Hits() {
		super(3);
	}

	@Override
	public String getKey() {
		return "+++";
	}

}
