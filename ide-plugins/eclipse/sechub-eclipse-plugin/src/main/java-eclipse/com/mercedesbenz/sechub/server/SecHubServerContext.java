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
	private int wantedPage;

	public SecHubServerContext() {
		reset();
	}

	public void reset() {
		model = new SecHubServerDataModel();
		resetPages();
		status=null;
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
		return access != null && status!=null && status.isAlive();
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

	public void resetPages() {
		this.wantedPage = 0;
		this.currentJobPage = null;
	}

	public boolean incrementWantedPage() {
		if (!canGoNextPage()) {
			return false;
		}
		this.wantedPage += 1;
		return true;
	}

	public boolean decrementWantedPage() {
		if (!canGoPreviousPage()) {
			return false;
		}
		this.wantedPage -= 1;
		return true;
	}

	public boolean canGoNextPage() {
		if (currentJobPage == null) {
			return false;
		}
		return getShownPage() < getShownTotalPages();

	}

	public boolean canGoPreviousPage() {
		if (currentJobPage == null) {
			return false;
		}
		return getShownPage() > 1;

	}

	public void setWantedPage(int wantedPage) {
		this.wantedPage = wantedPage;
	}

	public int getWantedPage() {
		return wantedPage;
	}

	public int getShownTotalPages() {
		SecHubJobInfoForUserListPage listPage = currentJobPage;
		if (listPage == null) {
			return 0;
		}
		Integer totalPages = listPage.getTotalPages();
		if (totalPages == null) {
			return 0;
		}
		return totalPages;
	}

	public int getShownPage() {
		SecHubJobInfoForUserListPage listPage = currentJobPage;
		if (listPage == null) {
			return 0;
		}
		Integer page = listPage.getPage();
		if (page == null) {
			return 0;
		}
		if (page==0) {
			if (getShownTotalPages()==0){
				// we need to show 0/0
				return 0;
			}
		}
		return page + 1;
	}

}
