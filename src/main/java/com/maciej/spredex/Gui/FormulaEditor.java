package com.maciej.spredex.Gui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.maciej.spredex.Sheet.Sheet;

public class FormulaEditor extends DefaultCellEditor {
	private final Sheet sheet;

	public FormulaEditor(Sheet sheet, Font font) {
		super(new Antialiased.TextField());

		this.sheet = sheet;

		JTextField field = (JTextField) getComponent();
		field.setFont(font);

		setClickCountToStart(2);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
												 boolean isSelected, int row, int column) {
		String formula = sheet.formulaAt(row, column);
		return super.getTableCellEditorComponent(table, formula, isSelected, row, column);
	}
}
