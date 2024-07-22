// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.admin.ui.action.ActionSupport;

public class DialogUI {

    private static final Logger LOG = LoggerFactory.getLogger(DialogUI.class);
    private JFileChooser fileChooser = new JFileChooser();
    private JFrame frame;

    public DialogUI(JFrame frame) {
        this.frame = frame;
    }

    public boolean confirm(String message) {
        int x = JOptionPane.showConfirmDialog(frame, message, "Please confirm", JOptionPane.OK_OPTION);
        return x == JOptionPane.OK_OPTION;
    }

    public void inform(String message) {
        JOptionPane.showMessageDialog(frame, message, "Warning", JOptionPane.INFORMATION_MESSAGE);

    }

    public void warn(String message) {
        JOptionPane.showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /*
     * Selects file by file chooser
     *
     * @param initialPath the initial file path or <code>null</code>
     *
     * @return file or <code>null</code>
     */
    public File selectFile(String initialPath) {
        return selectFile(initialPath, null);
    }

    /**
     * Selects file by file chooser
     *
     * @param initialPath the initial file path or <code>null</code>
     * @param fileFilter  a file filter to use, can be <code>null</code>
     *
     * @return file or <code>null</code>
     */
    public File selectFile(String initialPath, javax.swing.filechooser.FileFilter fileFilter) {
        fileChooser.setFileFilter(fileFilter);
        if (initialPath != null) {
            File file = new File(initialPath);

            if (file.exists()) {
                if (file.isDirectory()) {
                    fileChooser.setCurrentDirectory(file);
                } else {
                    fileChooser.setSelectedFile(file);
                }
            } else {
                File parent = file.getParentFile();
                if (parent != null && parent.exists()) {
                    fileChooser.setCurrentDirectory(parent);
                    fileChooser.setSelectedFile(file);
                }
            }
        }
        DialogState state = new DialogState();
        try {

            if (SwingUtilities.isEventDispatchThread()) {
                /* we are already inside EDT */
                state.result = fileChooser.showOpenDialog(frame);
            } else {
                /*
                 * outside EDT, so ensure action is executed inside EDT and blocks until result
                 * available
                 */
                SwingUtilities.invokeAndWait(() -> {
                    state.result = fileChooser.showOpenDialog(frame);
                });
            }
        } catch (InvocationTargetException | InterruptedException e) {
            LOG.error("Filechooser selection failed", e);
        }
        if (state.result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();

    }

    private class DialogState {
        int result;
    }

    /**
     * Shows an input dialog for user. Last entered values for given identifier will
     * be shown
     *
     * @param message
     * @param identifier
     * @return
     */
    public Optional<String> getUserInput(String message, String defaultValue) {
        return Optional.ofNullable(JOptionPane.showInputDialog(frame, message, defaultValue));
    }

    /**
     * Shows an input dialog for user with a combo box
     *
     * @param message
     * @param identifier
     * @return
     */
    public <T> Optional<T> getUserInputFromCombobox(String message, String title, List<T> comboboxValues, T initialValue) {
        ComboxSelectionDialogUI<T> dialog = new ComboxSelectionDialogUI<>(frame, title, message, comboboxValues, initialValue);
        dialog.showDialog();

        if (!dialog.isOkPressed()) {
            return Optional.empty();
        }
        return Optional.of(dialog.getSelectionFromCombobox());

    }

    /**
     * Shows an input dialog for user. Last entered values for given identifier will
     * be shown
     *
     * @param message
     * @param identifier
     * @return
     */
    public Optional<String> getUserPassword(String message, String defaultValue) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a password:");
        JPasswordField passwordField = new JPasswordField(10);
        passwordField.setText(defaultValue);
        panel.add(label);
        panel.add(passwordField);
        String[] options = new String[] { "OK", "Cancel" };
        int option = JOptionPane.showOptionDialog(frame, panel, message, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        if (option == 0) {
            char[] password = passwordField.getPassword();
            return Optional.ofNullable(new String(password));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getUserInputFromTextArea(String title, String content) {
        SimpleTextDialog dialog = new SimpleTextDialog(title);
        dialog.setText(content);
        dialog.setToolTip("Use this text as multi line editor");
        dialog.setVisible(true);

        if (!dialog.isOkPressed()) {
            return Optional.empty();
        }
        return Optional.ofNullable(dialog.getText());
    }

    public ThreeButtonDialogResult<String> getUserInputFromField(String inputLabelText) {
        ThreeButtonDialogUI dialog = new ThreeButtonDialogUI(frame, "Input", inputLabelText);
        dialog.setVisible(true);

        ThreeButtonDialogResult<String> options = new ThreeButtonDialogResult<String>(dialog.isCancelPressed(), dialog.isAddPressed(),
                dialog.isFinishPresssed(), dialog.getText());

        return options;
    }

    public List<String> editList(String title, List<String> list) {
        SimpleTextDialog dialog = new SimpleTextDialog(title);
        StringBuilder sb = new StringBuilder();
        for (String part : list) {
            sb.append(part);
            sb.append("\n");
        }
        dialog.setText(sb.toString());
        dialog.setToolTip("Each line represents a list entry!");
        dialog.setVisible(true);

        if (!dialog.isOkPressed()) {
            return null;
        }

        String[] splittedLines = dialog.getText().split("\n");

        List<String> result = new ArrayList<>();
        for (String line : splittedLines) {
            if (line == null) {
                continue;
            }
            String v = line.trim();
            if (!v.isEmpty()) {
                result.add(v);
            }
        }
        return result;
    }

    public String editString(String title, String inputString) {
        SimpleTextDialog dialog = new SimpleTextDialog(title);

        dialog.setText(inputString);
        dialog.setToolTip("Each line represents a list entry!");
        dialog.setVisible(true);

        if (!dialog.isOkPressed()) {
            return null;
        }

        return dialog.getText();
    }

    private class SimpleTextDialog extends JDialog {

        private static final long serialVersionUID = 1L;
        private JTextArea textArea;
        private JButton okButton;
        private boolean okPresssed;

        SimpleTextDialog(String title) {
            super(frame, title, true);
            setLayout(new BorderLayout());
            this.textArea = new JTextArea();
            this.textArea.setPreferredSize(new Dimension(500, 200));
            JPopupMenu popup = new JPopupMenu();
            textArea.setComponentPopupMenu(popup);

            ActionSupport support = ActionSupport.getInstance();
            support.apply(popup, support.createDefaultCutCopyAndPastActions());

            add(new JScrollPane(textArea), BorderLayout.CENTER);

            this.okButton = new JButton("OK");
            this.okButton.addActionListener(this::okPressed);
            JPanel lowerPanel = new JPanel();
            lowerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            lowerPanel.add(okButton);
            add(lowerPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(frame);
        }

        public void setToolTip(String text) {
            textArea.setToolTipText(text);
        }

        public boolean isOkPressed() {
            return okPresssed;
        }

        public void setText(String text) {
            this.textArea.setText(text);
        }

        public String getText() {
            return textArea.getText();
        }

        private Object okPressed(ActionEvent x) {
            okPresssed = true;
            setVisible(false);
            return null;
        }
    }

}
