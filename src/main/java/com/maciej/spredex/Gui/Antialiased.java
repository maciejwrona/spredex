package com.maciej.spredex.Gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class Antialiased {

	public static class Table extends JTable {
		public Table(TableModel model) {
			super(model);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			antialiasGraphics(graphics);
			super.paintComponent(graphics);
		}
	}

	public static class Label extends JLabel {
		public Label(String text) {
			super(text);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			antialiasGraphics(graphics);
			super.paintComponent(graphics);
		}
	}

	public static class TableHeader extends JTableHeader {
		public TableHeader(TableColumnModel model) {
			super(model);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			antialiasGraphics(graphics);
			super.paintComponent(graphics);
		}
	}

	public static class TextField extends JTextField {
		@Override
		protected void paintComponent(Graphics graphics) {
			antialiasGraphics(graphics);
			super.paintComponent(graphics);
		}
	}

	private static void antialiasGraphics(Graphics graphics) {
		Graphics2D graphics2d = (Graphics2D) graphics;
		
		// Enforce text antialiasing for the entire table paint cycle
		graphics2d.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING, 
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON
		);
		
		// Fixes character spacing
		graphics2d.setRenderingHint(
			RenderingHints.KEY_FRACTIONALMETRICS, 
			RenderingHints.VALUE_FRACTIONALMETRICS_ON
		);
	}
}
