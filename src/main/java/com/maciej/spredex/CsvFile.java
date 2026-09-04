package com.maciej.spredex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvFile {

	public static List<List<String>> parseCsvFile(String path) throws FileNotFoundException {
		List<List<String>> result = new ArrayList<>();

		try (Scanner scanner = new Scanner(new File(path))) {
			while (scanner.hasNextLine()) {
				result.add(lineToRow(scanner.nextLine()));
			}
		}

		return result;
	}

	private static List<String> lineToRow(String line) {
		List<String> values = new ArrayList<>();

		try (Scanner lineScanner = new Scanner(line)) {
			lineScanner.useDelimiter(",");

			while (lineScanner.hasNext()) {
				values.add(lineScanner.next());
			}
		}

		return values;
	}
}
