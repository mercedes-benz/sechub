// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.access.SecHubAccess.ServerAccessStatus;
import com.mercedesbenz.sechub.access.SecHubAccessFactory;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.preferences.PreferenceIdConstants;
import com.mercedesbenz.sechub.preferences.SecHubPreferences;
import com.mercedesbenz.sechub.provider.joblist.DateTimeColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.JobUUIDColumnLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.TrafficLightLabelProvider;
import com.mercedesbenz.sechub.provider.joblist.ExecutedByColumnLabelProvider;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.SecHubServerConnection;

public class SecHubServerView extends ViewPart {

	private TreeViewer serverTreeViewer;
	private SecHubServerTreeViewContentProvider serverTreeContentProvider;
	private RefreshSecHubServerViewAction refreshServerViewAction;
	private OpenSecHubServerPreferencesAction openServerPreferencesAction;
	private Combo projectCombo;
	private SecHubServerContext serverContext;
	private TreeViewer jobTreeViewer;
	private SecHubJobTreeViewContentProvider jobTreeContentProvider;

	@Override
	public void createPartControl(Composite parent) {

		serverContext = new SecHubServerContext();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());

		serverTreeContentProvider = new SecHubServerTreeViewContentProvider();

		serverTreeViewer = new TreeViewer(composite, SWT.FULL_SELECTION);
		serverTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		serverTreeViewer.setContentProvider(serverTreeContentProvider);

		projectCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

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
						refreshJobTableForSelectedProject();
					}
				}
			}
		});

		jobTreeContentProvider = new SecHubJobTreeViewContentProvider();
		jobTreeViewer = new TreeViewer(composite, SWT.FULL_SELECTION);
		jobTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		jobTreeViewer.setContentProvider(jobTreeContentProvider);

		createJobColumns();

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
			boolean serverSetupChanged = false;
			serverSetupChanged = serverSetupChanged || event.getProperty().equals(PreferenceIdConstants.SERVER);
			serverSetupChanged = serverSetupChanged
					|| event.getProperty().equals(PreferenceIdConstants.CREDENTIALS_CHANGED);

			if (serverSetupChanged) {
				refreshServerView();
			}
		});
		refreshServerView();
	}

	protected void refreshJobTableForSelectedProject() {
		SecHubAccess access = serverContext.getAccessOrNull();
		if (access == null) {
			return;
		}
		String selectedProjectId = serverContext.getSelectedProjectId();
		SecHubJobInfoForUserListPage currentPage = access.fetchJobInfoListOrNull(selectedProjectId, 30, 0);
		serverContext.setCurrentJobPage(currentPage);
		
		jobTreeViewer.setInput(currentPage);

	}

	private void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();

		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.add(refreshServerViewAction);
		toolBarManager.add(openServerPreferencesAction);

		IMenuManager menuManager = actionBars.getMenuManager();
		menuManager.add(refreshServerViewAction);
		menuManager.add(openServerPreferencesAction);
	}

	private void createJobColumns() {
		TreeViewerColumn id = createTreeViewerColumn(jobTreeViewer, "Created", 100);
		id.setLabelProvider(new DateTimeColumnLabelProvider());

		TreeViewerColumn trafficLight = createTreeViewerColumn(jobTreeViewer, "T.", 20);
		trafficLight.setLabelProvider(new TrafficLightLabelProvider());

		TreeViewerColumn jobUUID = createTreeViewerColumn(jobTreeViewer, "UUID", 100);
		jobUUID.setLabelProvider(new JobUUIDColumnLabelProvider());

		TreeViewerColumn jobOwner = createTreeViewerColumn(jobTreeViewer, "By", 50);
		jobOwner.setLabelProvider(new ExecutedByColumnLabelProvider());

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
	}

	@Override
	public void setFocus() {
		serverTreeViewer.getControl().setFocus();
	}

	public void refreshServerView() {
		initContext();

		serverTreeViewer.setInput(serverContext.getModel());
		
		refreshProjectCombo();
		refreshJobTableForSelectedProject();
	}

	private void initContext() {
		serverContext.reset();

		String serverURL = SecHubPreferences.get().getServerURL();

		if (serverURL == null || serverURL.isBlank()) {
			return;
		}

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
		List<ProjectData> projects = secHubAccess.fetchProjectList();
		serverContext.getModel().setProjects(projects);

		if (projects.isEmpty()) {
			serverContext.setSelectedProjectId(null);
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
		}
	}

	private void refreshProjectCombo() {
		projectCombo.removeAll();

		if (!serverContext.isConnectedWithServer()) {
			projectCombo.setVisible(false);
			return;
		}

		projectCombo.setVisible(true);

		// Fetch project list from SecHubAccess or other relevant source
		SecHubAccess access = serverContext.getAccessOrNull();
		if (access == null) {
			return;
		}
		List<ProjectData> projects = serverContext.getModel().getProjects();
		String selection = serverContext.getSelectedProjectId();

		int index = -1;
		int indexToSelect=-1;
		for (ProjectData data : projects) {
			index++;
			String projectId = data.getProjectId();
			if (Objects.equals(projectId, selection)) {
				indexToSelect=index;
			}
			projectCombo.add(projectId);
			
		}

		if (indexToSelect > -1) {
			projectCombo.select(indexToSelect);
		}
	}

}