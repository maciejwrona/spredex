package com.maciej.spredex.Gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.maciej.spredex.Sheet.Sheet;

public class ValueRenderer extends DefaultTableCellRenderer {
	private final Sheet sheet;

	public ValueRenderer(Sheet sheet) {
		this.sheet = sheet;
	}

	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, 
			boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		boolean toCenter = sheet.isErrorAt(row, column);
		if (toCenter) {
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		else {
			setHorizontalAlignment(SwingConstants.LEFT);
		}
		setToolTipText(sheet.getErrorMessageAt(row, column));	

		return this;
	}
}
