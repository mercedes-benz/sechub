// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.Action;

import com.mercedesbenz.sechub.api.internal.gen.model.*;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;

/**
 * This is only a simple test application - so we do not need to start IntelliJ
 * all time here and are able to test UI interaction provided by
 * SecHubToolWindowUISupport class
 */
public class SecHubToolWindowUISupportTestMain {

    public static void main(String[] args) {
        new InternalUITest().start();
    }

    private static class InternalUITest {

        private static int findingCounter = 1;
        private SecHubToolWindowUISupport supportToTest;

        private void start() {
            JFrame frame = new JFrame("Test application");
            JMenu menu = new JMenu("Action");
            menu.add(new SetNewModelAction());
            JMenuBar menuBar = new JMenuBar();

            ImageIcon codeScanIcon = new ImageIcon("icons/activity.png", "Just a green ball...");

            menuBar.add(menu);
            frame.setJMenuBar(menuBar);

            JTree callHierarchyTree = new JTree();
            FindingModel model = createTestModel();

            JTable reportTable = new JTable();
            JTable callStepDetailTable = new JTable();
            JSplitPane callHierarchySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(callHierarchyTree),
                    new JScrollPane(callStepDetailTable));
            JTabbedPane tabbedPane = new JTabbedPane();
            JPanel webRequestPanel = new JPanel();
            JPanel webResponsePanel = new JPanel();
            JPanel attackPanel = new JPanel();

            JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(reportTable), tabbedPane);
            frame.add(pane2);

            JPanel southPanel = new JPanel();
            JLabel reportFindingLabel = new JLabel("No finding selected");
            JLabel callStepLabel = new JLabel("No call step selected");
            southPanel.add(reportFindingLabel);
            southPanel.add(new JLabel("---"));
            southPanel.add(callStepLabel);
            frame.add(southPanel, BorderLayout.SOUTH);

            callHierarchyTree.setPreferredSize(new Dimension(800, 600));

            SecHubToolWindowUIContext context = new SecHubToolWindowUIContext();
            context.findingTable = reportTable;
            context.callHierarchyDetailTable = callStepDetailTable;
            context.callHierarchyTree = callHierarchyTree;
            context.errorLog = new ErrorLog() {
            };
            context.cweIdLabel = new JLabel("cwe");
            context.findingTypeDetailsTabbedPane = tabbedPane;
            context.callHierarchyTabComponent = callHierarchySplitPane;
            context.descriptionAndSolutionTabbedPane = new JTabbedPane();

            JTextArea webRequestTextArea = new JTextArea();
            JTextArea webResponseTextArea = new JTextArea();
            JTextArea attackTextArea = new JTextArea();

            attackPanel.setLayout(new BorderLayout());
            attackPanel.add(new JScrollPane(attackTextArea), BorderLayout.CENTER);

            webRequestPanel.setLayout(new BorderLayout());
            webRequestPanel.add(new JScrollPane(webRequestTextArea), BorderLayout.CENTER);

            webResponsePanel.setLayout(new BorderLayout());
            webResponsePanel.add(new JScrollPane(webResponseTextArea), BorderLayout.CENTER);

            context.webRequestTextArea = webRequestTextArea;
            context.webResponseTextArea = webResponseTextArea;
            context.attackTextArea = attackTextArea;
            context.attackTabComponent = attackPanel;
            context.webRequestTabComponent = webRequestPanel;
            context.webResponseTabComponent = webResponsePanel;

            context.componentFactory = new ComponentBuilder() {

                @Override
                public JScrollPane createScrollPane(JComponent component) {
                    return new JScrollPane(component);
                }
            };
            context.findingRenderDataProvider = new FindingRenderDataProvider() {

                @Override
                public Icon getIconForScanType(ScanType scanType) {
                    if (ScanType.CODE_SCAN.equals(scanType)) {
                        return codeScanIcon;
                    }
                    return null;
                }

                @Override
                public Icon getIconForTrafficLight(TrafficLight trafficLight) {
                    return null;
                }

            };
            supportToTest = new SecHubToolWindowUISupport(context);
            supportToTest.initialize();
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            supportToTest.setFindingModel(model);
            supportToTest.addCallStepChangeListener((callStep, showEditor) -> {
                callStepLabel.setText(callStep.getRelevantPart() + " showEditor=" + showEditor);
            });

            supportToTest.addReportFindingSelectionChangeListener((finding) -> {
                reportFindingLabel.setText(finding.getDescription());
            });
        }

        private FindingModel createTestModel() {
            FindingModel model = new FindingModel();

            append(model, "alpha", Severity.CRITICAL, ScanType.CODE_SCAN);
            append(model, "beta", Severity.HIGH, ScanType.CODE_SCAN);
            append(model, "gamma", Severity.MEDIUM, ScanType.CODE_SCAN);
            append(model, "delta", Severity.LOW, ScanType.CODE_SCAN);
            append(model, "epsilon", Severity.INFO, ScanType.CODE_SCAN);
            append(model, "zeta", Severity.INFO, ScanType.WEB_SCAN);
            append(model, "eta", Severity.INFO, ScanType.INFRA_SCAN);
            append(model, "theta", Severity.INFO, ScanType.SECRET_SCAN);
            return model;
        }

        private void append(FindingModel model, String prefix, Severity severity, ScanType scanType) {
            int step = 1;

            SecHubFinding finding = new SecHubFinding();
            if (scanType.equals(ScanType.WEB_SCAN)) {
                SecHubReportWeb web = new SecHubReportWeb();
                finding.setWeb(web);
                web.getRequest().setMethod("http");
                web.getRequest().getHeaders().put("header-key-", "header-value");
                web.getResponse().setReasonPhrase("the reason...");
                web.getResponse().getHeaders().put("header-key-", "header-value");
                web.getAttack().setVector("attack vector...");
                SecHubReportWebEvidence evidence = new SecHubReportWebEvidence();
                evidence.setSnippet("<Code> snippet with problem inside </code>");
                web.getAttack().setEvidence(evidence);
            }

            /* @formatter:off */
            FindingNode node1 = FindingNode.builder()
                    .setId(findingCounter++)
                    .setDescription(generateDescription(prefix))
                    .setScanType(scanType)
                    .setName("the name for " + prefix)
                    .setSecHubFinding(finding)
                    .setColumn(12)
                    .setLine(1)
                    .setLocation("/some/where/found/Xyz.java")
                    .setSeverity(severity)
                    .setCallStackStep(step++)
                    .setRelevantPart("i am relevant1")
                    .setSource("I am source... and i am relevant")
                    .build();

            FindingNode node2 = FindingNode.builder()
                    .setId(findingCounter++)
                    .setDescription(generateDescription(prefix))
                    .setColumn(13)
                    .setLine(2)
                    .setLocation("/some/where/found/Xyz.java")
                    .setSeverity(severity)
                    .setCallStackStep(step++)
                    .setRelevantPart("i am relevant2")
                    .setSource("I am source... and i am relevant")
                    .build();

            FindingNode node3 = FindingNode.builder()
                    .setId(findingCounter++)
                    .setDescription(generateDescription(prefix))
                    .setColumn(14)
                    .setLine(3)
                    .setLocation("/some/where/found/Xyz.java")
                    .setSeverity(severity)
                    .setCallStackStep(step++)
                    .setRelevantPart("i am relevant3")
                    .setSource("I am source... and i am relevant")
                    .build();
            /* @formatter:on */

            node1.getChildren().add(node2);
            node2.getChildren().add(node3);
            model.getFindings().add(node1);
        }

        private String generateDescription(String prefix) {
            return "describe-" + prefix + "_" + findingCounter;
        }

        private class SetNewModelAction extends AbstractAction {
            private static final long serialVersionUID = 1L;

            private SetNewModelAction() {
                putValue(Action.NAME, "set new model");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                InternalUITest uiTest = InternalUITest.this;
                uiTest.supportToTest.setFindingModel(uiTest.createTestModel());
            }

        }
    }
}
