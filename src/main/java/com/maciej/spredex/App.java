package com.maciej.spredex;

import java.io.FileNotFoundException;
import java.util.List;

import com.maciej.spredex.Gui.SpredexGui;
import com.maciej.spredex.Sheet.Sheet;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
		int maxRows = 100000;
		int maxColumns = 10000;
		Sheet sheet = new Sheet(maxRows, maxColumns);

		if (args.length > 1) {
			System.out.println("Invalid number of arguments, provide one or zero.");
			return;
		}
		else if (args.length == 1) {
			try {
				List<List<String>> csv = CsvFile.parseCsvFile(args[0]);
				sheet.loadTable(csv);
			}
			catch (FileNotFoundException e) {
				System.out.println("Could not process " + args[0] + ".");
				return;
			}
		}

		System.out.println("Hello from spredex hq!");
		SpredexGui gui = new SpredexGui(sheet);
		gui.on();
    }
}
