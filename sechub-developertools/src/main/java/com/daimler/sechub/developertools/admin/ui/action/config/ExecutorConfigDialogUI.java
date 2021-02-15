// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import static com.daimler.sechub.developertools.admin.ui.DialogGridBagConstraintsFactory.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;
import com.daimler.sechub.developertools.admin.ui.action.adapter.ProductExecutorTemplatesDialogUI.TemplatesDialogConfig;
import com.daimler.sechub.developertools.admin.ui.action.adapter.ProductExecutorTemplatesDialogUI.TemplatesDialogResult;
import com.daimler.sechub.developertools.admin.ui.action.adapter.ShowProductExecutorTemplatesDialogAction;
import com.daimler.sechub.developertools.admin.ui.util.SortedMapToTextConverter;
import com.daimler.sechub.developertools.admin.ui.util.TextToSortedMapConverter;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.test.executorconfig.TestExecutorConfig;
import com.daimler.sechub.test.executorconfig.TestExecutorSetupJobParam;

public class ExecutorConfigDialogUI {

    private UIContext context;
    private JPanel mainPanel;
    private TestExecutorConfig config;
    private JComboBox<ProductIdentifier> productIdentifierCombobox;
    private JSpinner executorVersionTextField;
    private JTextField nameTextField;
    private JTextField userTextField;
    private JTextField pwdTextField;
    private JTextField baseURLTextField;
    private JTextArea jobParametersTextArea;
    private JCheckBox enabledCheckBox;
    private JButton buttonOk;

    private boolean okPressed = false;
    private JPanel buttonPanel;
    private String title;
    private JTextField uuidTextField;
    private String buttonOkText;
    private SortedMapToTextConverter mapToTextConverter = new SortedMapToTextConverter();
    private TextToSortedMapConverter textToMapConverter = new TextToSortedMapConverter();
    private JScrollPane jobParameterScrollPane;

    public ExecutorConfigDialogUI(UIContext context, String title) {
        this(context, title, createExampleConfig());
    }

    public ExecutorConfigDialogUI(UIContext context, String title, TestExecutorConfig config) {
        this.context = context;
        this.config = config;
        this.title = title;
        this.buttonOkText = "Ok";
    }

    UIContext getContext() {
        return context;
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public static TestExecutorConfig createExampleConfig() {
        TestExecutorConfig config = new TestExecutorConfig();
        config.setup.baseURL = "https://newproduct.example.com";
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("example.key1", "value1"));
        config.setup.jobParameters.add(new TestExecutorSetupJobParam("example.key2", "value2"));
        config.setup.credentials.user = "env:EXAMPLE_USER_VARIABLE";
        config.setup.credentials.password = "env:EXAMPLE_PASSWORD_VARIABLE";
        config.enabled = false;
        config.executorVersion = 1;
        config.productIdentifier = ProductIdentifier.PDS_CODESCAN.name();

        return config;
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
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(context.getFrame());

        SwingUtilities.invokeLater(()->scrollToLeftTopOfJobParameters());
        dialog.setVisible(true);
    }

    private void createButtonPanel(JDialog dialog) {
        buttonPanel = new JPanel(new GridBagLayout());

        buttonOk = new JButton(buttonOkText);
        buttonOk.addActionListener((event) -> {
            String name = getNameFromUI();
            if (name == null || name.isEmpty()) {
                nameTextField.setBorder(BorderFactory.createLineBorder(Color.RED));
                return;
            }
            okPressed = true;
            dialog.setVisible(false);
            dialog.dispose();
        });

        buttonPanel.add(buttonOk, createLabelConstraint(0));
    }

    private void createMainPanel() {

        ActionSupport actionSupport = ActionSupport.getInstance();

        mainPanel = new JPanel(new GridBagLayout());

        int row = 0;
        /* name */
        nameTextField = new JTextField(config.name);
        mainPanel.add(new JLabel("Name"), createLabelConstraint(row));
        mainPanel.add(nameTextField, createComponentConstraint(row++));
        nameTextField.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
        nameTextField.setToolTipText("mandatory field");

        /* enabled */
        enabledCheckBox = new JCheckBox("", config.enabled);
        mainPanel.add(new JLabel("Enabled"), createLabelConstraint(row));
        mainPanel.add(enabledCheckBox, createComponentConstraint(row++));

        /* UUID readonly */
        uuidTextField = new JTextField(config.uuid != null ? "" + config.uuid : null);
        uuidTextField.setEditable(false);
        mainPanel.add(new JLabel("UUID"), createLabelConstraint(row));
        mainPanel.add(uuidTextField, createComponentConstraint(row++));

        /* product */
        ComboBoxModel<ProductIdentifier> comboBoxModel = new DefaultComboBoxModel<>(ProductIdentifier.values());
        productIdentifierCombobox = new JComboBox<>(comboBoxModel);
        productIdentifierCombobox.setSelectedItem(ProductIdentifier.valueOf(config.productIdentifier));
        mainPanel.add(new JLabel("Product identifier"), createLabelConstraint(row));
        mainPanel.add(productIdentifierCombobox, createComponentConstraint(row++));

        SpinnerNumberModel versionSpinnerModel = new SpinnerNumberModel(1, 0, 9999, 1);
        executorVersionTextField = new JSpinner(versionSpinnerModel);
        executorVersionTextField.setValue(config.executorVersion);
        mainPanel.add(new JLabel("Executor version"), createLabelConstraint(row));
        mainPanel.add(executorVersionTextField, createComponentConstraint(row++));

        /* base url */
        baseURLTextField = new JTextField(config.setup.baseURL);
        mainPanel.add(new JLabel("Product base url"), createLabelConstraint(row));
        mainPanel.add(baseURLTextField, createComponentConstraint(row++));
        actionSupport.provideUndoRedo(baseURLTextField);

        /* credentials */
        userTextField = new JTextField(config.setup.credentials.user);
        mainPanel.add(new JLabel("Product user:"), createLabelConstraint(row));
        mainPanel.add(userTextField, createComponentConstraint(row++));

        pwdTextField = new JTextField(config.setup.credentials.password);
        mainPanel.add(new JLabel("Product password/apitoken:"), createLabelConstraint(row));
        mainPanel.add(pwdTextField, createComponentConstraint(row++));

        /* template button */
        JButton button = new JButton("Edit parameters");

        mainPanel.add(button, createComponentConstraint(row++));

        /* job parameters */
        jobParametersTextArea = new JTextArea(resolveInitialJobParamsAsString());
        actionSupport.installAllTextActionsAsPopupTo(jobParametersTextArea);

        mainPanel.add(new JLabel("Parameters:"), createLabelConstraint(row));

        jobParameterScrollPane = new JScrollPane(jobParametersTextArea);
        GridBagConstraints jobParameterScrollPaneGridDataConstraints = createComponentConstraint(row++);
        jobParameterScrollPaneGridDataConstraints.gridheight = 140;
        jobParameterScrollPaneGridDataConstraints.weighty = 0.0;
        jobParameterScrollPaneGridDataConstraints.fill = GridBagConstraints.BOTH;

        jobParameterScrollPane.setPreferredSize(new Dimension(400, 300));
        jobParameterScrollPane.setMinimumSize(new Dimension(400, 300));

        mainPanel.add(jobParameterScrollPane, jobParameterScrollPaneGridDataConstraints);
        
        button.addActionListener(e -> {
            ProductIdentifier procutIdentifier = (ProductIdentifier) comboBoxModel.getSelectedItem();
            int version = (int) versionSpinnerModel.getNumber();
            ShowProductExecutorTemplatesDialogAction action = context.getCommandUI().resolveShowProductExecutorMappingDialogActionOrNull(procutIdentifier,
                    version);
            if (action == null) {
                JOptionPane.showMessageDialog(context.getFrame(), "No special parameter editor dialog available for " + procutIdentifier + " v" + version);
                return;
            }

            TemplatesDialogConfig config = new TemplatesDialogConfig();
            config.inputContent = jobParametersTextArea.getText();
            config.provideExportAllButton = true;
            config.provideExportAllButtonText = "Apply";

            /* open template dialog, auto import clipboard content and add apply button */
            TemplatesDialogResult result = action.openDialog(config);
            if (result.provideExportAllButtonPressed) {
                jobParametersTextArea.setText(result.outputContent);

                scrollToLeftTopOfJobParameters();

            }

        });

    }

    private void scrollToLeftTopOfJobParameters() {
        // scroll back to left top...
        JScrollBar vscrollbar = jobParameterScrollPane.getVerticalScrollBar();
        vscrollbar.setValue(vscrollbar.getMinimum());

        JScrollBar hscrollbar = jobParameterScrollPane.getHorizontalScrollBar();
        hscrollbar.setValue(hscrollbar.getMinimum());
    }

    public void setTextForOKButton(String text) {
        buttonOkText = text;
    }

    protected String resolveInitialJobParamsAsString() {
        TreeMap<String, String> map = new TreeMap<>();
        for (TestExecutorSetupJobParam param : config.setup.jobParameters) {
            map.put(param.key, param.value);
        }
        return mapToTextConverter.convertToText(map);
    }

    public TestExecutorConfig getUpdatedConfig() {
        Integer execVersionObj = (Integer) executorVersionTextField.getValue();

        config.productIdentifier = productIdentifierCombobox.getSelectedItem().toString();
        config.enabled = enabledCheckBox.isSelected();
        config.name = getNameFromUI();
        config.executorVersion = execVersionObj.intValue();
        // config.uuid - is read only and just for view...

        /* setup changes */
        config.setup.baseURL = baseURLTextField.getText();
        config.setup.credentials.user = userTextField.getText();
        config.setup.credentials.password = pwdTextField.getText();
        writeJobParamsStringBackToConfig();

        return config;
    }

    private String getNameFromUI() {
        return nameTextField.getText().trim();
    }

    private void writeJobParamsStringBackToConfig() {

        SortedMap<String, String> map = textToMapConverter.convertFromText(jobParametersTextArea.getText());

        config.setup.jobParameters.clear();
        for (String key : map.keySet()) {
            String value = map.get(key);
            config.setup.jobParameters.add(new TestExecutorSetupJobParam(key, value));

        }
    }

}
