// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.mercedesbenz.sechub.SecHubActivator;
import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.access.SecHubAccess.ServerAccessStatus;
import com.mercedesbenz.sechub.access.SecHubAccessFactory;
import com.mercedesbenz.sechub.api.internal.gen.invoker.ApiException;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubReport;
import com.mercedesbenz.sechub.preferences.PreferenceIdConstants;
import com.mercedesbenz.sechub.preferences.SecHubPreferences;
import com.mercedesbenz.sechub.provider.joblist.DateTimeColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.ExecutedByColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.JobUUIDColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.ResultColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.StatusColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.TrafficLightLabelProvider;
import com.mercedesbenz.sechub.report.SecHubReportView;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.SecHubServerConnection;
import com.mercedesbenz.sechub.util.EclipseUtil;

public class SecHubServerView extends ViewPart {

	public static final String ID = "com.mercedesbenz.sechub.views.SecHubServerView";

	private TreeViewer serverTreeViewer;
	private SecHubServerTreeViewContentProvider serverTreeContentProvider;
	private RefreshSecHubServerViewAction refreshServerViewAction;
	private OpenSecHubServerPreferencesAction openServerPreferencesAction;
	private Combo projectCombo;
	private SecHubServerContext serverContext;
	private TreeViewer jobTreeViewer;
	private SecHubJobTreeViewContentProvider jobTreeContentProvider;

	private Label projectLabel;

	private Label currentPageLabel;

	private Label pagesLabel;

	private NextJobPageSecHubServerViewAction nextPageAction;

	private PreviousJobPageSecHubServerViewAction previousPageAction;

	private SearchJobDirectlyServerViewAction searchJobDirectlyAction;

	private Composite composite;

	private OpenWebUIServerViewAction openWebUIAction;

	private OpenProjectFalsePositivesDialogAction openProjectFalsePositivesDialogAction;

	@Override
	public void createPartControl(Composite parent) {
		serverContext = SecHubServerContext.INSTANCE;
		
		String selectedProject = SecHubProjectSelectionStorage.loadSelectedProjectId();

		serverContext.setSelectedProjectId(selectedProject);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());

		Composite headlineComposite = new Composite(composite, SWT.NONE);
		headlineComposite.setLayout(new GridLayout(3, false));
		headlineComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		projectLabel = new Label(headlineComposite, SWT.None);
		projectLabel.setText("Project");

		projectCombo = new Combo(headlineComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		projectCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = projectCombo.getSelectionIndex();
				if (selectedIndex >= 0) {
					ProjectData selectedProject = serverContext.getModel().getProjects().get(selectedIndex);
					String projectId = selectedProject.getProjectId();
					if (!Objects.equals(projectId, serverContext.getSelectedProjectId())) {
						/* change detected - set and reload necessary */
						serverContext.setSelectedProjectId(projectId);
						serverContext.resetPages();
						refreshJobTableForSelectedProject();
					}
				}
			}
		});

		serverTreeContentProvider = new SecHubServerTreeViewContentProvider();

		serverTreeViewer = new TreeViewer(headlineComposite, SWT.NO_SCROLL | SWT.V_SCROLL);
		serverTreeViewer.getControl().setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		serverTreeViewer.setContentProvider(serverTreeContentProvider);
		serverTreeViewer.setInput(new SecHubServerDataModel());// dummy model to avoid empty cell rendering in tree
		serverTreeViewer.addDoubleClickListener((event) -> {
			openServerPreferencesAction.run();
		});

		jobTreeContentProvider = new SecHubJobTreeViewContentProvider();
		jobTreeViewer = new TreeViewer(composite, SWT.FULL_SELECTION);
		jobTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		jobTreeViewer.setContentProvider(jobTreeContentProvider);

		createJobColumns();

		jobTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {

				SecHubReportView.clear();

				SecHubAccess access = serverContext.getAccessOrNull();
				if (access == null) {
					return;
				}

				IStructuredSelection selection = (IStructuredSelection) jobTreeViewer.getSelection();
				Object selectedElement = selection.getFirstElement();

				if (selectedElement instanceof SecHubJobInfoForUser info) {
					UUID jobUUID = info.getJobUUID();

					loadJobFromRemoteAndDisplay(jobUUID);

				}

			}

		});

		Composite pagingComposite = new Composite(composite, SWT.NONE);
		pagingComposite.setLayout(new GridLayout(5, false));
		pagingComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));

		currentPageLabel = new Label(pagingComposite, SWT.NONE);
		currentPageLabel.setText("0");

		Label pagesdivider = new Label(pagingComposite, SWT.NONE);
		pagesdivider.setText("/");

		pagesLabel = new Label(pagingComposite, SWT.NONE);
		pagesLabel.setText("0");

		SechubServerTreeLabelProvider labelProvider = new SechubServerTreeLabelProvider();
		ILabelDecorator labelDecorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		DecoratingStyledCellLabelProvider labelProviderDelegate = new DecoratingStyledCellLabelProvider(labelProvider,
				labelDecorator, null);
		serverTreeViewer.setLabelProvider(labelProviderDelegate);

		Tree serverTree = serverTreeViewer.getTree();
		serverTree.setHeaderVisible(false);
		serverTree.setLinesVisible(false);

		Tree jobTree = jobTreeViewer.getTree();
		jobTree.setHeaderVisible(true);
		jobTree.setLinesVisible(true);

		createActions();
		contributeToActionBars();

		GridLayoutFactory.fillDefaults().generateLayout(parent);

		SecHubPreferences.get().getScopedPreferenceStore().addPropertyChangeListener(event -> {
			/* @formatter:off*/
			
			boolean serverSetupChanged = false;
			serverSetupChanged = serverSetupChanged || event.getProperty().equals(PreferenceIdConstants.SERVER);
			serverSetupChanged = serverSetupChanged || event.getProperty().equals(PreferenceIdConstants.CREDENTIALS_CHANGED);
			serverSetupChanged = serverSetupChanged || event.getProperty().equals(PreferenceIdConstants.TRUST_ALL);

			/* @formatter:on*/
			if (serverSetupChanged) {
				refreshServerView();
			}
		});

		refreshServerView();
	}

	public static void loadJobFromRemoteAndDisplay(UUID jobUUID) {
		EclipseUtil.safeAsyncExec(() -> internalLoadJobFromRemoteAndDisplay(jobUUID));
	}

	private static void internalLoadJobFromRemoteAndDisplay(UUID jobUUID) {
		try {
			SecHubServerContext serverContext = SecHubServerContext.INSTANCE;
			SecHubAccess access = serverContext.getAccessOrNull();
			if (access==null) {
				throw new IllegalStateException("No server connection");
			}
			String projectId = serverContext.getSelectedProjectId();

			SecHubReport report = access.downloadJobReport(projectId, jobUUID);

			SecHubActivator.getDefault().getImporter().importAndDisplayReport(report, projectId);

		} catch (ApiException | IllegalStateException e) {
			EclipseUtil.showErrorDialog("Was not able to download job report.", e);
		}
	}

	protected void refreshJobTableForSelectedProject() {
		RefreshServerJobTableViewJob job = new RefreshServerJobTableViewJob();
		job.schedule();

	}

	private void refreshJobTableByJob() {
		if (!serverContext.isConnectedWithServer()) {
			resetJobTableAndPaging();
			return;
		}
		SecHubAccess access = serverContext.getAccessOrNull();
		if (access == null) {
			resetJobTableAndPaging();
			return;
		}
		String selectedProjectId = serverContext.getSelectedProjectId();
		SecHubJobInfoForUserListPage currentPage;
		try {
			currentPage = access.fetchJobInfoList(selectedProjectId, 10, serverContext.getWantedPage());
			serverContext.setCurrentJobPage(currentPage);
			jobTreeViewer.setInput(currentPage);

			currentPageLabel.setText("" + serverContext.getShownPage());
			pagesLabel.setText("" + serverContext.getShownTotalPages());

		} catch (ApiException e) {
			resetJobTableAndPaging();

			EclipseUtil.showErrorDialog("Was not able to fetch job list for project " + selectedProjectId, e);
		}

		nextPageAction.setEnabled(isNextPageEnabled());
		previousPageAction.setEnabled(isPreviousPageEnabled());
	}

	private void resetJobTableAndPaging() {
		serverContext.setCurrentJobPage(null);
		jobTreeViewer.setInput(null);
		serverContext.setCurrentJobPage(null);
		jobTreeViewer.setInput(null);
		currentPageLabel.setText("0");
		pagesLabel.setText("0");
	}

	private void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();

		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(refreshServerViewAction);
		toolBarManager.add(new Separator());

		toolBarManager.add(previousPageAction);
		toolBarManager.add(nextPageAction);
		toolBarManager.add(new Separator());

		toolBarManager.add(searchJobDirectlyAction);
		toolBarManager.add(new Separator());

		toolBarManager.add(openWebUIAction);

		IMenuManager menuManager = actionBars.getMenuManager();
		menuManager.add(refreshServerViewAction);
		menuManager.add(new Separator());

		menuManager.add(nextPageAction);
		menuManager.add(previousPageAction);
		menuManager.add(new Separator());

		menuManager.add(searchJobDirectlyAction);
		menuManager.add(new Separator());

		menuManager.add(openWebUIAction);
		menuManager.add(openServerPreferencesAction);
		menuManager.add(openProjectFalsePositivesDialogAction);
	}

	private void createJobColumns() {
		TreeViewerColumn id = createTreeViewerColumn(jobTreeViewer, "Created", 160);
		id.setLabelProvider(new DateTimeColumnLabelProvider());

		TreeViewerColumn trafficLight = createTreeViewerColumn(jobTreeViewer, "", 20);
		trafficLight.setLabelProvider(new TrafficLightLabelProvider());
		trafficLight.getColumn().setToolTipText("Trafficlight");

		TreeViewerColumn jobUUID = createTreeViewerColumn(jobTreeViewer, "UUID", 100);
		jobUUID.setLabelProvider(new JobUUIDColumnLabelProvider());

		TreeViewerColumn status = createTreeViewerColumn(jobTreeViewer, "Status", 60);
		status.setLabelProvider(new StatusColumnLabelProvider());

		TreeViewerColumn result = createTreeViewerColumn(jobTreeViewer, "Result", 50);
		result.setLabelProvider(new ResultColumnLabelProvider());

		TreeViewerColumn jobOwner = createTreeViewerColumn(jobTreeViewer, "Executed by", 80);
		jobOwner.setLabelProvider(new ExecutedByColumnLabelProvider());
		jobOwner.getColumn().setToolTipText("The person or system account who started the job");

	}

	private TreeViewerColumn createTreeViewerColumn(TreeViewer viewer, String title, int width) {
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		TreeColumn column = treeViewerColumn.getColumn();
		column.setWidth(width);
		column.setText(title);

		return treeViewerColumn;
	}

	private void createActions() {
		refreshServerViewAction = new RefreshSecHubServerViewAction(this);
		openServerPreferencesAction = new OpenSecHubServerPreferencesAction();
		nextPageAction = new NextJobPageSecHubServerViewAction(this);
		previousPageAction = new PreviousJobPageSecHubServerViewAction(this);

		searchJobDirectlyAction = new SearchJobDirectlyServerViewAction(this);

		openWebUIAction = new OpenWebUIServerViewAction(this);
		
		openProjectFalsePositivesDialogAction = new OpenProjectFalsePositivesDialogAction(); 
	}

	@Override
	public void setFocus() {
		serverTreeViewer.getControl().setFocus();
	}

	public void refreshServerView() {

		initContext();

		SecHubServerDataModel model = serverContext.getModel();
		serverTreeViewer.setInput(model);

		if (serverContext.isConnectedWithServer()) {
			serverTreeViewer.getTree().setToolTipText("Connected to SecHub server");
			openWebUIAction.setEnabled(true);
		} else {
			serverTreeViewer.getTree()
					.setToolTipText("Not alive or wrong credentials\n(double click to change preferences)");
			openWebUIAction.setEnabled(false);
		}

		serverTreeViewer.refresh(true);

		refreshProjectCombo();
		refreshJobTableForSelectedProject();
		refreshFalsePositives();
		

	}

	private void refreshFalsePositives() {
		serverContext.reloadFalsePositiveDataForCurrentProject();
		SecHubReportView.refreshFalsePositives();
	}

	private void initContext() {
		serverContext.reset();

		String serverURL = SecHubPreferences.get().getServerURL();

		List<ProjectData> projects = Collections.emptyList();
		if (serverURL != null && !serverURL.isBlank()) {

			SecHubAccess secHubAccess = SecHubAccessFactory.create();
			serverContext.setAccess(secHubAccess);

			ServerAccessStatus status = secHubAccess.fetchServerAccessStatus();
			serverContext.setStatus(status);

			SecHubServerConnection connection = serverContext.getModel().new SecHubServerConnection();
			connection.setUrl(serverURL);
			connection.setAlive(status.isAlive());
			connection.setLoginSuccessful(!status.isLoginFaiure());

			serverContext.getModel().setConnection(connection);
			/* fetch project data */

			try {

				if (serverContext.isConnectedWithServer()) {
					projects = secHubAccess.fetchProjectList();
				}

			} catch (ApiException e) {
				EclipseUtil.showErrorDialog("Was not able to retrieve project list", e);
			}
		}
		serverContext.getModel().setProjects(projects);

		if (projects.isEmpty()) {
			serverContext.setSelectedProjectId(null);
			searchJobDirectlyAction.setEnabled(false);
		} else {
			String selected = serverContext.getSelectedProjectId();

			boolean needsToSelectFirstProject = true;
			for (ProjectData project : projects) {
				if (Objects.equals(project.getProjectId(), selected)) {
					/* found, project still exist - just keep it in context */
					needsToSelectFirstProject = false;
					break;
				}
			}
			if (needsToSelectFirstProject) {
				serverContext.setSelectedProjectId(projects.get(0).getProjectId()); // just select first one when
																					// nothing selected before
			}
			searchJobDirectlyAction.setEnabled(true);
		}
	}

	private void refreshProjectCombo() {
		projectCombo.removeAll();

		if (!serverContext.isConnectedWithServer()) {
			projectLabel.setVisible(false);
			projectCombo.setVisible(false);
			return;
		}

		projectLabel.setVisible(true);
		projectCombo.setVisible(true);

		// Fetch project list from SecHubAccess or other relevant source
		SecHubAccess access = serverContext.getAccessOrNull();
		if (access == null) {
			return;
		}

		List<ProjectData> projects = serverContext.getModel().getProjects();
		String selection = serverContext.getSelectedProjectId();

		int index = -1;
		int indexToSelect = -1;
		for (ProjectData data : projects) {
			index++;
			String projectId = data.getProjectId();
			if (Objects.equals(projectId, selection)) {
				indexToSelect = index;
			}
			projectCombo.add(projectId);

		}

		if (indexToSelect > -1) {
			projectCombo.select(indexToSelect);
		}
		// avoid layout failures by calling layout method on parent always:
		projectCombo.getParent().layout(true);
	}

	public void nextPage() {
		if (serverContext.incrementWantedPage()) {
			refreshJobTableForSelectedProject();
		}
	}

	public void previousPage() {
		if (serverContext.decrementWantedPage()) {
			refreshJobTableForSelectedProject();
		}
	}

	public boolean isNextPageEnabled() {
		return serverContext.canGoNextPage();
	}

	public boolean isPreviousPageEnabled() {
		return serverContext.canGoPreviousPage();
	}

	public String getSelectedProjectId() {
		return serverContext.getSelectedProjectId();
	}
	
	private class RefreshServerJobTableViewJob extends UIJob {

		public RefreshServerJobTableViewJob() {
			super("Refreshing SecHub server job table ");
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			refreshJobTableByJob();
			return Status.OK_STATUS;
		}

	}

	public void searchJobDirectly() {
		if (!serverContext.isConnectedWithServer()) {
			return;
		}

		String projectid = serverContext.getSelectedProjectId();
		if (projectid == null || projectid.isBlank()) {
			return;
		}
		JobUUIDDialog dialog = new JobUUIDDialog(EclipseUtil.getActiveWorkbenchShell(), projectid);
		if (dialog.open() == Dialog.OK) {
			UUID jobUUID = dialog.getJobUUID();
			loadJobFromRemoteAndDisplay(jobUUID);
		}
	}

}