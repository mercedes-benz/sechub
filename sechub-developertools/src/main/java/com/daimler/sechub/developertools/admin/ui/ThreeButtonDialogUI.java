// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * The ThreeButtonDialogUI class provides a JDialog with three buttons and one
 * input field.
 */
public class ThreeButtonDialogUI extends JDialog {

	private static final long serialVersionUID = -160421934785613918L;

	private JLabel inputLabel;
	private JTextField inputField;
	private JButton cancel;
	private JButton add;
	private JButton finish;

	private boolean cancelPressed;
	private boolean addPressed;
	private boolean finishPresssed;

	/**
	 * Creates a new dialog with three buttons and one input field.
	 * 
	 * @param parentFrame    the Frame from which the dialog is displayed
	 * @param title          the <code>String</code> to display in the dialog's
	 *                       title bar
	 * @param inputLabelText the text which is displayed as label for the input text
	 *                       field
	 */
	public ThreeButtonDialogUI(JFrame parentFrame, String title, String inputLabelText) {
		super(parentFrame, title, true);

		JPanel textPanel = new JPanel(new BorderLayout());
		inputLabel = new JLabel(inputLabelText);
		inputField = new JTextField();
		textPanel.add(inputLabel, BorderLayout.NORTH);
		textPanel.add(inputField, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		cancel = new JButton("Cancel");
		cancel.addActionListener(this::cancelPressed);
		finish = new JButton("Finish");
		finish.addActionListener(this::finishPressed);
		add = new JButton("Add");
		add.addActionListener(this::addPressed);

		buttonPanel.add(cancel);
		buttonPanel.add(finish);
		buttonPanel.add(add);

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(20, 20, 20, 20));
		content.add(textPanel, BorderLayout.NORTH);
		content.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(content);

		// select the add button as default button
		JRootPane rootPane = getRootPane();
		rootPane.setDefaultButton(add);

		pack();
		setLocationRelativeTo(parentFrame);
		inputField.requestFocusInWindow();
	}

	/**
	 * Returns the user input of the input text field or <code>null</code>
	 * 
	 * @return text or <code>null</code>
	 */
	public String getText() {
		String inputValue = inputField.getText();

		if (inputValue.isEmpty()) {
			inputValue = null;
		}

		return inputValue;
	}

	private Object finishPressed(ActionEvent x) {
		finishPresssed = true;
		setVisible(false);
		return null;
	}

	private Object cancelPressed(ActionEvent x) {
		cancelPressed = true;
		setVisible(false);
		return null;
	}

	private Object addPressed(ActionEvent x) {
		addPressed = true;
		setVisible(false);
		return null;
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

	public boolean isAddPressed() {
		return addPressed;
	}

	public boolean isFinishPresssed() {
		return finishPresssed;
	}
}