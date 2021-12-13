package ch.megiste.gboh.command;

import com.sun.org.apache.xpath.internal.operations.Mod;

public class Modifiers {
	public static Modifier<Boolean> boolMod(ModifierDefinition def){
		return new Modifier<>(def,true);
	}
	public static Modifier<Integer> intMod(ModifierDefinition def, Integer i){
		return new Modifier<>(def,i);
	}
}
