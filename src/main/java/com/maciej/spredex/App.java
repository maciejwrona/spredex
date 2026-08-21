package com.maciej.spredex;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.maciej.spredex.Cell.CellLocation;
import com.maciej.spredex.Sheet.MatrixSheet;
import com.maciej.spredex.Sheet.Sheet;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
		JFrame frame = new JFrame("Excel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		Sheet sheet = new MatrixSheet(26);
		JTable table = new JTable(sheet);

		Font cellFont = new Font("SansSerif", Font.PLAIN, 20);
		Font headerFont = new Font("SansSerif", Font.BOLD, 22);

		table.setFont(cellFont);
		table.setRowHeight(45);
		table.getTableHeader().setFont(headerFont);
		table.getTableHeader().setPreferredSize(new Dimension(0, 50));

		JTextField editorField = new JTextField();
		editorField.setFont(cellFont);
		DefaultCellEditor singleClickEditor = new DefaultCellEditor(editorField);
		singleClickEditor.setClickCountToStart(1);
		table.setDefaultEditor(Object.class, singleClickEditor);

		frame.add(new JScrollPane(table), BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
    }
}
