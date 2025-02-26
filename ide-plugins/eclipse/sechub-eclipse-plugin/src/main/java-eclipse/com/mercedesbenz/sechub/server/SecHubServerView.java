// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.access.SecHubAccess.ServerAccessData;
import com.mercedesbenz.sechub.access.SecHubAccessFactory;
import com.mercedesbenz.sechub.preferences.PreferenceIdConstants;
import com.mercedesbenz.sechub.preferences.SecHubPreferences;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel.ServerElement;

public class SecHubServerView extends ViewPart {

	private TreeViewer treeViewer;
	private SecHubServerTreeViewContentProvider serverTreeContentProvider;
	private RefreshSecHubServerViewAction refreshServerViewAction;
	private OpenSecHubServerPreferencesAction openServerPreferencesAction;

	@Override
	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION); // we need FULL_SELECTION for windows SWT, otherwise
																	// only first column selectable...

		serverTreeContentProvider = new SecHubServerTreeViewContentProvider();
		treeViewer.setContentProvider(serverTreeContentProvider);
		
		SechubServerTreeLabelProvider labelProvider = new SechubServerTreeLabelProvider();
		
		ILabelDecorator labelDecorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		
		DecoratingStyledCellLabelProvider labelProviderDelegate = new DecoratingStyledCellLabelProvider(labelProvider, labelDecorator,null);
		treeViewer.setLabelProvider(labelProviderDelegate);
		
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(false);
		tree.setLinesVisible(false);

		
		createActions();
		contributeToActionBars();
		
		GridLayoutFactory.fillDefaults().generateLayout(parent);
		
		/* whenever the serverURL changes the check will be executed */
		SecHubPreferences.get().getScopedPreferenceStore().addPropertyChangeListener(event -> {
			boolean serverSetupChanged = false;
			serverSetupChanged = serverSetupChanged || event.getProperty().equals(PreferenceIdConstants.SERVER);
			serverSetupChanged = serverSetupChanged || event.getProperty().equals(PreferenceIdConstants.CREDENTIALS_CHANGED);
			
			if (serverSetupChanged) {
				refreshServerView();
			}
		});
		refreshServerView();

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

	private void createActions() {
		refreshServerViewAction = new RefreshSecHubServerViewAction(this);
		openServerPreferencesAction = new OpenSecHubServerPreferencesAction();
	}
	
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public void refreshServerView() {
		SecHubServerDataModel model = new SecHubServerDataModel();
		initModel(model);
		
	    treeViewer.setInput(model);
	}

	private void initModel(SecHubServerDataModel model) {
		// Currently we provide max of one SecHub server
		String serverURL = SecHubPreferences.get().getServerURL();
		
		if (serverURL==null || serverURL.isBlank()) {
			return;
		}
		
		ServerElement serverElement = model.new ServerElement();
		serverElement.setUrl(serverURL);
		
		model.getServers().add(serverElement);
		
		SecHubAccess secHubAccess = SecHubAccessFactory.create();
		ServerAccessData status = secHubAccess.fetchServerAccessData();
		serverElement.setAlive(status.isAlive());
		serverElement.setLoginSuccessful(! status.isLoginFaiure());
		
	}

}
