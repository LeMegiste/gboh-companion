package ch.megiste.gboh.command;

public class Modifiers {
	public static Modifier<Boolean> boolMod(ModifierDefinition def){
		return new Modifier<>(def,true);
	}
	public static Modifier<Integer> intMod(ModifierDefinition def, Integer i){
		return new Modifier<>(def,i);
	}
}
