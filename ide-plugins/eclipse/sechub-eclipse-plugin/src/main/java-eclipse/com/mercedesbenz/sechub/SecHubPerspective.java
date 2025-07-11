package com.mercedesbenz.sechub;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.mercedesbenz.sechub.callhierarchy.SecHubCallHierarchyView;
import com.mercedesbenz.sechub.report.SecHubReportView;
import com.mercedesbenz.sechub.server.SecHubServerView;

public class SecHubPerspective implements IPerspectiveFactory {
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

        layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.20f, editorArea);

        layout.addView(SecHubCallHierarchyView.ID, IPageLayout.RIGHT, 0.6f, editorArea);

        layout.addView(SecHubServerView.ID, IPageLayout.BOTTOM, 0.70f, editorArea);

        layout.addView(SecHubReportView.ID, IPageLayout.BOTTOM, 0.50f, SecHubCallHierarchyView.ID);

	}
}