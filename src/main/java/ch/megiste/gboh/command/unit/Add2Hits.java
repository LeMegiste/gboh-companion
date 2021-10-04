package ch.megiste.gboh.command.unit;

public class Add2Hits extends Hit {

	public Add2Hits() {
		super(2);
	}

	@Override
	public String getKey() {
		return "++";
	}

}
