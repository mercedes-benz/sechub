package com.mercedesbenz.sechub.server;

import com.mercedesbenz.sechub.access.SecHubAccess;
import com.mercedesbenz.sechub.access.SecHubAccess.ServerAccessStatus;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.server.data.SecHubServerDataModel;

public class SecHubServerContext {

	private SecHubServerDataModel model = new SecHubServerDataModel();
	private SecHubAccess access;
	private ServerAccessStatus status;
	private String selectedProjectId;
	private SecHubJobInfoForUserListPage currentJobPage;

	public SecHubServerContext() {
		reset();
	}

	public void reset() {
		model = new SecHubServerDataModel();
	}

	public SecHubServerDataModel getModel() {
		return model;
	}

	public void setAccess(SecHubAccess access) {
		this.access = access;
	}

	public SecHubAccess getAccessOrNull() {
		return access;
	}

	public void setStatus(ServerAccessStatus status) {
		this.status = status;
	}

	public boolean isConnectedWithServer() {
		return access != null && status.isAlive();
	}

	public void setSelectedProjectId(String selectedProjectId) {
		this.selectedProjectId = selectedProjectId;
	}

	public String getSelectedProjectId() {
		return selectedProjectId;
	}

	public void setCurrentJobPage(SecHubJobInfoForUserListPage currentPage) {
		this.currentJobPage = currentPage;
	}

	public SecHubJobInfoForUserListPage getCurrentJobPage() {
		return currentJobPage;
	}
}
