package com.maciej.spredex;

import com.maciej.spredex.Gui.SpredexGui;
import com.maciej.spredex.Sheet.Sheet;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
		System.out.println("Hello from spredex hq!");

		int maxRows = 100000;
		int maxColumns = 10000;

		Sheet sheet = new Sheet(maxRows, maxColumns);
		SpredexGui gui = new SpredexGui(sheet);
		gui.on();
    }
}
