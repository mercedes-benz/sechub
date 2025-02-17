// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.plugin.model.FindingModel;
import com.mercedesbenz.sechub.plugin.model.FindingNode;
import com.mercedesbenz.sechub.plugin.model.SecHubFindingoWebScanDataProvider;
import com.mercedesbenz.sechub.plugin.util.ErrorLog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;
import java.util.*;

/**
 * Because its very inconvenient and slow to test and develop the toolwindow
 * this support class was established. There exists a simple
 * SecHubToolWindowUISupportTestMain application class which can be called to
 * tweak ui without need to start intellj all time
 */
public class SecHubToolWindowUISupport {
    private static final String COLUMN_NAME_TYPE = "Type";
    private static final String COLUMN_NAME_SEVERITY = "Severity";
    private static final String COLUMN_NAME_ID = "Id";
    private static final String COLUMN_NAME_NAME = "Name";
    private static final String COLUMN_NAME_LOCATION = "Location";
    private static final String[] REPORT_TABLE_COLUMN_NAMES = {COLUMN_NAME_ID, COLUMN_NAME_SEVERITY, COLUMN_NAME_TYPE, COLUMN_NAME_NAME, COLUMN_NAME_LOCATION};
    private final JTable reportTable;
    private final JTree callHierarchyTree;
    private final JTable callStepDetailTable;
    private final ErrorLog errorLog;
    private final JLabel cweIdLabel;
    private final SecHubToolWindowUIContext context;
    private final JTabbedPane findingTypeDetailsTabbedPane;

    private FindingModel findingModel;
    private Set<CallStepChangeListener> callStepChangeListeners;
    private Set<ReportFindingSelectionChangeListener> reportFindingSelectionChangeListeners;

    private SecHubFindingoWebScanDataProvider webRequestDataProvider = new SecHubFindingoWebScanDataProvider();


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

    public interface CallStepChangeListener {
        public void callStepChanged(FindingNode callStep, boolean openInEditor);
    }

    public interface ReportFindingSelectionChangeListener {
        public void reportFindingSelectionChanged(FindingNode callStep);
    }

    public void addCallStepChangeListener(CallStepChangeListener listener) {
        this.callStepChangeListeners.add(listener);
    }

    public void removeCallStepChangeListener(CallStepChangeListener listener) {
        this.callStepChangeListeners.remove(listener);
    }

    public void addReportFindingSelectionChangeListener(ReportFindingSelectionChangeListener listener) {
        this.reportFindingSelectionChangeListeners.add(listener);
    }

    public void removeReportFindingSelectionChangeListener(ReportFindingSelectionChangeListener listener) {
        this.reportFindingSelectionChangeListeners.remove(listener);
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
        reportTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                    handleReportTableSelection(true);
                }
            }

        });
        reportTable.getSelectionModel().addListSelectionListener((event) -> {
            handleReportTableSelection(false);
        });
        reportTable.setMinimumSize(new Dimension(600, 300));

        reportTable.getColumn(COLUMN_NAME_TYPE).setCellRenderer(new ReportScanTypeIconTableCellRenderer());
        reportTable.getColumn(COLUMN_NAME_SEVERITY).setCellRenderer(new ReportSeverityTableCellRenderer());
    }

    private void initTableModelAndRowSorting() {

        SecHubTableModel tableModel = new SecHubTableModel(REPORT_TABLE_COLUMN_NAMES);
        TableRowSorter<SecHubTableModel> rowSorter = new TableRowSorter<>(tableModel);
        rowSorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        rowSorter.setComparator(1, new Comparator<Severity>() {
            @Override
            public int compare(Severity o1, Severity o2) {
                return o1.compareTo(o2);
            }
        });
        reportTable.setModel(tableModel);
        reportTable.setRowSorter(rowSorter);
        rowSorter.toggleSortOrder(0); // initial sort on first column

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
        Object obj = reportTable.getModel().getValueAt(row, 0);
        if (obj == null) {
            errorLog.error("entry row in table is null!");
            return;
        }
        Integer integer = Integer.valueOf(obj.toString());
        int id = integer.intValue();

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
            // we must create a new scrollpane all time - otherwise tabbed pane makes problems with viewport handling!
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
            Object[] rowData = new Object[]{callStep.getCallStackStep(), callStep.getLine(), callStep.getColumn(), callStep.getLocation()};
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
            Object[] rowData = new Object[]{finding.getId(), finding.getSeverity(), finding.getScanType(), finding.getName(),
                    finding.getLocation()};
            elements.add(rowData);
        }

        this.findingModel = findingModel;
        setReportTableElements(elements);
    }

    private class ReportScanTypeIconTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            //noinspection unchecked
            if (value instanceof ScanType) {
                ScanType scanType = (ScanType) value;
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

    ;

    private class ReportSeverityTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            //noinspection unchecked
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

    ;
}
