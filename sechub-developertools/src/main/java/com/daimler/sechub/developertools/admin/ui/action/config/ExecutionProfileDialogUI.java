// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import static com.daimler.sechub.developertools.admin.ui.DialogGridBagConstraintsFactory.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.daimler.sechub.commons.model.JSONConverter;
import com.daimler.sechub.developertools.admin.ui.TableRendersupport;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.FailsafeAction;
import com.daimler.sechub.test.executionprofile.TestExecutionProfile;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;

public class ExecutionProfileDialogUI {

    private UIContext context;
    private JPanel mainPanel;
    private TestExecutionProfile profile;
    private JTextField idTextField;
    private JTextArea descriptionTextArea;
    private JCheckBox enabledCheckBox;
    private JButton buttonOk;

    private boolean okPressed = false;
    private JPanel buttonPanel;
    private String title;
    private boolean idEditAllowed;
    private DefaultTableModel model;
    private JTable configurationTable;
    private JTextArea projectIdsTextArea;
    private EditConfigurationAction editConfigurationAction;
    private JLabel projectIdsLabel;
    private String buttonOkText;
    
    public ExecutionProfileDialogUI(UIContext context, String title) {
        this(context, title, true, createExampleProfile());
    }

    public ExecutionProfileDialogUI(UIContext context, String title, boolean idEditAllowed, TestExecutionProfile profile) {
        this.context = context;
        this.profile = profile;
        this.title = title;
        this.idEditAllowed = idEditAllowed;
        this.editConfigurationAction = new EditConfigurationAction(context);
        this.buttonOkText="Ok";
    }

    UIContext getContext() {
        return context;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public static TestExecutionProfile createExampleProfile() {
        TestExecutionProfile profile = new TestExecutionProfile();
        profile.description = "";
        return profile;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(context.getFrame());
        dialog.setLayout(new BorderLayout());

        createMainPanel();
        createButtonPanel(dialog);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(new Dimension(800, 800));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
    }
    
    public void setTextForOKButton(String text) {
        buttonOkText=text;
    }

    private void createButtonPanel(JDialog dialog) {
        buttonPanel = new JPanel(new GridBagLayout());

        buttonOk = new JButton(buttonOkText);
        buttonOk.addActionListener((event) -> {
            String profileId = fetchProfileIdFromUI();
            if (profileId==null || profileId.isEmpty()) {
                idTextField.setBorder(BorderFactory.createLineBorder(Color.RED));
                return;
            }
            okPressed = true;
            dialog.setVisible(false);
            dialog.dispose();
        });

        buttonPanel.add(buttonOk, createLabelConstraint(0));
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new GridBagLayout());

        int row = 0;
        /* id */
        idTextField = new JTextField(profile.id);
        mainPanel.add(new JLabel("Id"), createLabelConstraint(row));
        mainPanel.add(idTextField, createComponentConstraint(row++));
        if (idEditAllowed) {
            idTextField.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            idTextField.setToolTipText("mandatory field");
        } else {
            idTextField.setEditable(false);
        }
        descriptionTextArea = new JTextArea(profile.description);
        mainPanel.add(new JLabel("Description"), createLabelConstraint(row));
        mainPanel.add(descriptionTextArea, createComponentConstraint(row++));

        /* enabled */
        enabledCheckBox = new JCheckBox("", profile.enabled);
        mainPanel.add(new JLabel("Enabled"), createLabelConstraint(row));
        mainPanel.add(enabledCheckBox, createComponentConstraint(row++));

        /* configurations */
        model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Executor config name");
        model.addColumn("enabled");
        model.addColumn("uuid");

        configurationTable = new JTable(model);
        configurationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()>1) {
                    int row = configurationTable.getSelectedRow();
                    Object selectedValue = model.getValueAt(row, 2);
                    if (selectedValue instanceof UUID) {
                        UUID uuid = (UUID) selectedValue;
                        
                        boolean changed = editConfigurationAction.executeDirectly(uuid);
                        if (!changed) {
                            return;
                        }
                        reloadChangedDataIntoLocalProfile(uuid);
                    }else {
                        throw new IllegalStateException("not a uuid:"+selectedValue);
                    }
                }
            }

            private void reloadChangedDataIntoLocalProfile(UUID uuid) {
                TestExecutorConfig found = null;
                for (TestExecutorConfig config: profile.configurations) {
                    if (uuid.equals(config.uuid)){
                        found=config;
                        break;
                    }
                }
                if (found==null) {
                    getContext().getOutputUI().error("Did not found config again with uuid:"+uuid);
                    return;
                }
                profile.configurations.remove(found);
                TestExecutorConfig newConfig = getContext().getAdministration().fetchExecutorConfiguration(uuid);
                profile.configurations.add(newConfig);
                
                rebuildTableModelRows();
            }
        });
        rebuildTableModelRows();
        if (model.getRowCount() > 0) {
            configurationTable.setRowSelectionInterval(0, 0);
        }
        new TableRendersupport().addStandardTableCellRender(configurationTable);

        GridBagConstraints tableConstraint = createComponentConstraint(row++);
        tableConstraint.gridx = 0;
        tableConstraint.gridwidth++;
        tableConstraint.weighty=0.5;
        JScrollPane tableScrollPane = new JScrollPane(configurationTable);
        tableScrollPane.setPreferredSize(new Dimension(600,200));
        mainPanel.add(tableScrollPane, tableConstraint);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(new JButton(new AddConfigAction()));
        buttonPanel.add(new JButton(new RemoveConfigAction()));

        mainPanel.add(buttonPanel, createComponentConstraint(row++));

        /* project ids - label */
        projectIdsLabel = new JLabel();
        GridBagConstraints projectIdLabelConstraint = createComponentConstraint(row++);
        projectIdLabelConstraint.ipady=0;
        projectIdLabelConstraint.gridx = 0;
        projectIdLabelConstraint.gridwidth++;
        mainPanel.add(projectIdsLabel,projectIdLabelConstraint);
        
        /* text area for project ids */
        projectIdsTextArea = new JTextArea();
        projectIdsTextArea.setLineWrap(true);
        projectIdsTextArea.setEditable(false);
        projectIdsTextArea.setColumns(200);
        GridBagConstraints textAreaConstraint = createComponentConstraint(row++);
        textAreaConstraint.gridx = 0;
        textAreaConstraint.gridwidth++;
        textAreaConstraint.weighty=0.5;
        JScrollPane textAreaScrollPane = new JScrollPane(projectIdsTextArea);
        textAreaScrollPane.setPreferredSize(new Dimension(600,200));
        mainPanel.add(textAreaScrollPane, textAreaConstraint);
        
        updateProjectIdsAtUI();
        
    }

    private void updateProjectIdsAtUI() {
        String json = JSONConverter.get().toJSON(profile.projectIds);
        projectIdsTextArea.setText(json);
        projectIdsLabel.setText("Assigned projects:"+profile.projectIds.size());
    }

    private void rebuildTableModelRows() {
        /* clear old model - by set row count 0 - removes old data...*/
        model.setRowCount(0);
        /* name, enabled, uuid */
        for (TestExecutorConfig config : profile.configurations) {
            model.addRow(new Object[] { config.name, config.enabled, config.uuid });
        }

    }

    private class AddConfigAction extends FailsafeAction {

        private static final long serialVersionUID = -7451653578286573056L;

        public AddConfigAction() {
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource("icons/material-io/twotone_add_circle_black_18dp.png")));
            putValue(Action.NAME, "Add");
        }

        @Override
        protected void safeActionPerformed(ActionEvent e) {

            ListExecutorConfigurationDialogUI dialogUI = new ListExecutorConfigurationDialogUI(getContext(), "Please selected wanted config to add");
            dialogUI.showDialog();
            if (!dialogUI.isOkPressed()) {
                return;
            }
            UUID uuid = dialogUI.getSelectedValue();
            if (uuid == null) {
                return;
            }
            TestExecutorConfig config = getContext().getAdministration().fetchExecutorConfiguration(uuid);
            profile.configurations.add(config);

            rebuildTableModelRows();
        }

    }

    private class RemoveConfigAction extends FailsafeAction {

        private static final long serialVersionUID = 966121685944120849L;

        public RemoveConfigAction() {
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource("icons/material-io/twotone_remove_circle_black_18dp.png")));
            putValue(Action.NAME, "Remove");
        }

        @Override
        public void safeActionPerformed(ActionEvent e) {
            int row = configurationTable.getSelectedRow();
            Object uuidObj = model.getValueAt(row, 2);

            TestExecutorConfig configWithUUID = null;
            for (TestExecutorConfig config : profile.configurations) {
                if (config.uuid.equals(uuidObj)) {
                    configWithUUID = config;
                    break;
                }
            }
            if (configWithUUID == null) {
                getContext().getDialogUI().confirm("There is no config available with uuid:" + uuidObj);
                return;
            }
            profile.configurations.remove(configWithUUID);
            rebuildTableModelRows();
            configurationTable.revalidate();
        }

    }

    public TestExecutionProfile getUpdatedProfile() {
        profile.enabled = enabledCheckBox.isSelected();
        if (idEditAllowed) {
            profile.id = fetchProfileIdFromUI();
        }
        profile.description = descriptionTextArea.getText();
        profile.projectIds=null; // set explicit to null, we cannot update this, and we do not want it inside output json... 
        return profile;
    }

    private String fetchProfileIdFromUI() {
        return idTextField.getText().trim().toLowerCase();
    }

}
