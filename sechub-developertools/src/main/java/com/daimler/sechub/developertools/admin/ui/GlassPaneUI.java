// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GlassPaneUI {

	private MyGlassPane glassPane;

	public GlassPaneUI(UIContext context, JFrame frame) {
		this.glassPane = new MyGlassPane();
		frame.setGlassPane(glassPane);
	}

	public void block(boolean block) {
		SwingUtilities.invokeLater(()-> handleBlock(block));
	}

	private Object handleBlock(boolean block) {
		glassPane.setVisible(block);
		return null;
	}

	private class MyGlassPane extends JPanel{

		private static final long serialVersionUID = 1L;

		public MyGlassPane() {
			Color color = new Color(0.0f, 0.0f, 0.0f, 0.5f);
			setBackground(color);
			setVisible(false);
		}

	}
}
