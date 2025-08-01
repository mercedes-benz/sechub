// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.server.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;

public class SecHubServerDataModel {

	private SecHubServerConnection connection;
	private List<ProjectData> projects = new ArrayList<ProjectData>();
	

	public class SecHubServerConnection {
		private String url;
		private boolean alive;
		private boolean loginSuccessful;

		public SecHubServerDataModel getModel() {
			return SecHubServerDataModel.this;
		}

		public void setUrl(String serverURL) {
			this.url = serverURL;
		}

		public String getUrl() {
			return url;
		}

		public void setAlive(boolean alive) {
			this.alive = alive;
		}
		
		public boolean isAlive() {
			return alive;
		}
		
		public void setLoginSuccessful(boolean loginSuccessful) {
			this.loginSuccessful = loginSuccessful;
		}

		public boolean isLoginSuccessful() {
			return loginSuccessful;
		}
		
	}


	public void setConnection(SecHubServerConnection connection) {
		 this.connection=connection;
	}
	
	public SecHubServerConnection getConnection() {
		return connection;
	}

	public void setProjects(List<ProjectData> projects) {
		this.projects.clear();
		if (projects!=null) {
			this.projects.addAll(projects);
		}
	}
	
	public List<ProjectData> getProjects() {
		return Collections.unmodifiableList(projects);
	}
}
