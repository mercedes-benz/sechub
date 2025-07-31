// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.callhierarchy;

import static com.mercedesbenz.sechub.util.EclipseUtil.getSharedImageDescriptor;

import javax.inject.Inject;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.model.WorkspaceFindingNodeLocator;
import com.mercedesbenz.sechub.provider.CallHierarchyLabelProvider;
import com.mercedesbenz.sechub.provider.FindingModelTreeContentProvider;
import com.mercedesbenz.sechub.provider.OnlyInputElementItselfTreeContentProvider;
import com.mercedesbenz.sechub.provider.findings.FindingNodeColumLabelProviderBundle;
import com.mercedesbenz.sechub.util.BrowserUtil;
import com.mercedesbenz.sechub.util.CweLinkTextCreator;

/**
 * This view shows call hierarchy of sechub report entries - shall look similar
 * to JDT java call hierarchy, so two trees involved: left for navigation and
 * right for details
 * 
 * @author albert
 *
 */
public class SecHubCallHierarchyView extends ViewPart {

	public static final String ID = "com.mercedesbenz.sechub.views.SecHubCallHierarchyView";

	private FindingNodeColumLabelProviderBundle columnProviders = new FindingNodeColumLabelProviderBundle();

	private TreeViewer treeViewerRight;

	@Inject
	IWorkbench workbench;

	WorkspaceFindingNodeLocator locator;

	private TreeViewer treeViewerLeft;

	private Link linkDescriptionWithLinks;

	private MoveToStepBeforeAction stepBeforeAction;
	private MoveToNextStepAction stepNextAction;

	private MoveToFirstStepAction firstStepAction;

	private MoveToLastStepAction lastStepAction;

	private StyledText rightTreeDescriptionText;

	private Composite mainComposite;

	@Override
	public void createPartControl(Composite parent) {

		locator = new WorkspaceFindingNodeLocator(workbench);

		createComponents(parent);

		makeActions();
		createColumns();
		hookSelectionListener();
		contributeToActionBars();

	}

	private void createComponents(Composite parent) {
		Layout layout = parent.getLayout();
		if (layout instanceof FillLayout) {
			/*
			 * beautify output , we do not want space here - so output like JDT call
			 * hierarchy
			 */
			FillLayout gl = (FillLayout) layout;
			gl.marginWidth = -5;
		}
		/* main composite */
		GridLayout mainCompositeLayout = new GridLayout();
		mainCompositeLayout.marginLeft = 0;
		mainCompositeLayout.marginRight = 0;

		GridData mainCompositelayoutData1 = GridDataFactory.fillDefaults().grab(true, true).create();

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(mainCompositelayoutData1);
		mainComposite.setLayout(mainCompositeLayout);

		/* headline */
		GridData headlineCompositeLayoutData = GridDataFactory.fillDefaults().grab(true, false).create();
		FillLayout headlineCompositeLayout = new FillLayout();

		Composite headlineComposite = new Composite(mainComposite, SWT.NONE);
		headlineComposite.setLayout(headlineCompositeLayout);
		headlineComposite.setLayoutData(headlineCompositeLayoutData);

		linkDescriptionWithLinks = new Link(headlineComposite, SWT.NONE);
		linkDescriptionWithLinks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedText = e.text;
				if (selectedText==null) {
					return;
				}
				BrowserUtil.openInExternalBrowser(selectedText);

			}
		});
		linkDescriptionWithLinks.setText("");

		/* trees */
		GridData treeDividerSashFormLayoutData = GridDataFactory.fillDefaults().grab(true, true).create();
		SashForm treeDividerSashForm = new SashForm(mainComposite, SWT.HORIZONTAL);
		treeDividerSashForm.setLayoutData(treeDividerSashFormLayoutData);

		treeViewerLeft = new TreeViewer(treeDividerSashForm);
		treeViewerLeft.setContentProvider(new FindingModelTreeContentProvider());
		treeViewerLeft.setLabelProvider(new DelegatingStyledCellLabelProvider(new CallHierarchyLabelProvider()));

		Tree treeLeft = treeViewerLeft.getTree();
		treeLeft.setHeaderVisible(false);
		treeLeft.setLinesVisible(false);

		SashForm treeDivider2SashForm = new SashForm(treeDividerSashForm, SWT.VERTICAL);
		treeViewerRight = new TreeViewer(treeDivider2SashForm);
		treeViewerRight.setContentProvider(new OnlyInputElementItselfTreeContentProvider());

		Composite descriptionComposite = new Composite(treeDivider2SashForm, SWT.NONE);
		GridData descriptionCompositeLayoutData = GridDataFactory.fillDefaults().grab(true, true).create();
		descriptionComposite.setLayout(new GridLayout(1, false));
		descriptionComposite.setLayoutData(descriptionCompositeLayoutData);

		Label label = new Label(descriptionComposite, SWT.NONE);
		label.setText("Report source code:");
		GridData labelSourceLayoutData = GridDataFactory.fillDefaults().grab(true, false).create();
		label.setLayoutData(labelSourceLayoutData);

		GridData textSourceLayoutData = GridDataFactory.fillDefaults().grab(true, true).create();
		rightTreeDescriptionText = new StyledText(descriptionComposite, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		rightTreeDescriptionText.setEditable(false);
		rightTreeDescriptionText.setLayoutData(textSourceLayoutData);
		rightTreeDescriptionText.setAlwaysShowScrollBars(false);

		Tree treeRight = treeViewerRight.getTree();
		treeRight.setHeaderVisible(true);
		treeRight.setLinesVisible(true);

	}

	@Override
	public void setFocus() {
		treeViewerLeft.getControl().setFocus();
	}

	private void createColumns() {
		createRightTreeColumns();
	}

	private void makeActions() {
		stepBeforeAction = new MoveToStepBeforeAction();
		stepNextAction = new MoveToNextStepAction();
		firstStepAction = new MoveToFirstStepAction();
		lastStepAction = new MoveToLastStepAction();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(firstStepAction);
		manager.add(stepBeforeAction);
		manager.add(stepNextAction);
		manager.add(lastStepAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(firstStepAction);
		manager.add(stepBeforeAction);
		manager.add(stepNextAction);
		manager.add(lastStepAction);
	}

	private void createRightTreeColumns() {

		TreeViewerColumn step = createTreeViewerColumn(treeViewerRight, "Step", 40);
		step.setLabelProvider(columnProviders.stepLabelProvider);

		TreeViewerColumn line = createTreeViewerColumn(treeViewerRight, "Line", 60);
		line.setLabelProvider(columnProviders.lineLabelProvider);

		TreeViewerColumn column = createTreeViewerColumn(treeViewerRight, "Column", 60);
		column.setLabelProvider(columnProviders.columnLabelProvider);

		TreeViewerColumn location = createTreeViewerColumn(treeViewerRight, "Location", 200);
		location.setLabelProvider(columnProviders.locationLabelProvider);

	}

	private TreeViewerColumn createTreeViewerColumn(TreeViewer viewer, String title, int width) {
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		TreeColumn column = treeViewerColumn.getColumn();
		column.setWidth(width);
		column.setText(title);

		return treeViewerColumn;
	}

	private void hookSelectionListener() {
		/* on selection just show details */
		treeViewerLeft.addSelectionChangedListener(event -> {
			ISelection selection = treeViewerLeft.getSelection();
			if (!(selection instanceof IStructuredSelection)) {
				return;
			}

			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (!(element instanceof FindingNode)) {
				return;
			}
			selectNodeOnRightTree((FindingNode) element);

		});
		
		/* On double click open in editor - means 
		 * when no double click, we can easy look into findings without 
		 * opening. Interesting to show findings where we do not have the
		 * correct code - no annoying dialogs on every selection click in this case.
		 */
		treeViewerLeft.addDoubleClickListener(event ->{
			ISelection selection = treeViewerLeft.getSelection();
			if (!(selection instanceof IStructuredSelection)) {
				return;
			}

			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (!(element instanceof FindingNode)) {
				return;
			}
			searchInProjectsForFinding((FindingNode) element);
		});

	}

	private void searchInProjectsForFinding(FindingNode finding) {
		locator.searchInProjectsForFindingAndShowInEditor(finding);
	}

	private void selectNodeOnRightTree(FindingNode node) {
		treeViewerRight.setInput(node);
		String text = null;
		if (node != null) {
			text = node.getSource();
		} 
		if (text==null) {
			text=""; // may not be null
		}
		rightTreeDescriptionText.setText(text.trim());
	}

	public void update(FindingModel model) {
		treeViewerLeft.setInput(model);
		treeViewerLeft.expandAll();

		FindingNode finding = model.getFirstFinding();
		if (finding != null) {
			String headDescription =CweLinkTextCreator.createCweLinkTextWithInfos(finding);
			
			linkDescriptionWithLinks.setText(headDescription);
			treeViewerLeft.setSelection(new StructuredSelection(finding));
			mainComposite.layout();
			
		} else {
			linkDescriptionWithLinks.setText("");
			treeViewerRight.setInput(null);
			rightTreeDescriptionText.setText("");
		}
	}

	private abstract class AbstractNodeSelectionAction extends Action {

		public final void run() {
			ISelection selection = treeViewerLeft.getSelection();
			if (!(selection instanceof IStructuredSelection)) {
				return;
			}
			IStructuredSelection sse = (IStructuredSelection) selection;
			Object element = sse.getFirstElement();
			if (!(element instanceof FindingNode)) {
				return;
			}
			FindingNode node = (FindingNode) element;
			FindingNode nodeToSelect = calculateNextNodeToSelect(node);
			if (node == nodeToSelect) {
				/* same as before, so just ignore */
				return;
			}

			treeViewerLeft.setSelection(new StructuredSelection(nodeToSelect), true);
		}

		protected abstract FindingNode calculateNextNodeToSelect(FindingNode node);
	}

	private class MoveToFirstStepAction extends AbstractNodeSelectionAction {
		public MoveToFirstStepAction() {
			setText("Move to entry point (first step)");
			setToolTipText("Move to entry point (first step)");
			setImageDescriptor(getSharedImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV));
		}

		@Override
		protected FindingNode calculateNextNodeToSelect(FindingNode node) {
			while (node.getParent() != null) {
				node = node.getParent();
			}
			return node;
		}
	}

	private class MoveToLastStepAction extends AbstractNodeSelectionAction {
		public MoveToLastStepAction() {
			setText("Move to data sink(last step)");
			setToolTipText("Move to data sink(last step)");
			setImageDescriptor(getSharedImageDescriptor(IDE.SharedImages.IMG_OPEN_MARKER));
		}

		@Override
		protected FindingNode calculateNextNodeToSelect(FindingNode node) {
			while (!node.getChildren().isEmpty()) {
				node = node.getChildren().get(0);
			}
			return node;
		}
	}

	private class MoveToStepBeforeAction extends AbstractNodeSelectionAction {
		public MoveToStepBeforeAction() {
			setText("Move to step before");
			setToolTipText("Move to step before and show editor location");
			setImageDescriptor(getSharedImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		}

		@Override
		protected FindingNode calculateNextNodeToSelect(FindingNode node) {
			FindingNode parent = node.getParent();
			if (parent == null) {
				return node;
			}
			return parent;
		}
	}

	private class MoveToNextStepAction extends AbstractNodeSelectionAction {
		public MoveToNextStepAction() {
			setText("Move to next step");
			setToolTipText("Move to next step and show editor location");
			setImageDescriptor(getSharedImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		}

		@Override
		protected FindingNode calculateNextNodeToSelect(FindingNode node) {
			if (!node.hasChildren()) {
				return node;
			}
			FindingNode child = node.getChildren().get(0);
			return child;

		}

	}
}
