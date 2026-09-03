package com.maciej.spredex.Gui;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.maciej.spredex.Sheet.Sheet;

public class SpredexGui extends JFrame {
	private final static int rowHeight = 50; 
	private final static int columnWidth = 200;
	private final static Font bigFont = new Font("Arial", Font.PLAIN, 26);
	private final Sheet sheet;

	public SpredexGui(Sheet sheet) {
		super("spredex");
		this.sheet = sheet;
		this.setExtendedState(MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		JTable table = initializeTable(sheet);

		this.add(new JScrollPane(table));
	}

	public void on() {
		this.setVisible(true);
	}

	private JTable initializeTable(Sheet sheet) {
		JTable table = new Antialiased.Table(sheet);
		JTableHeader header = new Antialiased.TableHeader(table.getColumnModel());

		table.setTableHeader(header);
		table.setDefaultEditor(Object.class, new FormulaEditor(sheet, bigFont));

		// Size
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(rowHeight);
		for (int i = 0; i < sheet.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(columnWidth);
		}

		// Font
		table.setFont(bigFont);
		table.getTableHeader().setFont(bigFont);

		table.setRowSelectionAllowed(false);

		return table;
	}
}
