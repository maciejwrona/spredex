package com.maciej.spredex.Gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.maciej.spredex.CellLoc;
import com.maciej.spredex.Sheet.Sheet;

public class CellFiller extends MouseAdapter {
	private final JTable table;
	private final Sheet sheet;
	private final Font font;
	private CellLoc start;
	
	public CellFiller(JTable table, Sheet sheet) {
		this.table = table;
		this.sheet = sheet;
		this.font = table.getFont().deriveFont(Font.BOLD);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		start = pointToCellLoc(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		CellLoc end = pointToCellLoc(e.getPoint());
		
		if (start == null || 
			(start.row() == end.row() && start.column() == end.column())) {
			return;
		}

		if (confirm()) {
			Set<CellLoc> toFill = getCellsToFill(start, end);
			sheet.swipeCellToFillList(start, toFill);
		}
		else {
			table.clearSelection();
		}
	}

	private boolean confirm() {
		String confirmationMessage = 
			"Copy cell " + start + " into selected area?";
		JLabel body = new Antialiased.Label(confirmationMessage);
		body.setFont(font);

		int choice = JOptionPane.showConfirmDialog(
				null, body, "Confirm Fill", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		return choice == JOptionPane.YES_OPTION;
	}

	private Set<CellLoc> getCellsToFill(CellLoc start, CellLoc end) {
		Set<CellLoc> result = new HashSet<>();
		for (int i = Math.min(start.row(), end.row()); i <= Math.max(start.row(), end.row()); i++) {
			for (int j = Math.min(start.column(), end.column()); j <= Math.max(start.column(), end.column()); j++) {
				// Dont fill the starting cell
				if (i == start.row() && j == start.column()) {
					continue;
				}

				result.add(new CellLoc(i, j));
			}
		}

		return result;
	}

	private CellLoc pointToCellLoc(Point point) {
		return new CellLoc(
				table.rowAtPoint(point) + 1,
				table.columnAtPoint(point) + 1
				);
	}
}
