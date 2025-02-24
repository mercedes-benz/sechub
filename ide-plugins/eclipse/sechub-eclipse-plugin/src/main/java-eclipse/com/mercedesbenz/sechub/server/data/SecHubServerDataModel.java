package com.mercedesbenz.sechub.server.data;

import java.util.ArrayList;
import java.util.List;

public class SecHubServerDataModel {

	private List<ServerElement> servers = new ArrayList<ServerElement>();

	public List<ServerElement> getServers() {
		return servers;
	}

	public class ServerElement implements SecHubServerData {
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

}
