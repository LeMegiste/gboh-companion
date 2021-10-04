package ch.megiste.gboh.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.util.Strings;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public class Helper {

	private final static TreeMap<Integer, String> map = new TreeMap<>();

	static {

		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");

	}

	public final static String toRoman(int number) {
		int l = map.floorKey(number);
		if (number == l) {
			return map.get(number);
		}
		return map.get(l) + toRoman(number - l);
	}

	public static <T extends Enum<T>> T readEnum(String strValue, Class<T> clazz, T defaultValue) {
		if (strValue != null && strValue.length() > 0) {
			return Enum.valueOf(clazz, strValue);
		}

		return defaultValue;
	}

	public static <T extends Serializable> T clone(final T source) {
		Preconditions.checkNotNull(source);
		return SerializationUtils.clone(source);
	}

	public static Properties loadProperties() {
		Properties p = new Properties();
		try (FileReader fr = new FileReader("gboh.properties")) {
			p.load(fr);
			return p;
		} catch (IOException e) {
			throw new GbohError(e);
		}

	}

	public static void storeProperties(final Properties generalProperties) {

		try (FileWriter fw = new FileWriter("gboh.properties")) {
			generalProperties.store(fw, "" + new Date());
		} catch (IOException e) {
			throw new GbohError(e);
		}

	}

	// get a file from the resources folder
	// works everywhere, IDEA, unit test and JAR file.
	public InputStream getFileFromResourceAsStream(String fileName) {

		// The class loader that loaded the class
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		// the stream holding the file content
		if (inputStream == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {
			return inputStream;
		}

	}

	public List<CSVRecord> getCsvFromResource(String fileName) {
		try (InputStream is = getFileFromResourceAsStream(fileName); InputStreamReader isr = new InputStreamReader(
				is)) {
			CSVFormat format = CSVFormat.newFormat('\t').withFirstRecordAsHeader();

			final List<CSVRecord> records = format.parse(isr).getRecords();
			return records;

		} catch (IOException e) {
			throw new GbohError(e);
		}

	}

	public static String buildModifiersLog(String prefix, List<String> modifiersWithExplanations) {
		if (modifiersWithExplanations.size() == 0) {
			return "";
		} else {
			List<String> allText = new ArrayList<>(modifiersWithExplanations);
			if (!Strings.isEmpty(prefix)) {
				allText.add(0, prefix);
			}
			return " (" + Joiner.on(", ").join(allText) + ")";
		}

	}
}
