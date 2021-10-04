package ch.megiste.gboh;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TestCL {

	public static final String PROMPT = ">>";

	private static List<String> logs = Arrays.asList("man toto", "Zut!", "Hello !");

	private static PrintStream out;

	private static String prePrintedCommand;

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		out = System.out;
		while (true) {

			out.print(PROMPT);
			if(prePrintedCommand!=null){
				out.print(prePrintedCommand);
				prePrintedCommand=null;
				out.print(PROMPT);
			}
			String str = scanner.next();

			if (str.equals("exit")) {
				break;
			}
			if (str.equals("h")) {
				out.print("\r");
				prePrintedCommand=logs.get(0);

				continue;
			}

			out.println(str);
		}
	}
}
