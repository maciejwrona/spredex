package com.maciej.spredex.Gui;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.maciej.spredex.Sheet.Sheet;

public class SpredexGui extends JFrame {
	private final static int rowHeight = 50; 
	private final static int columnWidth = 200;
	private final static int rowNumbersWidth = 80;
	private final static Font bigFont = new Font("Arial", Font.PLAIN, 26);

	public SpredexGui(Sheet sheet) {
		super("spredex");
		this.setExtendedState(MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		JTable table = initializeTable(sheet);
		JTable rowNumbers = getRowNumbers(table);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowNumbers);

		this.add(scrollPane);
	}

	public void on() {
		this.setVisible(true);
	}

	private JTable initializeTable(Sheet sheet) {
		JTable table = new Antialiased.Table(sheet);
		JTableHeader header = new Antialiased.TableHeader(table.getColumnModel());

		table.setTableHeader(header);
		table.setDefaultEditor(Object.class, new FormulaEditor(sheet, bigFont));
		table.setDefaultRenderer(Object.class, new ValueRenderer(sheet));

		// Size
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(rowHeight);
		for (int i = 0; i < sheet.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(columnWidth);
		}

		// Enable selection
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		// Font
		table.setFont(bigFont);
		table.getTableHeader().setFont(bigFont);

		return table;
	}

	private JTable getRowNumbers(JTable table) {
		DefaultTableModel numberedRowsModel = new DefaultTableModel(table.getRowCount(), 1) {
			@Override
			public Object getValueAt(int row, int column) {
				return row + 1;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable rowNumbers = new Antialiased.Table(numberedRowsModel);

		// Match table header styling
		rowNumbers.setFont(bigFont);
		rowNumbers.setRowHeight(table.getRowHeight());
		rowNumbers.setBackground(table.getTableHeader().getBackground());
		rowNumbers.setForeground(table.getTableHeader().getForeground());

		// Turn off any selection
		rowNumbers.setRowSelectionAllowed(false);
		rowNumbers.setCellSelectionEnabled(false);
		rowNumbers.setFocusable(false);

		// Center align
		rowNumbers.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(SwingConstants.CENTER);

		rowNumbers.setPreferredScrollableViewportSize(new Dimension(rowNumbersWidth, 0));
		rowNumbers.getColumnModel().getColumn(0).setCellRenderer(center);
		rowNumbers.getColumnModel().getColumn(0).setPreferredWidth(rowNumbersWidth);

		return rowNumbers;
	}
}
