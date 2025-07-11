// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webfinding;

import static com.mercedesbenz.sechub.util.EclipseUtil.getSharedImageDescriptor;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.provider.FirstFindingNodesOnlyFindingModelTreeContentProvider;
import com.mercedesbenz.sechub.report.SecHubReportImportDialog;
import com.mercedesbenz.sechub.util.TrafficLightImageResolver;

public class SecHubWebFindingView extends ViewPart {

	public static final String ID = "com.mercedesbenz.sechub.views.SecHubWebFindingView";

	private Text reportUUID;
	private Label trafficLight;
	private Label additionalInfo;

	private TreeViewer treeViewer;

	@Inject
	IWorkbench workbench;

	private Composite composite;

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {

//		composite = new Composite(parent, SWT.BORDER);
//		composite.setLayout(new GridLayout(3, false));
//		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

//		trafficLight = new Label(composite, SWT.NONE);
//		trafficLight.setText("");
//
//		reportUUID = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
//		reportUUID.setText("");
//
//		additionalInfo = new Label(composite, SWT.NONE);
//		additionalInfo.setText("");

		treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION); // we need FULL_SELECTION for windows SWT, otherwise only first column selectable...
		treeViewer.setContentProvider(new FirstFindingNodesOnlyFindingModelTreeContentProvider());
		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);
		
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		makeActions();
		createColumns();
		hookDoubleClickAction();
		contributeToActionBars();

		GridLayoutFactory.fillDefaults().generateLayout(parent);

	}

	protected void removeAllReportData() {
		update(null);
	}

	private void makeActions() {
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
	}

	private void createColumns() {
		TreeViewerColumn finding = createTreeViewerColumn("Finding", 50);
		finding.setLabelProvider(new ColumnLabelProvider());
//		id.setLabelProvider(columnProviders.idLabelProvider);

//		TreeViewerColumn severity = createTreeViewerColumn("Severity", 80);
//		severity.setLabelProvider(columnProviders.severityLabelProvider);

//		TreeViewerColumn type = createTreeViewerColumn("", 20);
//		type.setLabelProvider(columnProviders.scanTypeLabelProvider);
//		type.getColumn().setToolTipText("Scan type");
		
//		TreeViewerColumn description = createTreeViewerColumn("Name", 220);
//		description.setLabelProvider(columnProviders.descriptionLabelProvider);
//
//		TreeViewerColumn location = createTreeViewerColumn("Location", 200);
//		location.setLabelProvider(columnProviders.fileNameLabelProvider);
//
//		TreeViewerColumn line = createTreeViewerColumn("Line", 60);
//		line.setLabelProvider(columnProviders.lineLabelProvider);
		

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
	}

	public void update(FindingModel model) {
		treeViewer.setInput(model);

		if (model != null) {
			this.reportUUID.setText(""+model.getJobUUID());
			this.trafficLight.setImage(TrafficLightImageResolver.resolveImage(model.getTrafficLight()));
			this.trafficLight.setToolTipText("The traffic light for this report is "+model.getTrafficLight());
			
			String info = model.getStatus()+" - "+ model.getFindingCount()+" findings";
			String projectId = model.getProjectId();
			if (projectId!=null) {
				info = info + " for project '"+projectId+"'";
			}
			
			this.additionalInfo.setText(info);
		} else {
			this.reportUUID.setText("No report available");
			this.trafficLight.setImage(null);
			this.trafficLight.setToolTipText("No traffic light available");
			this.additionalInfo.setText("");
		}
		composite.layout();
		
		// select first element - will also refresh call hierarchy view
		handleDoubleClick(null);

	}
	
	public void importReport() {
		SecHubReportImportDialog dialog = new SecHubReportImportDialog(getSite().getShell());
		dialog.open();
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

}
