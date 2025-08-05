// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportViewUpdater;
import com.mercedesbenz.sechub.plugin.idea.falsepositive.FalsePositiveSyncStatus;
import com.mercedesbenz.sechub.plugin.idea.falsepositive.FalsePositivesCacheManager;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import com.mercedesbenz.sechub.plugin.model.SecHubReportFindingModelService;
import org.jetbrains.annotations.NotNull;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.Severity;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.model.SecHubFindingoWebScanDataProvider;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;
import org.jetbrains.annotations.Nullable;

/**
 * Because its very inconvenient and slow to test and develop the toolwindow
 * this support class was established. There exists a simple
 * SecHubToolWindowUISupportTestMain application class which can be called to
 * tweak ui without need to start intellj all time
 */
public class SecHubToolWindowUISupport {
    private static final SecHubReportFindingModelService findingModelService = SecHubReportFindingModelService.getInstance();
    private static final SecHubReportViewUpdater viewUpdater = SecHubReportViewUpdater.getInstance();
    private static final FalsePositivesCacheManager cacheManager = FalsePositivesCacheManager.getInstance();
    
    private static final String COLUMN_NAME_MARK_FALSE_POSITIVE = "False Positive";
    private static final String COLUMN_NAME_TYPE = "Type";
    private static final String COLUMN_NAME_SEVERITY = "Severity";
    private static final String COLUMN_NAME_ID = "Id";
    private static final String COLUMN_NAME_NAME = "Name";
    private static final String COLUMN_NAME_LOCATION = "Location";
    /* @formatter:off */
    private static final String[] REPORT_TABLE_COLUMN_NAMES = {
            COLUMN_NAME_MARK_FALSE_POSITIVE,
            COLUMN_NAME_ID,
            COLUMN_NAME_SEVERITY,
            COLUMN_NAME_TYPE,
            COLUMN_NAME_NAME,
            COLUMN_NAME_LOCATION
    };
    /* @formatter:on */
    private final SecHubToolWindowUIContext context;
    private final JTable reportTable;
    private final JTree callHierarchyTree;
    private final JTable callStepDetailTable;
    private final ErrorLog errorLog;
    private final JLabel cweIdLabel;
    private final JTabbedPane findingTypeDetailsTabbedPane;

    private FindingModel findingModel;
    private final Set<CallStepChangeListener> callStepChangeListeners;
    private final Set<ReportFindingSelectionChangeListener> reportFindingSelectionChangeListeners;

    private final SecHubFindingoWebScanDataProvider webRequestDataProvider = new SecHubFindingoWebScanDataProvider();

    public SecHubToolWindowUISupport(SecHubToolWindowUIContext context) {
        this.context = context;
        this.reportTable = context.findingTable;
        this.callHierarchyTree = context.callHierarchyTree;
        this.callStepDetailTable = context.callHierarchyDetailTable;
        this.errorLog = context.errorLog;
        this.cweIdLabel = context.cweIdLabel;
        this.findingTypeDetailsTabbedPane = context.findingTypeDetailsTabbedPane;
        this.callStepChangeListeners = new LinkedHashSet<>();
        this.reportFindingSelectionChangeListeners = new LinkedHashSet<>();
    }

    public void syncFalsePositives() {
        UUID jobUUID = findingModel.getJobUUID();
        MarkFalsePositivesDialog dialog = new MarkFalsePositivesDialog();

        boolean hasMarkedFalsePositives = cacheManager.hasMarkedFalsePositives(jobUUID);

        if (hasMarkedFalsePositives && dialog.showAndGet()) {
            String reason = dialog.getSelectedReason();
            String comment = dialog.getComment();
            String combinedComment = "%s - %s".formatted(reason, comment);

            ProgressManager.getInstance().executeProcessUnderProgress(() -> {
                String projectId = findingModel.getProjectId();
                ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();

                if (projectId == null) {
                    boolean isSecHubServerAlive = secHubAccess().isSecHubServerAlive();

                    if (!isSecHubServerAlive) {
                        Messages.showErrorDialog(
                                "Failed to sync false positives: SecHub server is not reachable.",
                                "Sync Error"
                        );
                        return;
                    }

                    List<ProjectData> secHubProjects = secHubAccess().getSecHubProjects();

                    if (secHubProjects.isEmpty()) {
                        Messages.showErrorDialog(
                                "You are not assigned to any project.",
                                "No Project Found"
                        );
                        return;
                    }

                    String[] projectIds = secHubProjects.stream()
                            .map(ProjectData::getProjectId)
                            .toArray(String[]::new);

                    ProjectIdChooserDialog projectIdChooserDialog = new ProjectIdChooserDialog(projectIds);

                    if (projectIdChooserDialog.showAndGet()) {
                        projectId = projectIdChooserDialog.getSelectedProjectId();
                    } else {
                        return;
                    }
                }

                FalsePositiveSyncStatus syncStatus = cacheManager.syncFalsePositives(projectId, jobUUID, combinedComment, progressIndicator);

                if (syncStatus == FalsePositiveSyncStatus.SYNC_FAILED) {
                    Messages.showErrorDialog(
                            "Failed to sync false positives with the server. Please try again later.",
                            "Sync Error"
                    );
                    return;
                }

                if (syncStatus == FalsePositiveSyncStatus.EMPTY_CACHE) {
                    return;
                }

                findingModel = findingModelService.fetchAndBuildFindingModel(projectId, jobUUID, progressIndicator);
                resetTablePresentation();
                resetCallHierarchyStepTable();
                resetFindingNodeTabPane();
                resetDescriptionAndSolutionTabPane(false);
                viewUpdater.updateReportViewInAWTThread(findingModel);
            }, ProgressIndicatorProvider.getGlobalProgressIndicator());
        }
    }

    public interface CallStepChangeListener {
        void callStepChanged(FindingNode callStep, boolean openInEditor);
    }

    public interface ReportFindingSelectionChangeListener {
        void reportFindingSelectionChanged(FindingNode callStep);
    }

    public void addCallStepChangeListener(CallStepChangeListener listener) {
        this.callStepChangeListeners.add(listener);
    }

    public void addReportFindingSelectionChangeListener(ReportFindingSelectionChangeListener listener) {
        this.reportFindingSelectionChangeListeners.add(listener);
    }

    public void initialize() {
        initCweIdLink();
        initReportTable();
        initCallHierarchyTree();
        initCallStepDetailTable();
    }

    private void initCweIdLink() {
        setCweId(null);
        cweIdLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cweIdLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Integer cweId = context.currentSelectedCweId;
                if (cweId == null) {
                    return;
                }
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop == null) {
                    return;
                }
                String uriAsText = createMitreCweDescriptionLink(cweId);

                try {
                    URI uri = new URI(uriAsText);
                    desktop.browse(uri);
                } catch (Exception exception) {
                    context.errorLog.error("Was not able to open URI:" + uriAsText, exception);
                }
            }
        });
    }

    @NotNull
    private String createMitreCweDescriptionLink(Integer cweId) {
        return "https://cwe.mitre.org/data/definitions/" + cweId + ".html";
    }

    private void initCallStepDetailTable() {
        callStepDetailTable.setModel(new SecHubTableModel("Step", "Line", "Column", "Location"));
        resetCallHierarchyStepTable();
    }

    public void resetCallHierarchyStepTable() {
        /* resize headers */
        TableColumnModel columnModel = callStepDetailTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(400);
    }

    public FindingRenderDataProvider getRenderDataProvider() {
        return context.findingRenderDataProvider;
    }

    private void initCallHierarchyTree() {
        callHierarchyTree.setRootVisible(false);
        callHierarchyTree.setModel(new SechubTreeModel());
        callHierarchyTree.addTreeSelectionListener((event) -> {

            SecHubTreeNode selected = (SecHubTreeNode) callHierarchyTree.getLastSelectedPathComponent();
            if (selected == null) {
                return;
            }
            FindingNode callStep = selected.getFindingNode();

            showCallStep(callStep, false);
        });

        callHierarchyTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SecHubTreeNode selected = (SecHubTreeNode) callHierarchyTree.getLastSelectedPathComponent();
                if (selected == null) {
                    return;
                }
                FindingNode callStep = selected.getFindingNode();
                if (e.getClickCount() > 1) {
                    showCallStep(callStep, true);
                }
            }
        });

        callHierarchyTree.setMinimumSize(new Dimension(300, 200));

    }

    private void initReportTable() {
        findingModel = new FindingModel();
        initTableModelAndRowSorting();

        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn markFalsePositiveColumn = reportTable.getColumn(COLUMN_NAME_MARK_FALSE_POSITIVE);
        JCheckBox markFalsePositiveColumnHeader = createMarkFalsePositiveColumnHeader(markFalsePositiveColumn);
        markFalsePositiveColumn.setWidth(60);
        markFalsePositiveColumn.setMaxWidth(60);
        markFalsePositiveColumn.setHeaderRenderer(new CenteredCheckBoxHeaderRenderer(markFalsePositiveColumnHeader));
        markFalsePositiveColumn.setCellRenderer(new FalsePositiveCellRenderer());
        markFalsePositiveColumn.setCellEditor(new FalsePositiveCellEditor());

        TableColumn idColumn = reportTable.getColumn(COLUMN_NAME_ID);
        idColumn.setWidth(60);
        idColumn.setMaxWidth(60);

        TableColumn severityColumn = reportTable.getColumn(COLUMN_NAME_SEVERITY);
        severityColumn.setWidth(100);
        severityColumn.setMaxWidth(100);

        TableColumn typeColumn = reportTable.getColumn(COLUMN_NAME_TYPE);
        typeColumn.setWidth(100);
        typeColumn.setMaxWidth(100);

        reportTable.getSelectionModel().addListSelectionListener((event) -> {
            handleReportTableSelection(false);
        });
        reportTable.setMinimumSize(new Dimension(600, 300));

        reportTable.getColumn(COLUMN_NAME_TYPE).setCellRenderer(new ReportScanTypeIconTableCellRenderer());
        reportTable.getColumn(COLUMN_NAME_SEVERITY).setCellRenderer(new ReportSeverityTableCellRenderer());

        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedCol = reportTable.columnAtPoint(e.getPoint());
                if (clickedCol != markFalsePositiveColumn.getModelIndex() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                    handleReportTableSelection(true);
                }
            }
        });

        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedRow = reportTable.rowAtPoint(e.getPoint());
                int clickedCol = reportTable.columnAtPoint(e.getPoint());
                int markFalsePositiveCol = markFalsePositiveColumn.getModelIndex();
                int findingIdCol = idColumn.getModelIndex();
                int scanTypeCol = typeColumn.getModelIndex();
                ScanType scanType = (ScanType) reportTable.getValueAt(clickedRow, scanTypeCol);

                if (clickedCol == markFalsePositiveColumn.getModelIndex() && ScanType.WEB_SCAN != scanType && clickedRow != -1) {
                    Boolean currentValue = getCurrentCheckboxValue(clickedRow, markFalsePositiveCol);

                    if (currentValue == null) {
                        return;
                    }

                    /* invert the value to toggle checkbox */
                    boolean isChecked = !currentValue;

                    UUID jobUUID = findingModel.getJobUUID();
                    int findingId = (int) reportTable.getValueAt(clickedRow, findingIdCol);
                    FalsePositiveTableModel model = new FalsePositiveTableModel(false, scanType);
                    model.setChecked(!currentValue);
                    reportTable.setValueAt(model, clickedRow, clickedCol);

                    if (isChecked) {
                        cacheManager.markFalsePositive(jobUUID, findingId);
                    } else {
                        cacheManager.unmarkFalsePositive(jobUUID, findingId);
                    }
                }
            }
        });
    }

    private @Nullable Boolean getCurrentCheckboxValue(int clickedRow, int markFalsePositiveCol) {
        Object valueAt = reportTable.getValueAt(clickedRow, markFalsePositiveCol);
        boolean currentValue;
        if (valueAt instanceof Boolean) {
            currentValue = (boolean) valueAt;
        } else if (valueAt instanceof FalsePositiveTableModel falsePositiveTableModel) {
            currentValue = falsePositiveTableModel.isChecked();
        } else {
            errorLog.error("Unexpected value at mark false positive column: " + valueAt);
            return null;
        }
        return currentValue;
    }

    private @NotNull JCheckBox createMarkFalsePositiveColumnHeader(TableColumn markFalsePositiveColumn) {
        JCheckBox headerCheckBox = new JCheckBox();

        int falsePositiveColumn = markFalsePositiveColumn.getModelIndex();

        headerCheckBox.addActionListener(e -> {
            boolean isChecked = headerCheckBox.isSelected();
            for (int row = 0; row < reportTable.getRowCount(); row++) {
                FalsePositiveTableModel falsePositiveTableModel = (FalsePositiveTableModel) reportTable.getValueAt(row, falsePositiveColumn);

                if (ScanType.WEB_SCAN != falsePositiveTableModel.getScanType() && !falsePositiveTableModel.isAlreadyMarkedAsFalsePositive()) {
                    falsePositiveTableModel.setChecked(isChecked);
                    reportTable.setValueAt(falsePositiveTableModel, row, falsePositiveColumn);
                    UUID jobUUID = findingModel.getJobUUID();
                    int findingIdColumn = reportTable.getColumn(COLUMN_NAME_ID).getModelIndex();
                    int findingId = (int) reportTable.getValueAt(row, findingIdColumn);

                    if (isChecked) {
                        cacheManager.markFalsePositive(jobUUID, findingId);
                    } else {
                        cacheManager.unmarkFalsePositive(jobUUID, findingId);
                    }
                }
            }
        });

        JTableHeader header = reportTable.getTableHeader();

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = reportTable.columnAtPoint(e.getPoint());
                if (col == falsePositiveColumn) {
                    headerCheckBox.setSelected(!headerCheckBox.isSelected());

                    for (ActionListener al : headerCheckBox.getActionListeners()) {
                        al.actionPerformed(new ActionEvent(headerCheckBox, ActionEvent.ACTION_PERFORMED, ""));
                    }

                    header.repaint();
                }
            }
        });

        return headerCheckBox;
    }

    private void initTableModelAndRowSorting() {

        SecHubTableModel tableModel = new SecHubTableModel(REPORT_TABLE_COLUMN_NAMES);
        TableRowSorter<SecHubTableModel> rowSorter = new TableRowSorter<>(tableModel);
        /* Disable sorting for first column (false positive) */
        rowSorter.setSortable(0, false);
        rowSorter.setComparator(1, (Comparator<Integer>) (o1, o2) -> o1 - o2);
        rowSorter.setComparator(2, (Comparator<Severity>) Enum::compareTo);
        reportTable.setModel(tableModel);
        reportTable.setRowSorter(rowSorter);
        rowSorter.toggleSortOrder(1); // initial sort on first column

        /* set headers */
        resetTablePresentation();
    }

    private void setReportTableElements(List<Object[]> elements) {
        TableModel model = reportTable.getModel();
        if (model instanceof SecHubTableModel) {
            SecHubTableModel tableModel = (SecHubTableModel) model;
            tableModel.setDataList(elements);
        } else {
            throw new IllegalStateException("Unsupported table model:" + model);
        }
        /* resize headers */
        resetTablePresentation();
    }

    public void resetTablePresentation() {
        TableColumnModel columnModel = reportTable.getColumnModel();
        int index = 0;
        columnModel.getColumn(index++).setPreferredWidth(50);
        columnModel.getColumn(index++).setPreferredWidth(80);
        columnModel.getColumn(index++).setPreferredWidth(80);
        columnModel.getColumn(index++).setPreferredWidth(100);
        columnModel.getColumn(index++).setPreferredWidth(400);

        reportTable.doLayout();
    }

    private void handleReportTableSelection(boolean doubleClick) {
        if (this.findingModel == null) {
            errorLog.error("No model available");
            return;
        }
        int rowViewIndex = reportTable.getSelectedRow();
        if (rowViewIndex == -1) {
            // cannot be selected in this case!
            return;
        }
        int row = reportTable.convertRowIndexToModel(rowViewIndex);
        int column = reportTable.convertColumnIndexToModel(1);
        Object obj = reportTable.getModel().getValueAt(row, column);
        if (obj == null) {
            errorLog.error("entry row in table is null!");
            return;
        }
        int id = Integer.parseInt(obj.toString());

        for (FindingNode finding : findingModel.getFindings()) {
            if (finding.getId() == id) {
                showFindingNode(finding, doubleClick);
                break;
            }
        }
    }

    private void setCweId(Integer cweId) {
        context.currentSelectedCweId = cweId;
        if (cweId == null) {
            cweIdLabel.setVisible(false);
            return;
        }
        cweIdLabel.setText("<html><a href=\"" + createMitreCweDescriptionLink(cweId) + "\">CWE-ID " + cweId + "</a></html>");
        cweIdLabel.setToolTipText("Open " + createMitreCweDescriptionLink(cweId) + " in external browser");
        cweIdLabel.setVisible(true);
    }

    public void showFindingNode(FindingNode findingNode, boolean openInEditor) {

        handleCWEId(findingNode);
        resetFindingNodeTabPane();
        resetDescriptionAndSolutionTabPane(findingNode != null);

        handleCallHierarchy(findingNode, openInEditor);
        handleWebRequest(findingNode);
        handleWebResponse(findingNode);
        handleAttack(findingNode);
    }

    public void resetFindingNodeTabPane() {
        // reset
        context.findingTypeDetailsTabbedPane.removeAll();
    }

    public void resetDescriptionAndSolutionTabPane(boolean visible) {
        // reset
        context.descriptionAndSolutionTabbedPane.setVisible(visible);
    }

    private void handleCallHierarchy(FindingNode findingNode, boolean openInEditor) {
        prepareCalHierarchyWhenNeccesary(findingNode, openInEditor);

        if (findingNode != null && findingNode.canBeShownInCallHierarchy()) {
            findingTypeDetailsTabbedPane.add("Call hierarchy", context.callHierarchyTabComponent);
        }
    }

    private void handleWebRequest(FindingNode findingNode) {
        if (findingNode != null && findingNode.canBeShownInWebRequest()) {
            context.webRequestTextArea.setText(webRequestDataProvider.getWebRequestDescription(findingNode.getSecHubFinding()));
            findingTypeDetailsTabbedPane.add("Request", context.componentFactory.createScrollPane(context.webRequestTabComponent));
        }
    }

    private void handleWebResponse(FindingNode findingNode) {
        if (findingNode != null && findingNode.canBeShownInWebResponse()) {
            context.webResponseTextArea.setText(webRequestDataProvider.getWebResponseDescription(findingNode.getSecHubFinding()));
            findingTypeDetailsTabbedPane.add("Response", context.componentFactory.createScrollPane(context.webResponseTabComponent));
        }

    }

    private void handleAttack(FindingNode findingNode) {
        if (findingNode != null && findingNode.canBeShownInAttack()) {
            JComponent attackTabComponent = context.attackTabComponent;
            // we must create a new scrollpane all time - otherwise tabbed pane makes
            // problems with viewport handling!
            findingTypeDetailsTabbedPane.add("Attack", context.componentFactory.createScrollPane(attackTabComponent));
            context.attackTextArea.setText(webRequestDataProvider.getWebAttackDescription(findingNode.getSecHubFinding()));
        }
    }

    private void prepareCalHierarchyWhenNeccesary(FindingNode findingNode, boolean openInEditor) {
        SechubTreeModel hierarchyTreeModel = (SechubTreeModel) callHierarchyTree.getModel();
        SecHubRootTeeNode newRootNode = new SecHubRootTeeNode();
        if (findingNode == null) {
            hierarchyTreeModel.setRoot(newRootNode);
            setCweId(null);
            showCallStep(null, false);
            return;
        }
        if (findingNode.canBeShownInCallHierarchy()) {
            buildCallHierarchyTreeNodes(newRootNode, findingNode);
            hierarchyTreeModel.setRoot(newRootNode);

            showCallStep(findingNode, openInEditor);
            callHierarchyTree.addSelectionInterval(0, 0);
        }

        /* inform listeners */
        for (ReportFindingSelectionChangeListener listener : reportFindingSelectionChangeListeners) {
            listener.reportFindingSelectionChanged(findingNode);
        }
    }

    private void handleCWEId(FindingNode findingNode) {
        if (findingNode != null) {
            setCweId(findingNode.getCweId());
        } else {
            setCweId(null);
        }
    }

    private void buildCallHierarchyTreeNodes(SecHubTreeNode parent, FindingNode findingNode) {
        SecHubTreeNode treeNode = new SecHubTreeNode(findingNode);
        parent.add(treeNode);

        for (FindingNode child : findingNode.getChildren()) {
            buildCallHierarchyTreeNodes(parent, child);
        }
    }

    private void showCallStep(FindingNode callStep, boolean openInEditor) {
        /* show in detail table */
        SecHubTableModel callStepTableModel = (SecHubTableModel) callStepDetailTable.getModel();
        callStepTableModel.removeAllRows();

        if (callStep != null) {
            Object[] rowData = new Object[] { callStep.getCallStackStep(), callStep.getLine(), callStep.getColumn(), callStep.getLocation() };
            callStepTableModel.addRow(rowData);
        }
        callStepTableModel.fireTableDataChanged();

        /* inform listeners */
        for (CallStepChangeListener listener : callStepChangeListeners) {
            listener.callStepChanged(callStep, openInEditor);
        }
    }

    public void setFindingModel(FindingModel findingModel) {
        List<Object[]> elements = new ArrayList<>();
        /* fill with new rows */
        List<FindingNode> findings = findingModel.getFindings();
        for (FindingNode finding : findings) {
            if (finding == null) {
                continue;
            }

            FalsePositiveTableModel falsePositiveTableModel;

            if (finding.isMarkedAsFalsePositive()) {
                falsePositiveTableModel = new FalsePositiveTableModel(true, finding.getScanType());
            } else {
                falsePositiveTableModel = cacheManager.findFalsePositive(findingModel.getJobUUID(), finding.getId())
                        .map(falsePositive -> new FalsePositiveTableModel(false, true, finding.getScanType()))
                        .orElse(new FalsePositiveTableModel(false, false, finding.getScanType()));

            }
            Object[] rowData = new Object[] { falsePositiveTableModel, finding.getId(), finding.getSeverity(), finding.getScanType(), finding.getName(), finding.getLocation() };
            elements.add(rowData);
        }

        this.findingModel = findingModel;
        setReportTableElements(elements);
    }

    private static @NotNull SecHubAccess secHubAccess() {
        return SecHubAccessFactory.create();
    }

    private class ReportScanTypeIconTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            // noinspection unchecked
            if (value instanceof ScanType scanType) {
                Icon icon = getRenderDataProvider().getIconForScanType(scanType);
                String text = getRenderDataProvider().getTextForScanType(scanType);
                setIcon(icon);
                setText(text);

            } else {
                setIcon(null);
                setText(null);
            }
            return this;
        }

    }

    private class ReportSeverityTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            // noinspection unchecked
            if (value instanceof Severity) {
                Severity severity = (Severity) value;
                String text = getRenderDataProvider().getTextForSeverity(severity);
                setText(text);

            } else {
                setIcon(null);
                setText(null);
            }
            return this;
        }

    }

    private static class CenteredCheckBoxHeaderRenderer implements TableCellRenderer {
        private final JCheckBox checkBox;

        public CenteredCheckBoxHeaderRenderer(JCheckBox checkBox) {
            this.checkBox = checkBox;
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.add(checkBox);
            panel.setOpaque(false);
            return panel;
        }
    }

    private static class FalsePositiveCellRenderer implements TableCellRenderer {
        private static final JCheckBox checkBox = new JCheckBox();
        private static final JLabel alreadyMarkedLabel;
        private static final JLabel webScanLabel;

        static {
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);

            alreadyMarkedLabel = new JLabel("✔");
            alreadyMarkedLabel.setToolTipText("Already marked as false positive");
            alreadyMarkedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            alreadyMarkedLabel.setFont(alreadyMarkedLabel.getFont().deriveFont(Font.BOLD, 14f));
            alreadyMarkedLabel.setForeground(JBColor.BLUE);

            webScanLabel = new JLabel(AllIcons.General.Web);
            webScanLabel.setToolTipText("Plugin does not support marking web scan findings. Please use the Web UI to web scan findings.");
            webScanLabel.setHorizontalAlignment(SwingConstants.CENTER);
            webScanLabel.setFont(webScanLabel.getFont().deriveFont(Font.BOLD, 14f));
            webScanLabel.setForeground(JBColor.BLUE);
        }

        @Override
        /* @formatter:off */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            /* @formatter:on */

            checkBox.setSelected(false);
            checkBox.setEnabled(false);

            if (value instanceof FalsePositiveTableModel falsePositiveTableModel) {

                if (falsePositiveTableModel.isAlreadyMarkedAsFalsePositive()) {
                    return alreadyMarkedLabel;
                }

                if (ScanType.WEB_SCAN == falsePositiveTableModel.getScanType()) {
                    return webScanLabel;
                }

                boolean isChecked = falsePositiveTableModel.isChecked();
                checkBox.setSelected(isChecked);
                checkBox.setEnabled(true);
            }

            return checkBox;
        }
    }

    private static class FalsePositiveCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JCheckBox checkBox = new JCheckBox();
        private FalsePositiveTableModel currentValue;

        public FalsePositiveCellEditor() {
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.addItemListener(e -> {
                if (currentValue == null || currentValue.isAlreadyMarkedAsFalsePositive()) {
                    return;
                }

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    currentValue.setChecked(true);
                } else if (currentValue != null) {
                    currentValue.setChecked(false);
                }

                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentValue = (FalsePositiveTableModel) value;
            checkBox.setSelected(currentValue.isChecked());
            checkBox.setEnabled(!currentValue.isAlreadyMarkedAsFalsePositive());
            return checkBox;
        }

        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }
    }

    private static class MarkFalsePositivesDialog extends DialogWrapper {
        private final JPanel panel = new JPanel(new BorderLayout());
        private final JRadioButton[] radioButtons = new JRadioButton[5];
        /* 3 rows, 20 columns */
        private final JTextArea commentArea = new JBTextArea(5, 20);

        private MarkFalsePositivesDialog() {
            super(true);
            createComponents();
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return panel;
        }

        public String getSelectedReason() {
            for (JRadioButton btn : radioButtons) {
                if (btn.isSelected()) return btn.getText();
            }
            return null;
        }

        public String getComment() {
            return commentArea.getText();
        }

        private void createComponents() {
            setTitle("Mark Selected Findings As False Positives");

            JPanel radioPanel = new JPanel(new GridLayout(0, 1));
            String[] options = {
                    "A fix has already been started",
                    "No bandwidth to fix this",
                    "Risk is tolerable to this project",
                    "This alert is inaccurate or incorrect",
                    "Vulnerable code is not actually used"
            };
            ButtonGroup group = new ButtonGroup();
            for (int index = 0; index < options.length; index++) {
                radioButtons[index] = new JRadioButton(options[index]);
                group.add(radioButtons[index]);
                radioPanel.add(radioButtons[index]);
            }
            radioButtons[0].setSelected(true);

            JScrollPane commentScrollPane = new JBScrollPane(commentArea);
            JPanel commentPanel = new JPanel(new BorderLayout());
            commentPanel.add(new JLabel("Comment:"), BorderLayout.NORTH);
            commentPanel.add(commentScrollPane, BorderLayout.CENTER);
            commentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

            panel.add(radioPanel, BorderLayout.NORTH);
            panel.add(commentPanel, BorderLayout.CENTER);
            panel.setPreferredSize(new Dimension(350, 200));
        }
    }

    private static class ProjectIdChooserDialog extends DialogWrapper {
        private final JComboBox<String> comboBox;

        public ProjectIdChooserDialog(String[] projectIds) {
            super(true);
            setTitle("Select Project");
            comboBox = new JComboBox<>(projectIds);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel();
            panel.add(new JLabel("Select the project this report belongs to:"));
            panel.add(comboBox);
            return panel;
        }

        public String getSelectedProjectId() {
            return (String) comboBox.getSelectedItem();
        }
    }
}
