// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.report;

import static com.mercedesbenz.sechub.util.EclipseUtil.getActivePage;
import static com.mercedesbenz.sechub.util.EclipseUtil.getSharedImageDescriptor;
import static com.mercedesbenz.sechub.util.EclipseUtil.safeAsyncExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.SecHubActivator;
import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.callhierarchy.SecHubCallHierarchyView;
import com.mercedesbenz.sechub.component.DragAndDropCallback;
import com.mercedesbenz.sechub.component.DragAndDropData;
import com.mercedesbenz.sechub.component.DragAndDropSupport;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.model.SecHubReportToFindingModelTransformer;
import com.mercedesbenz.sechub.model.WorkspaceFindingNodeLocator;
import com.mercedesbenz.sechub.provider.FirstFindingNodesOnlyFindingModelTreeContentProvider;
import com.mercedesbenz.sechub.provider.findings.FindingNodeColumLabelProviderBundle;
import com.mercedesbenz.sechub.server.SecHubServerContext;
import com.mercedesbenz.sechub.server.SecHubServerView;
import com.mercedesbenz.sechub.util.EclipseUtil;
import com.mercedesbenz.sechub.util.Logging;
import com.mercedesbenz.sechub.util.TrafficLightImageResolver;
import com.mercedesbenz.sechub.webfinding.SecHubWebFindingView;

public class SecHubReportView extends ViewPart {

	public static final String ID = "com.mercedesbenz.sechub.views.SecHubReportView";
	private static final SecHubReportToFindingModelTransformer transformer = new SecHubReportToFindingModelTransformer();

	private Text reportUUID;
	private Label trafficLight;
	private Label additionalInfo;
	private FindingNodeColumLabelProviderBundle columnProviders = new FindingNodeColumLabelProviderBundle();

	private TreeViewer treeViewer;
	private DragAndDropSupport dragAndDropSupport = new DragAndDropSupport();

	@Inject
	IWorkbench workbench;

	WorkspaceFindingNodeLocator locator;

	private Action removeAllReportData;

	private ImportSecHubReportAction importAction;

	private OpenSecHubServerViewAction openServerViewAction;

	private Composite composite;

	private ReportInfoAction showInformationAction;

	private Action markFalsePositivesAction;

	private Action unmarkFalsePositivesAction;
	
	private SecHubReport currentReport;
	
	private OpenFindingDetailsAction openDetailsAction;

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {

		locator = new WorkspaceFindingNodeLocator(workbench);

		composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		trafficLight = new Label(composite, SWT.NONE);
		trafficLight.setText("");

		reportUUID = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
		reportUUID.setText("");

		additionalInfo = new Label(composite, SWT.NONE);
		additionalInfo.setText("");

		treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer.setContentProvider(new FirstFindingNodesOnlyFindingModelTreeContentProvider());
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		makeActions();
		createColumns();
		hookDoubleClickAction();
		contributeToActionBars();
		createContextMenu();

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
		
		recalculateActionsEnabledStateBySelection();
	}

	public FindingModel getModel() {
		return (FindingModel) treeViewer.getInput();
	}

	protected void removeAllReportData() {
		setReport(null);
	}

	private void makeActions() {
		removeAllReportData = new RemoveAllReportDataAction();
		importAction = new ImportSecHubReportAction();
		openServerViewAction = new OpenSecHubServerViewAction();

		showInformationAction = new ReportInfoAction(this);
		
		markFalsePositivesAction = new MarkFalsePositivesAction(this);
		unmarkFalsePositivesAction = new DeletekFalsePositivesByReportViewAction(this);
		
		openDetailsAction = new OpenFindingDetailsAction();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openServerViewAction);
		manager.add(importAction);
		manager.add(new Separator());
		manager.add(markFalsePositivesAction);
		manager.add(unmarkFalsePositivesAction);
		manager.add(new Separator());
		manager.add(openDetailsAction);
		manager.add(showInformationAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(openServerViewAction);
		manager.add(importAction);
		manager.add(new Separator());
		manager.add(markFalsePositivesAction);
		manager.add(unmarkFalsePositivesAction);
		manager.add(new Separator());
		manager.add(openDetailsAction);
		manager.add(showInformationAction);
		manager.add(new Separator());
		manager.add(removeAllReportData);
	}

	private void createColumns() {
		TreeViewerColumn fp = createTreeViewerColumn("", 50);
		fp.getColumn().setToolTipText("Additional info. e.g marked as false positive");
		fp.setLabelProvider(columnProviders.falsePositiveColumnLabelProvider);

		TreeViewerColumn id = createTreeViewerColumn("Id", 50);
		id.setLabelProvider(columnProviders.idLabelProvider);

		TreeViewerColumn severity = createTreeViewerColumn("Severity", 80);
		severity.setLabelProvider(columnProviders.severityLabelProvider);

		TreeViewerColumn type = createTreeViewerColumn("", 20);
		type.setLabelProvider(columnProviders.scanTypeLabelProvider);
		type.getColumn().setToolTipText("Scan type");

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
				openDetailsForFirstSelectedElement();
			}
		});
	}

	protected void openDetailsForFirstSelectedElement() {
		IStructuredSelection selectedFinding = treeViewer.getStructuredSelection();
		FindingNode finding = (FindingNode) selectedFinding.getFirstElement();

		showFindingInDetailView(finding);

	}

	private void showFindingInDetailView(FindingNode node) {

		if (node == null) {
			// just erase
			showFindingInCallHierarchyView(null);
			showFindingInWebFindingView(null);
			return;
		}

		SecHubFinding finding = node.getFinding();
		ScanType scanType = finding.getType();
		if (scanType == ScanType.WEB_SCAN) {

			showFindingInCallHierarchyView(null);
			showFindingInWebFindingView(node);
		} else {

			showFindingInWebFindingView(null);
			showFindingInCallHierarchyView(node);
		}

	}

	private void showFindingInCallHierarchyView(FindingNode node) {
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
					if (node != null) {
						subModel.setJobUUID(node.getJobUUID());
						page.activate(view); // we ensure view is shown
						subModel.getFindings().add(node);
					}

					callhierarchyView.update(subModel);
				}
			} catch (PartInitException pie) {
				Logging.logError("Was not able to show junit view", pie);
			}
		});

	}

	private void showFindingInWebFindingView(FindingNode node) {
		safeAsyncExec(() -> {
			try {
				IWorkbenchPage page = getActivePage();
				if (page == null) {
					return;
				}
				IViewPart view = page.findView(SecHubWebFindingView.ID);
				if (view == null) {
					/* create and show the result view if it isn't created yet. */
					view = page.showView(SecHubWebFindingView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
				}
				if (view instanceof SecHubWebFindingView) {
					SecHubWebFindingView callhierarchyView = (SecHubWebFindingView) view;

					FindingModel subModel = new FindingModel();
					if (node != null) {
						subModel.setJobUUID(node.getJobUUID());
						page.activate(view); // we ensure view is shown
						subModel.getFindings().add(node);
					}

					callhierarchyView.update(subModel);
				}
			} catch (PartInitException pie) {
				Logging.logError("Was not able to show junit view", pie);
			}
		});

	}

	public void setReport(SecHubReport report) {
		this.currentReport = report;

		rebuildFindingModelAndUpdateUI(true);

	}

	private void rebuildFindingModelAndUpdateUI(boolean selectFirstElement) {
		FindingModel model = null;
		if (currentReport != null) {
			model = transformer.transform(currentReport, SecHubServerContext.INSTANCE.getSelectedProjectId());
		}
		treeViewer.setInput(model);

		if (model != null) {
			this.reportUUID.setText("" + model.getJobUUID());
			this.trafficLight.setImage(TrafficLightImageResolver.resolveImage(model.getTrafficLight()));
			this.trafficLight.setToolTipText("The traffic light for this report is " + model.getTrafficLight());

			String info = model.getStatus() + " - " + model.getFindingCount() + " findings";
			String projectId = model.getProjectId();
			if (projectId != null) {
				info = info + " for project '" + projectId + "'";
			}

			this.additionalInfo.setText(info);
		} else {
			this.reportUUID.setText("No report available");
			this.trafficLight.setImage(null);
			this.trafficLight.setToolTipText("No traffic light available");
			this.additionalInfo.setText("");
		}
		composite.layout();

		if (selectFirstElement) {
			// select first element - will also refresh call hierarchy view
			openDetailsForFirstSelectedElement();
		}
	}

	public void importReport() {
		SecHubReportImportDialog dialog = new SecHubReportImportDialog(getSite().getShell());
		dialog.open();
	}

	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.add(markFalsePositivesAction);
		menuManager.add(unmarkFalsePositivesAction);
		menuManager.add(new Separator());
		menuManager.add(openDetailsAction);

		Menu menu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				recalculateActionsEnabledStateBySelection();
			}

		});

		getSite().registerContextMenu(menuManager, treeViewer);
	}
	
	private void recalculateActionsEnabledStateBySelection() {
		boolean markFalsePositiveActive = true;
		boolean unmarkFalsePositiveActive = true;
		boolean openDetailsActionActive=true;
		
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		if(selection.isEmpty()) {
			markFalsePositiveActive=false;
			unmarkFalsePositiveActive=false;
			openDetailsActionActive=false;
		}else {
			for(Object selected: selection) {
				if(selected instanceof FindingNode) {
					FindingNode node = (FindingNode) selected;
					markFalsePositiveActive = markFalsePositiveActive && !node.isFalsePositive();
					unmarkFalsePositiveActive = unmarkFalsePositiveActive && node.isFalsePositive();
				}
			}
		}
		markFalsePositivesAction.setEnabled(markFalsePositiveActive);
		unmarkFalsePositivesAction.setEnabled(unmarkFalsePositiveActive);
		openDetailsAction.setEnabled(openDetailsActionActive);
	}

	public void recalculateFalsePositives() {
		transformer.updateFalsePositiveInfo(getModel());
		treeViewer.refresh();
		
		recalculateActionsEnabledStateBySelection();
	}

	private class OpenSecHubServerViewAction extends Action {
		private OpenSecHubServerViewAction() {
			setText("Open job from server");
			setToolTipText("Open job from SecHub server");
			setImageDescriptor(EclipseUtil.createDescriptor("icons/load-from-server.png"));
		}

		public void run() {
			IWorkbenchPage page = EclipseUtil.getActivePage();
			if (page == null) {
				throw new IllegalStateException("Workbench page not found");
			}
			IViewPart view = page.findView(SecHubServerView.ID);
			/* create and show the server view if it isn't created yet. */
			if (view == null) {
				try {
					view = page.showView(SecHubServerView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) {
					Logging.logError("Wasn't able create new SecHub server view", e);
					return;
				}

			}
			if (view instanceof SecHubServerView serverView) {
				serverView.searchJobDirectly();
			}
		}
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

	public static void clear() {
		update(null, false);
	}

	public static void update(SecHubReport report, boolean activate) {

		SecHubReportView reportView = findReportViewOrNull(activate);
		if (reportView != null) {
			reportView.setReport(report);
		}
	}

	public static void refreshFalsePositives() {
		SecHubReportView reportView = findReportViewOrNull(false);
		if (reportView != null) {
			reportView.recalculateFalsePositives();
		}
	}

	private static SecHubReportView findReportViewOrNull(boolean activate) {
		IWorkbenchPage page = EclipseUtil.getActivePage();
		if (page == null) {
			throw new IllegalStateException("Workbench page not found");
		}
		IViewPart view = page.findView(SecHubReportView.ID);

		/* create and show the result view if it isn't created yet. */
		if (view == null) {
			try {
				view = page.showView(SecHubReportView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
			} catch (PartInitException e) {
				Logging.logError("Wasn't able create new SecHub report view", e);
				return null;
			}

		}
		if (!(view instanceof SecHubReportView)) {
			throw new IllegalStateException("SecHub report view not found");
		}

		SecHubReportView reportView = (SecHubReportView) view;
		if (activate) {
			page.activate(reportView); // ensure report view is shown
		}
		return reportView;
	}

	public SecHubReport getCurrentReport() {
		return currentReport;
	}

	public List<Integer> fetchSelectedFindingIds() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		
		List<Integer> list = new ArrayList<Integer>();

		for (Object element : selection.toArray()) {
			if (element instanceof FindingNode) {
				FindingNode node = (FindingNode) element;
				SecHubFinding finding = node.getFinding();
				if (finding==null || ScanType.WEB_SCAN.equals(finding.getType())) {
					/* we just filter web scans - currently not supported*/
					continue;
				}
				list.add(node.getId());
			}
		}
		return list;
	}
	
	public class OpenFindingDetailsAction extends Action {
		
		public OpenFindingDetailsAction() {
			setText("Open details");
			setToolTipText("Open finding in details view\nHint: You can also double click on table entry");
			setImageDescriptor(EclipseUtil.createDescriptor("icons/details.png"));
		}
		
		@Override
		public void run() {
			openDetailsForFirstSelectedElement();
		}
		
	}
}
