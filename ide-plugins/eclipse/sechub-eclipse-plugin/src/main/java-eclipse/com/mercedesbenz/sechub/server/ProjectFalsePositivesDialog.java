// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveEntry;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveJobData;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.api.internal.gen.model.FalsePositiveProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.report.SecHubReportView;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class ProjectFalsePositivesDialog extends Dialog {

	private static final String TABLE_ID_DEFINITION = "info";
	private static final String TABLE_ID_COMMENT = "comment";
	private static final String TABLE_ID_DATE = "date";
	private static final String TABLE_ID_AUTHOR = "author";

	
	private StyledText jsonText;
	private TreeViewer treeViewer;
	private FalsePositiveTreeContentProvider jobTreeProvider;
	private IAction unmarkFalsePositivesAction;

	public ProjectFalsePositivesDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Found following false positive definitions for project '"
				+ SecHubServerContext.INSTANCE.getSelectedProjectId() + "'");
		newShell.setSize(1024, 300);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		TabFolder tabFolder = new TabFolder(container, SWT.NONE);

		TabItem jobsTab = new TabItem(tabFolder, SWT.NONE);
		jobsTab.setText("Table");
		Composite jobsComposite = new Composite(tabFolder, SWT.NONE);

		GridLayout jobsLayout = new GridLayout();
		jobsLayout.marginWidth = 0;
		jobsLayout.marginHeight = 0;
		jobsComposite.setLayout(jobsLayout);

		treeViewer = new TreeViewer(jobsComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		createColumns();

		GridData treeLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeViewer.getControl().setLayoutData(treeLayoutData);

		jobTreeProvider = new FalsePositiveTreeContentProvider();
		treeViewer.setContentProvider(jobTreeProvider);
		treeViewer.setComparator(new FalsePositivesViewerComparator());

		ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		jobsTab.setControl(jobsComposite);

		TabItem jsonTab = new TabItem(tabFolder, SWT.NONE);
		jsonTab.setText("Json");
		Composite jsonComposite = new Composite(tabFolder, SWT.NONE);
		jsonComposite.setLayout(new FillLayout());
		jsonTab.setControl(jsonComposite);

		jsonText = new StyledText(jsonComposite, SWT.WRAP | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData messagesLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		jsonText.setLayoutData(messagesLayoutData);
		jsonText.setAlwaysShowScrollBars(false);

		SecHubServerContext instance = SecHubServerContext.INSTANCE;
		SecHubAccess access = instance.getAccessOrNull();
		if (access == null || !instance.isConnectedWithServer()) {
			jsonText.setText("No connection to server");
		} else {
			String projectId = instance.getSelectedProjectId();
			FalsePositiveProjectConfiguration data;
			try {
				data = access.fetchFalsePositiveProjectData(projectId);
				jsonText.setText(JSONConverter.get().toJSON(data, true));
				treeViewer.setInput(data);
			} catch (ApiException e) {
				EclipseUtil.showErrorDialog("Cannot show false positives", e);
			}

		}
		createActions();
		createContextMenu();

		return container;
	}

	private void createActions() {
		unmarkFalsePositivesAction = new UnmarkFalsePositiveAction();
	}

	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.add(unmarkFalsePositivesAction);

		Menu menu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);

	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Only create the OK button
		createButton(parent, Dialog.OK, "OK", true);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	private void createColumns() {

		TreeViewerColumn created = createTreeViewerColumn(treeViewer, "Created", 180);
		created.setLabelProvider(new JobFalsePositiveLabelProvider(TABLE_ID_DATE));

		TreeViewerColumn author = createTreeViewerColumn(treeViewer, "Author", 150);
		author.setLabelProvider(new JobFalsePositiveLabelProvider(TABLE_ID_AUTHOR));

		TreeViewerColumn info = createTreeViewerColumn(treeViewer, "", 20);
		info.setLabelProvider(new JobFalsePositiveInfoLabelProvider());

		TreeViewerColumn definition = createTreeViewerColumn(treeViewer, "Definition", 320);
		definition.setLabelProvider(new JobFalsePositiveLabelProvider(TABLE_ID_DEFINITION));

		TreeViewerColumn comment = createTreeViewerColumn(treeViewer, "Comment", 300);
		comment.setLabelProvider(new JobFalsePositiveLabelProvider(TABLE_ID_COMMENT));

	}

	private TreeViewerColumn createTreeViewerColumn(TreeViewer treeViewer, String title, int width) {
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn column = treeViewerColumn.getColumn();
		column.setWidth(width);
		column.setText(title);

		return treeViewerColumn;
	}

	private class UnmarkFalsePositiveAction extends Action {

		public UnmarkFalsePositiveAction() {
			setText("Unmark false positive");
		}

		@Override
		public void run() {

			ISelection selection = treeViewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object first = ((IStructuredSelection) selection).getFirstElement();
				if (first instanceof FalsePositiveEntry) {
					FalsePositiveEntry entry = (FalsePositiveEntry) first;
					FalsePositiveJobData jobData = entry.getJobData();
					if (jobData == null) {
						MessageDialog.openInformation(EclipseUtil.getActiveWorkbenchShell(),
								"Cannot remove false positive", "False positives can currently only removed for jobs");
						return;
					}
					SecHubServerContext serverContext = SecHubServerContext.INSTANCE;
					SecHubAccess access = serverContext.getAccessOrNull();
					if (access == null || !serverContext.isConnectedWithServer()) {
						MessageDialog.openError(EclipseUtil.getActiveWorkbenchShell(), "Cannot remove false positive",
								"No server connection");
						return;
					}

					String projectId = serverContext.getSelectedProjectId();

					boolean confirmed = MessageDialog.openConfirm(EclipseUtil.getActiveWorkbenchShell(),
							"Confirm unmark", "Are you sure you want to unmark the false positive ?");
					if (!confirmed) {
						return;
					}

					try {
						access.unmarkJobFalsePositives(projectId, jobData.getJobUUID(),
								List.of(jobData.getFindingId()));
						treeViewer.remove(entry);

						/* sync server context */
						serverContext.reloadFalsePositiveDataForCurrentProject();

						/* sync report view false positives */
						SecHubReportView.refreshFalsePositives();

					} catch (ApiException e) {
						ErrorDialog.openError(EclipseUtil.getActiveWorkbenchShell(), "FP unmark not possble",
								"Was not able to unmark false psoitive, because of communication error",
								Status.error("Failed", e));
						return;
					}

				}
			}

		}
	}


	private class JobFalsePositiveInfoLabelProvider extends ColumnLabelProvider {

		private static final Image IMAGE_INFO = EclipseUtil.getImage("/icons/info.png");

		@Override
		public Image getImage(Object element) {
			if (element instanceof FalsePositiveEntry) {
				return IMAGE_INFO;
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			return null;
		}

		@Override
		public String getToolTipText(Object element) {
			if (element instanceof FalsePositiveEntry) {
				return JSONConverter.get().toJSON(element, true);
			}
			return null;
		}
	}

	private class JobFalsePositiveLabelProvider extends ColumnLabelProvider {

		private String type;

		private JobFalsePositiveLabelProvider(String type) {
			this.type = type;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof FalsePositiveEntry) {
				FalsePositiveEntry entry = (FalsePositiveEntry) element;
				if (TABLE_ID_DATE == type) {
					return "" + entry.getCreated();
				}
				if (TABLE_ID_AUTHOR == type) {
					return "" + entry.getAuthor();
				}

				FalsePositiveJobData jobData = entry.getJobData();
				if (jobData != null) {
					if (TABLE_ID_DEFINITION == type) {
						return jobData.getJobUUID() + " : " + jobData.getFindingId();
					}
					if (TABLE_ID_COMMENT == type) {
						return "" + jobData.getComment();
					}
				}

				FalsePositiveProjectData projectData = entry.getProjectData();
				if (projectData != null) {
					if (TABLE_ID_COMMENT == type) {
						return "" + projectData.getComment();
					}
					WebscanFalsePositiveProjectData webScan = projectData.getWebScan();
					if (webScan != null) {
						if (TABLE_ID_DEFINITION == type) {
							return webScan.getMethods() + "," + webScan.getUrlPattern() + ", CWE:" + webScan.getCweId();
						}
					} else {
						return "";
					}
				}
			}

			return "";
		}

	}

	private class FalsePositiveTreeContentProvider implements ITreeContentProvider {

		private static final Object[] EMPTY = new Object[] {};

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof FalsePositiveProjectConfiguration) {
				FalsePositiveProjectConfiguration config = (FalsePositiveProjectConfiguration) inputElement;
				return config.getFalsePositives().toArray();
			}
			return EMPTY;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return EMPTY;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}

	private class FalsePositivesViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof FalsePositiveEntry && e2 instanceof FalsePositiveEntry) {
				FalsePositiveEntry falsePositive1 = (FalsePositiveEntry) e1;
				FalsePositiveEntry falsePositive2 = (FalsePositiveEntry) e2;

				/* we just sort on creation date - last one is on top */
				String created1 = falsePositive1.getCreated();
				String created2 = falsePositive2.getCreated();

				if (created1 != null && created2 != null) {
					return created2.compareTo(created1);
				}
			}
			return 0; // fall back to "equal"
		}
	}
}