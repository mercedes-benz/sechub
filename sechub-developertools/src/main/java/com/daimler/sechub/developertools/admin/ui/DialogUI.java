// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;

public class DialogUI {

	private JFrame frame;
	private JFileChooser fileChooser = new JFileChooser();

	public DialogUI(JFrame frame) {
		this.frame = frame;
	}

	public boolean confirm(String message) {
		int x = JOptionPane.showConfirmDialog(frame, message, "Please confirm", JOptionPane.OK_OPTION);
		return x == JOptionPane.OK_OPTION;
	}

	public void warn(String message) {
		JOptionPane.showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Selects file by file chooser
	 * @return file or <code>null</code>
	 */
	public File selectFile(String initialPath) {
		if (initialPath!=null && new File(initialPath).exists()) {
			fileChooser.setCurrentDirectory(new File(initialPath));
		}
		int result = fileChooser.showOpenDialog(frame);
		if (result!=JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return fileChooser.getSelectedFile();

	}

	/**
	 * Shows an input dialog for user. Last entered values for given idenifier will
	 * be shown
	 *
	 * @param message
	 * @param identifier
	 * @return
	 */
	public Optional<String> getUserInput(String message, String defaultValue) {
		return Optional.ofNullable(JOptionPane.showInputDialog(frame, message, defaultValue));
	}

	public List<String> editList(String title, List<String> list){
		SimpleTextDialog dialog = new SimpleTextDialog(title);
		StringBuilder sb= new StringBuilder();
		for (String part: list) {
			sb.append(part);
			sb.append("\n");
		}
		dialog.setText(sb.toString());
		dialog.setToolTip("Each line represents a list entry!");
		dialog.setVisible(true);

		if (! dialog.isOkPresssed()) {
			return null;/*NOSONAR*/
		}

		String[] splittedLines = dialog.getText().split("\n");

		List<String> result = new ArrayList<>();
		for (String line: splittedLines) {
			if (line==null) {
				continue;
			}
			String v = line.trim();
			if (! v.isEmpty()) {
				result.add(v);
			}
		}
		return result;
	}

	private class SimpleTextDialog /*NOSONAR*/extends JDialog{

		private static final long serialVersionUID = 1L;
		private JTextArea textArea;
		private JButton okButon;
		private boolean okPresssed;

		SimpleTextDialog(String title){
			super(frame,title,true);
			setLayout(new BorderLayout());
			this.textArea = new JTextArea();
			this.textArea.setPreferredSize(new Dimension(500,200));
			JPopupMenu popup = new JPopupMenu();
			textArea.setComponentPopupMenu(popup);
			ActionSupport support = new ActionSupport();
			support.apply(popup, support.createDefaultCutCopyAndPastActions());

			add(new JScrollPane(textArea),BorderLayout.CENTER);

			this.okButon=new JButton("OK");
			this.okButon.addActionListener(this::okPressed);
			JPanel lowerPanel = new JPanel();
			lowerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			lowerPanel.add(okButon);
			add(lowerPanel,BorderLayout.SOUTH);

			pack();
			setLocationRelativeTo(frame);

		}

		public void setToolTip(String text) {
			textArea.setToolTipText(text);
		}

		public boolean isOkPresssed() {
			return okPresssed;
		}

		public void setText(String text) {
			this.textArea.setText(text);
		}

		public String getText() {
			return textArea.getText();
		}
		private Object okPressed(ActionEvent x) {
			okPresssed=true;
			setVisible(false);
			return null;
		}
	}


}
