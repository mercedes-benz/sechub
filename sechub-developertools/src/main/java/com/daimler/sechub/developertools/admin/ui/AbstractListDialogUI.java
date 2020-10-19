// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import static com.daimler.sechub.developertools.admin.ui.DialogGridBagConstraintsFactory.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public abstract class AbstractListDialogUI<T> {

    private UIContext context;
    private JPanel mainPanel;
    private JButton buttonOk;

    private boolean okPressed = false;
    private JPanel buttonPanel;
    private String title;
    private DefaultTableModel model;
    private JTable table;
    private Object selectedValue;
    private JDialog dialog;
    private UserApprovesSelectionAction approveAction;
    private String okButtonText;

    public AbstractListDialogUI(UIContext context, String title) {
        this.context = context;
        this.title = title;
        this.okButtonText="Ok";
        this.approveAction= new UserApprovesSelectionAction();
    }

    protected UIContext getContext() {
        return context;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    protected abstract void initializeDataForShowDialog();

    public void showDialog() {
        initializeDataForShowDialog();

        dialog = new JDialog(context.getFrame());
        dialog.setLayout(new BorderLayout());

        createMainPanel();
        createButtonPanel(dialog);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(new Dimension(600, 600));
        dialog.revalidate();
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    private void createButtonPanel(JDialog dialog) {
        buttonPanel = new JPanel(new GridBagLayout());

        buttonOk = new JButton(approveAction);
        buttonPanel.add(buttonOk, createLabelConstraint(0));
    }

    private class UserApprovesSelectionAction extends AbstractAction{

        private static final long serialVersionUID = -1550027561125909572L;
        
        private UserApprovesSelectionAction() {
            putValue(Action.NAME, okButtonText);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            okPressed = true;
            int row = table.getSelectedRow();
            selectedValue = model.getValueAt(row, getSelectionColumn());

            dialog.setVisible(false);
            dialog.dispose();
            
        }
        
    }
    
    protected abstract int getSelectionColumn();

    protected void initializeMainPanel(JPanel mainPanel) {
        addTableToMainPanel(mainPanel);

    }

    protected void addTableToMainPanel(JPanel mainPanel) {
        int row = 0;
        model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String header : createTableHeaders()) {
            model.addColumn(header);
        }

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setPreferredSize(new Dimension(-1, 300));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()>1) {
                    approveAction.actionPerformed(null);
                }
            }
        });
        new TableRendersupport().addStandardTableCellRender(table);

        refillTableModel(model, createTableContent());

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        GridBagConstraints tableConstraint = createComponentConstraint(row++);
        tableConstraint.gridx = 0;
        tableConstraint.gridwidth++;
        tableConstraint.gridheight = 5;
        mainPanel.add(new JScrollPane(table), tableConstraint);
    }

    @SuppressWarnings("unchecked")
    public T getSelectedValue() {
        return (T) selectedValue;
    }
    
    public void setOkButtonText(String okButtonText) {
        this.okButtonText=okButtonText;
        approveAction.putValue(Action.NAME, okButtonText);
    }

    protected abstract List<Object[]> createTableContent();

    protected abstract List<String> createTableHeaders();

    private void createMainPanel() {
        mainPanel = new JPanel(new GridBagLayout());
        initializeMainPanel(mainPanel);
    }

    protected void refillTableModel(DefaultTableModel model, List<Object[]> data) {
        for (int row = 0; row < model.getRowCount(); row++) {
            model.removeRow(row);
        }
        /* name, enabled, uuid */
        for (Object[] rowData : data) {
            model.addRow(rowData);
        }
    }
    
   
}
