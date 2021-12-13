package ch.megiste.gboh.command;

public class Modifier<T> {
	private final ModifierDefinition definition;
	private final T value ;

	public Modifier(final ModifierDefinition definition, final T value) {
		this.definition = definition;
		this.value = value;
	}

	public ModifierDefinition getDefinition() {
		return definition;
	}

	public T getValue() {
		return value;
	}
}
