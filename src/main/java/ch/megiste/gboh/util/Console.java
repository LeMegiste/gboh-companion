package ch.megiste.gboh.util;

public interface Console {

	void logNL(String str);

	String readLine(String prompt);

	void logFormat(String s,Object... params);
}
