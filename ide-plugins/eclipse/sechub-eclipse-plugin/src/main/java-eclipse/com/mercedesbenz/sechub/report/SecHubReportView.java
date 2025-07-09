// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import static com.mercedesbenz.sechub.EclipseUtil.getActivePage;
import static com.mercedesbenz.sechub.EclipseUtil.getSharedImageDescriptor;
import static com.mercedesbenz.sechub.EclipseUtil.safeAsyncExec;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.Logging;
import com.mercedesbenz.sechub.SecHubActivator;
import com.mercedesbenz.sechub.callhierarchy.SecHubCallHierarchyView;
import com.mercedesbenz.sechub.component.DragAndDropCallback;
import com.mercedesbenz.sechub.component.DragAndDropData;
import com.mercedesbenz.sechub.component.DragAndDropSupport;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.model.WorkspaceFindingNodeLocator;
import com.mercedesbenz.sechub.provider.FirstFindingNodesOnlyFindingModelTreeContentProvider;
import com.mercedesbenz.sechub.provider.findings.FindingNodeColumLabelProviderBundle;

public class SecHubReportView extends ViewPart {

	public static final String ID = "com.mercedesbenz.sechub.views.SecHubReportView";

	private Label reportUUID;
	private Label trafficLight;
	private Label numberOfFindings;
	private FindingNodeColumLabelProviderBundle columnProviders = new FindingNodeColumLabelProviderBundle();

	private static final String REPORT_UUID_TEXT = "Scan result for Job: ";
	private static final String TRAFFIC_LIGHT_TEXT = "Traffic Light: ";
	private static final String NUMBER_OF_FINDINGS_TEXT = "Findings: ";

	private TreeViewer treeViewer;
	private DragAndDropSupport dragAndDropSupport = new DragAndDropSupport();

	@Inject
	IWorkbench workbench;

	WorkspaceFindingNodeLocator locator;

	private Action removeAllReportData;

	private RemoveSelectedReportDataAction removeSelectedReportData;

	private ImportSecHubReportAction importAction;

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {

		locator = new WorkspaceFindingNodeLocator(workbench);

		reportUUID = new Label(parent, SWT.NONE);
		reportUUID.setText(REPORT_UUID_TEXT);

		trafficLight = new Label(parent, SWT.NONE);
		trafficLight.setText(TRAFFIC_LIGHT_TEXT);

		numberOfFindings = new Label(parent, SWT.NONE);
		numberOfFindings.setText(NUMBER_OF_FINDINGS_TEXT);

		treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION); // we need FULL_SELECTION for windows SWT, otherwise only first column selectable...
		treeViewer.setContentProvider(new FirstFindingNodesOnlyFindingModelTreeContentProvider());

		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		makeActions();
		createColumns();
		hookDoubleClickAction();
		contributeToActionBars();

		GridLayoutFactory.fillDefaults().generateLayout(parent);

		dragAndDropSupport.enableDragAndDrop(tree, new DragAndDropCallback() {

			@Override
			public void drop(DragAndDropData context) {

				File reportFile = context.getFirstFileOrNull();
				if (reportFile == null) {
					return;
				}
				SecHubActivator.getDefault().getImporter().importAndDisplayReport(reportFile);

			}
		});
	}

	protected void removeAllReportData() {
		update(null);
	}

	private void makeActions() {
		removeAllReportData = new RemoveAllReportDataAction();
		removeSelectedReportData = new RemoveSelectedReportDataAction();
		importAction = new ImportSecHubReportAction();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void removeSelectedReportData() {
		ISelection selection = treeViewer.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection sse = (IStructuredSelection) selection;
		treeViewer.remove(sse.toArray());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(removeSelectedReportData);
		manager.add(removeAllReportData);
		manager.add(new Separator());
		manager.add(importAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(removeSelectedReportData);
		manager.add(removeAllReportData);
		manager.add(new Separator());
		manager.add(importAction);
	}

	private void createColumns() {
		TreeViewerColumn id = createTreeViewerColumn("Id", 50);
		id.setLabelProvider(columnProviders.idLabelProvider);

		TreeViewerColumn severity = createTreeViewerColumn("Severity", 80);
		severity.setLabelProvider(columnProviders.severityLabelProvider);

		TreeViewerColumn description = createTreeViewerColumn("Name", 220);
		description.setLabelProvider(columnProviders.descriptionLabelProvider);

		TreeViewerColumn location = createTreeViewerColumn("Location", 200);
		location.setLabelProvider(columnProviders.fileNameLabelProvider);

		TreeViewerColumn line = createTreeViewerColumn("Line", 60);
		line.setLabelProvider(columnProviders.lineLabelProvider);

	}

	private TreeViewerColumn createTreeViewerColumn(String title, int width) {
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn column = treeViewerColumn.getColumn();
		column.setWidth(width);
		column.setText(title);

		return treeViewerColumn;
	}

	private void hookDoubleClickAction() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selectedFinding = treeViewer.getStructuredSelection();
		FindingNode finding = (FindingNode) selectedFinding.getFirstElement();

		searchInProjectsForFinding(finding);

	}

	private void searchInProjectsForFinding(FindingNode finding) {
		showFindingInCallHierarchyView(finding);
	}

	private void showFindingInCallHierarchyView(FindingNode finding) {
		safeAsyncExec(() -> {
			try {
				IWorkbenchPage page = getActivePage();
				if (page == null) {
					return;
				}
				IViewPart view = page.findView(SecHubCallHierarchyView.ID);
				if (view == null) {
					/* create and show the result view if it isn't created yet. */
					view = page.showView(SecHubCallHierarchyView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
				}
				if (view instanceof SecHubCallHierarchyView) {
					SecHubCallHierarchyView callhierarchyView = (SecHubCallHierarchyView) view;

					FindingModel subModel = new FindingModel();
					if (finding != null) {
						page.activate(view); // we ensure view is shown
						subModel.getFindings().add(finding);
					}

					callhierarchyView.update(subModel);
				}
			} catch (PartInitException pie) {
				Logging.logError("Was not able to show junit view", pie);
			}
		});

	}

	public void update(FindingModel model) {
		treeViewer.setInput(model);

		if (model != null) {
			this.reportUUID.setText(REPORT_UUID_TEXT + model.getJobUUID());
			this.trafficLight.setText(TRAFFIC_LIGHT_TEXT + model.getTrafficLight());
			this.numberOfFindings.setText(NUMBER_OF_FINDINGS_TEXT + model.getFindingCount());
		} else {
			this.reportUUID.setText(REPORT_UUID_TEXT + "");
			this.trafficLight.setText(TRAFFIC_LIGHT_TEXT + "");
			this.numberOfFindings.setText(NUMBER_OF_FINDINGS_TEXT + 0);
		}
		// select first element - will also refresh call hierarchy view
		handleDoubleClick(null);

	}
	
	public void importReport() {
		SecHubReportImportDialog dialog = new SecHubReportImportDialog(getSite().getShell());
		dialog.open();
	}

	private class ImportSecHubReportAction extends Action {
		private ImportSecHubReportAction() {
			setText("Import SecHub report");
			setToolTipText("Import SecHub report \n(TIP:You can do this also with drag and drop into findings table!)");
			setImageDescriptor(EclipseUtil.createDescriptor("icons/import_wiz.png"));
		}

		public void run() {
			importReport();
		}
	}

	private class RemoveAllReportDataAction extends Action {
		private RemoveAllReportDataAction() {
			setText("Remove report data");
			setToolTipText("Remove all report data");
			setImageDescriptor(getSharedImageDescriptor(ISharedImages.IMG_ELCL_REMOVEALL));
		}

		public void run() {
			removeAllReportData();
		}
	}

	private class RemoveSelectedReportDataAction extends Action {
		private RemoveSelectedReportDataAction() {
			setText("Remove selected data");
			setToolTipText("Remove selected report data");
			setImageDescriptor(getSharedImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));
		}

		public void run() {
			removeSelectedReportData();
		}
	}

	

}
