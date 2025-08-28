// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.tree.DefaultMutableTreeNode;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportImporter;
import com.mercedesbenz.sechub.plugin.idea.compatiblity.VirtualFileCompatibilityLayer;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.*;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.mercedesbenz.sechub.api.internal.gen.model.TrafficLight;
import com.mercedesbenz.sechub.plugin.idea.IntellijComponentFactory;
import com.mercedesbenz.sechub.plugin.idea.IntellijRenderDataProvider;
import com.mercedesbenz.sechub.plugin.idea.IntellijShowInEditorSupport;
import com.mercedesbenz.sechub.plugin.idea.util.ErrorLogger;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUIContext;
import com.mercedesbenz.sechub.plugin.ui.SecHubToolWindowUISupport;
import com.mercedesbenz.sechub.plugin.ui.SecHubTreeNode;
import com.mercedesbenz.sechub.plugin.util.SimpleStringUtil;
import org.jetbrains.annotations.Nullable;

public class SecHubReportPanel implements SecHubPanel {
    private static final Logger LOG = Logger.getInstance(SecHubReportPanel.class);
    private static final int SECHUB_REPORT_DEFAULT_GAP = 5;
    private static SecHubReportPanel INSTANCE;

    private SecHubToolWindowUISupport uiSupport;
    private IntellijShowInEditorSupport showInEditorSupport;
    private Icon callHierarchyElementIcon;

    private final ToolWindow toolWindow;
    private final Project project;
    private JLabel trafficLightIconLabel;
    private JLabel amountOfFindingsLabel;
    private JBTextField scanResultForJobText;
    private JBTable callStepDetailTable;
    private Tree callHierarchyTree;
    private JBTable reportTable;
    private JBLabel cweIdLabel;
    private JBTextArea reportSourceCodeTextArea;
    private JBTextArea findingDescriptionTextArea;

    private JPanel contentPanel;
    private JBLabel findingLabel;
    private JTextArea findingSolutionTextArea;
    private JBTabbedPane southTabPane;
    private JPanel webRequestPanel;
    private JPanel webResponsePanel;
    private JTabbedPane descriptionAndSolutionTabbedPane;
    private JTextArea webRequestTextArea;
    private JTextArea webResponseTextArea;
    private JTextArea attackTextArea;
    private JPanel callHierarchyPanel;
    private JPanel attackPanel;
    private JButton explanationButton;

    public SecHubReportPanel(Project project, ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;

        createComponents();
        createAndInstallSupport();

        installDragAndDrop();

        customizeCallHierarchyTree();
        customizeCallStepDetailsTable();

        resetReport();
    }

    private void createComponents() {

        JPanel contentNorth = createReportTablePanel();
        JPanel contentSouth = createFindingPanel();

        JBSplitter reportAndDetailsSplitterPanel = new OnePixelSplitter(true);
        reportAndDetailsSplitterPanel.setShowDividerControls(true);
        reportAndDetailsSplitterPanel.setShowDividerIcon(true);
        reportAndDetailsSplitterPanel.setFirstComponent(new JBScrollPane(contentNorth));
        reportAndDetailsSplitterPanel.setSecondComponent(contentSouth);
        reportAndDetailsSplitterPanel.setDividerPositionStrategy(Splitter.DividerPositionStrategy.DISTRIBUTE);

        callHierarchyElementIcon = AllIcons.General.ChevronDown;

        contentPanel = new JBPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(reportAndDetailsSplitterPanel, BorderLayout.CENTER);
    }

    private JPanel createReportTablePanel() {

        JBTable reportTable = new JBTable();
        JBScrollPane reportTableScrollPane = new JBScrollPane(reportTable);

        JLabel trafficLightIconLabel = new JLabel();
        JLabel amountOfFindingsLabel = new JLabel();

        JPanel secHubReportTablePanel = new JBPanel();
        secHubReportTablePanel.setLayout(new BorderLayout());

        JBTextField scanResultForJobText = new JBTextField();
        scanResultForJobText.setEditable(false);
        scanResultForJobText.setBorder(null); // avoid jumping field in UI - looks now like a label, but people can select and
                                              // copy the job uuid if wanted...

        JPanel secHubHeaderPanel = new JBPanel();
        secHubHeaderPanel.setLayout(new BoxLayout(secHubHeaderPanel, BoxLayout.Y_AXIS));

        secHubHeaderPanel.add(createActionBarPanel());

        JPanel secHubReportResultPanel = new JBPanel();
        secHubReportResultPanel.setLayout(new HorizontalLayout(SECHUB_REPORT_DEFAULT_GAP));

        JPanel secHubTrafficLightPanel = new JBPanel();
        secHubTrafficLightPanel.setLayout(new HorizontalLayout(SECHUB_REPORT_DEFAULT_GAP));
        secHubTrafficLightPanel.add(trafficLightIconLabel);
        secHubTrafficLightPanel.setBorder(BorderFactory.createEmptyBorder(SECHUB_REPORT_DEFAULT_GAP, 10, 0, 0));

        secHubReportResultPanel.add(secHubTrafficLightPanel);
        secHubReportResultPanel.add(amountOfFindingsLabel);
        secHubReportResultPanel.add(scanResultForJobText);

        secHubHeaderPanel.add(secHubReportResultPanel);

        JPanel secHubReportContentPanel = new JBPanel();
        secHubReportContentPanel.setLayout(new BorderLayout());
        secHubReportTablePanel.add(secHubHeaderPanel, BorderLayout.NORTH);
        secHubReportTablePanel.add(secHubReportContentPanel, BorderLayout.CENTER);
        secHubReportContentPanel.add(reportTableScrollPane, BorderLayout.CENTER);

        /* now setup fields */
        this.scanResultForJobText = scanResultForJobText;
        this.trafficLightIconLabel = trafficLightIconLabel;
        this.amountOfFindingsLabel = amountOfFindingsLabel;

        this.reportTable = reportTable;

        return secHubReportTablePanel;
    }

    @NotNull
    private JPanel createActionBarPanel() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        AnAction importReport = new AnAction("Import Report", "Import report from disk", AllIcons.Actions.MenuOpen) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                importReportFromDisk(e.getProject());
            }
        };
        actionGroup.add(importReport);

        AnAction clearAction = new AnAction("Clear Report", "Clear current report data", AllIcons.Actions.Rollback) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                resetReport();
            }
        };
        actionGroup.add(clearAction);

        Icon checkmarkIcon = new CheckmarkIcon();
        AnAction syncFalsePositives = new AnAction("Mark False Positives", "Mark false positives", checkmarkIcon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                uiSupport.syncFalsePositives();
            }
        };
        actionGroup.add(syncFalsePositives);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true);
        toolbar.setTargetComponent(null);
        toolbar.setMinimumButtonSize(new Dimension(25, 25));

        JPanel actionBar = new JBPanel(new BorderLayout());
        actionBar.add(toolbar.getComponent(), BorderLayout.WEST);
        actionBar.setOpaque(false);
        actionBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        actionBar.setMinimumSize(new Dimension(0, 30));
        actionBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        actionBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        return actionBar;
    }

    private JPanel createFindingPanel() {

        JComponent findingNorthPanel = createFindingNorthComponent();
        JComponent findingSouthPanel = createFindingSouthComponent();

        JBSplitter findingAndFindingStepsSplitter = new OnePixelSplitter(true);
        findingAndFindingStepsSplitter.setFirstComponent(findingNorthPanel);
        findingAndFindingStepsSplitter.setSecondComponent(findingSouthPanel);
        findingAndFindingStepsSplitter.setShowDividerControls(true);
        findingAndFindingStepsSplitter.setShowDividerIcon(true);
        findingAndFindingStepsSplitter.setProportion(0.1f);

        return findingAndFindingStepsSplitter;

    }

    private JComponent createFindingNorthComponent() {
        JBLabel findingLabel = new JBLabel();
        JBLabel cweIdLabel = new JBLabel();
        JButton explanationButton = new JButton("✨Explain");

        explanationButton.setToolTipText("Explain SecHub Vulnerability");

        JPanel findingCweExplanationPanel = new JBPanel<>();
        findingCweExplanationPanel.setLayout(new HorizontalLayout(SECHUB_REPORT_DEFAULT_GAP));
        findingCweExplanationPanel.add(findingLabel);
        findingCweExplanationPanel.add(cweIdLabel);
        findingCweExplanationPanel.add(explanationButton);

        JBTextArea findingDescriptionTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        JBTextArea findingSolutionTextArea = prepareNonEditLargeTextArea(new JBTextArea());

        descriptionAndSolutionTabbedPane = new JBTabbedPane();
        descriptionAndSolutionTabbedPane.add("Description", new JBScrollPane(findingDescriptionTextArea));
        descriptionAndSolutionTabbedPane.add("Solution", new JBScrollPane(findingSolutionTextArea));

        JPanel northPanel = new JBPanel();
        northPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        northPanel.add(findingCweExplanationPanel);
        northPanel.add(descriptionAndSolutionTabbedPane);

        /* setup now as fields */
        this.findingLabel = findingLabel;
        this.cweIdLabel = cweIdLabel;
        this.explanationButton = explanationButton;
        this.findingDescriptionTextArea = findingDescriptionTextArea;
        this.findingSolutionTextArea = findingSolutionTextArea;

        return northPanel;
    }

    private JBTextArea prepareNonEditLargeTextArea(JBTextArea textArea) {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        Caret caret = textArea.getCaret();
        if (caret instanceof DefaultCaret defaultCaret) {
            defaultCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        return textArea;
    }

    private JComponent createFindingSouthComponent() {
        southTabPane = new JBTabbedPane();

        createCallHierachyComponents();

        createWebRequestComponents();
        createWebResponseComponents();
        createAttackComponents();

        return southTabPane;
    }

    private void createWebResponseComponents() {
        webResponseTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        webResponsePanel = new JBPanel<>();
        webResponsePanel.setLayout(new BorderLayout());
        webResponsePanel.add(webResponseTextArea, BorderLayout.CENTER);
    }

    private void createWebRequestComponents() {
        webRequestTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        webRequestPanel = new JBPanel<>();
        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.add(new JBScrollPane(webRequestTextArea), BorderLayout.CENTER);
    }

    private void createAttackComponents() {
        attackTextArea = prepareNonEditLargeTextArea(new JBTextArea());
        attackPanel = new JBPanel<>();
        attackPanel.setLayout(new BorderLayout());
        attackPanel.add(new JBScrollPane(attackTextArea), BorderLayout.CENTER);
    }

    private void createCallHierachyComponents() {
        Tree callHierarchyTree = new Tree();

        JBTextArea reportSourceCodeTextArea = prepareNonEditLargeTextArea(new JBTextArea());

        JBLabel reportSourceCodeLabel = new JBLabel("Source:");

        JBPanel reportSourceCodePanel = new JBPanel();
        reportSourceCodePanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        reportSourceCodePanel.add(reportSourceCodeLabel);
        reportSourceCodePanel.add(reportSourceCodeTextArea);

        JBTable callStepDetailTable = new JBTable();
        JBPanel callStepDetailPanel = new JBPanel();
        callStepDetailPanel.setLayout(new VerticalLayout(SECHUB_REPORT_DEFAULT_GAP));
        callStepDetailPanel.add(new JBScrollPane(callStepDetailTable));
        callStepDetailPanel.add(reportSourceCodePanel);

        OnePixelSplitter callHierarchySplitterPanel = new OnePixelSplitter(false);
        callHierarchySplitterPanel.setFirstComponent(new JBScrollPane(callHierarchyTree));
        callHierarchySplitterPanel.setSecondComponent(new JBScrollPane(callStepDetailPanel));
        callHierarchySplitterPanel.setShowDividerControls(true);
        callHierarchySplitterPanel.setShowDividerIcon(true);
        callHierarchySplitterPanel.setDividerPositionStrategy(Splitter.DividerPositionStrategy.DISTRIBUTE);

        /* now set as fields */
        this.callStepDetailTable = callStepDetailTable;
        this.callHierarchyTree = callHierarchyTree;
        this.callHierarchyPanel = callHierarchySplitterPanel;
        this.reportSourceCodeTextArea = reportSourceCodeTextArea;
    }

    private void customizeCallStepDetailsTable() {
        callStepDetailTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    Object component = callHierarchyTree.getLastSelectedPathComponent();
                    if (component instanceof DefaultMutableTreeNode treeNode) {
                        Object userObject = treeNode.getUserObject();
                        if (userObject instanceof FindingNode) {
                            showInEditor((FindingNode) userObject);
                        }
                    }
                }
            }
        });
    }

    private void createAndInstallSupport() {

        showInEditorSupport = new IntellijShowInEditorSupport();

        SecHubToolWindowUIContext context = new SecHubToolWindowUIContext();
        context.project = this.project;
        context.findingTable = reportTable;

        context.callHierarchyTabComponent = callHierarchyPanel;
        context.callHierarchyTree = callHierarchyTree;
        context.callHierarchyDetailTable = callStepDetailTable;

        context.errorLog = ErrorLogger.getInstance();
        context.cweIdLabel = cweIdLabel;
        context.explanationButton = explanationButton;
        context.findingRenderDataProvider = new IntellijRenderDataProvider();
        context.componentFactory = new IntellijComponentFactory();
        context.findingTypeDetailsTabbedPane = southTabPane;

        context.descriptionAndSolutionTabbedPane = descriptionAndSolutionTabbedPane;

        context.webResponseTabComponent = webResponsePanel;
        context.webResponseTextArea = webResponseTextArea;

        context.webRequestTabComponent = webRequestPanel;
        context.webRequestTextArea = webRequestTextArea;

        context.attackTextArea = attackTextArea;
        context.attackTabComponent = attackPanel;

        uiSupport = new SecHubToolWindowUISupport(context);

        uiSupport.addCallStepChangeListener((callStep, showEditor) -> {
            reportSourceCodeTextArea.setText(callStep == null ? "" : SimpleStringUtil.toStringTrimmed(callStep.getSource()) + "\n");
            /* now show in editor as well */
            if (showEditor) {
                showInEditor(callStep);
            }
        });

        uiSupport.addReportFindingSelectionChangeListener((finding) -> {
            findingLabel.setText("Finding " + finding.getId() + ":");
            findingDescriptionTextArea.setText(finding.getDescription() == null ? "No description available" : finding.getDescription());
            if (finding.getSolution() == null) {
                if (finding.getCweId() == null) {
                    findingSolutionTextArea.setText("No solution available");
                } else {
                    findingSolutionTextArea.setText("Please follow the CWE link and read the solution text there.");
                }
            } else {
                findingSolutionTextArea.setText(finding.getSolution());
            }
        });

        uiSupport.initialize();
    }

    private void installDragAndDrop() {
        SecHubToolWindowTransferSupport transferHandler = new SecHubToolWindowTransferSupport(ErrorLogger.getInstance());
        reportTable.setTransferHandler(transferHandler);
        callHierarchyTree.setTransferHandler(transferHandler);

        contentPanel.setTransferHandler(transferHandler);
        reportSourceCodeTextArea.setTransferHandler(transferHandler);
        callStepDetailTable.setTransferHandler(transferHandler);
    }

    private void customizeCallHierarchyTree() {
        callHierarchyTree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (!(value instanceof DefaultMutableTreeNode)) {
                    return;
                }
                SecHubTreeNode treeNode = (SecHubTreeNode) value;
                FindingNode findingNode = treeNode.getFindingNode();
                if (findingNode == null) {
                    return;
                }
                String relevantPart = findingNode.getRelevantPart();
                if (relevantPart != null) {
                    append(relevantPart, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                    append(" - ");
                }
                String fileName = findingNode.getFileName();
                append(fileName == null ? "Unknown" : fileName, SimpleTextAttributes.GRAY_ATTRIBUTES);

                setIcon(callHierarchyElementIcon);

            }
        });
    }

    public static void registerInstance(SecHubReportPanel secHubToolWindow) {
        LOG.info("register tool windows instance:" + secHubToolWindow);
        INSTANCE = secHubToolWindow;
    }

    public static SecHubReportPanel getInstance() {
        return INSTANCE;
    }

    private void showInEditor(FindingNode callStep) {
        if (callStep == null) {
            return;
        }
        if (!callStep.canBeShownInCallHierarchy()) {
            return;
        }
        showInEditorSupport.showInEditor(toolWindow, callStep);
    }

    @Override
    public JPanel getContent() {
        return contentPanel;
    }

    public void update(FindingModel model) {
        // reset old field data
        findingLabel.setText("");
        cweIdLabel.setText("");
        findingDescriptionTextArea.setText("");
        findingSolutionTextArea.setText("");
        reportSourceCodeTextArea.setText("");
        descriptionAndSolutionTabbedPane.setVisible(false);
        uiSupport.showFindingNode(null, false);

        UUID jobUUID = model.getJobUUID();
        TrafficLight trafficLight = model.getTrafficLight();

        if (trafficLight == null) {
            trafficLight = TrafficLight.OFF;
        }

        if (jobUUID == null) {
            amountOfFindingsLabel.setText("No SecHub report loaded");
            scanResultForJobText.setText("");
        } else {
            amountOfFindingsLabel.setText(model.getFindings().size() + " findings in job:");
            scanResultForJobText.setText(jobUUID.toString());
        }

        Icon iconForTrafficLight = new TrafficLightIcon(trafficLight);
        trafficLightIconLabel.setIcon(iconForTrafficLight);
        trafficLightIconLabel.setToolTipText("Traffic light is:" + trafficLight);
        trafficLightIconLabel.setBorder(BorderFactory.createEmptyBorder(0, -2, SECHUB_REPORT_DEFAULT_GAP, 0));

        uiSupport.setFindingModel(model);
    }

    private void importReportFromDisk(Project currentProject) {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json");
        fileChooserDescriptor.setDescription("Open SecHub report");
        @Nullable
        VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, currentProject, null);

        if (file == null) {
            return;
        }
        @NotNull
        Path p = VirtualFileCompatibilityLayer.toNioPath(file);
        try {
            SecHubReportImporter.getInstance().importAndDisplayReport(p.toFile());
        } catch (IOException ex) {
            LOG.error("Failed to import " + p, ex);
        }
    }

    public void resetReport() {
        update(new FindingModel());

        uiSupport.resetTablePresentation();
        uiSupport.resetCallHierarchyStepTable();
        uiSupport.resetFindingNodeTabPane();
        uiSupport.resetDescriptionAndSolutionTabPane(false);
    }

    private static class TrafficLightIcon implements Icon {
        private final JBColor color;

        public TrafficLightIcon(TrafficLight trafficLight) {
            switch (trafficLight) {
                case RED -> color = JBColor.RED;
                case YELLOW -> color = JBColor.YELLOW;
                case GREEN -> color = JBColor.GREEN;
                default -> color = JBColor.GRAY;
            }
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x + 2, y + 2, 12, 12);
            g.setColor(JBColor.DARK_GRAY);
            g.drawOval(x + 2, y + 2, 12, 12);
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class CheckmarkIcon implements Icon {

        private static final int SIZE = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(JBColor.BLUE);
            g2.setFont(new Font("Dialog", Font.BOLD, SIZE));
            FontMetrics fm = g2.getFontMetrics();
            String checkmarkIcon = "✔";
            int textX = x + (getIconWidth() - fm.stringWidth(checkmarkIcon)) / 2;
            int textY = y + ((getIconHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(checkmarkIcon, textX, textY);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
}