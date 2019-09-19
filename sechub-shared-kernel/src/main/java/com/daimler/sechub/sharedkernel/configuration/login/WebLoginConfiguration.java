package com.daimler.sechub.sharedkernel.configuration.login;

import java.net.URL;
import java.util.Optional;

public class WebLoginConfiguration {

	private URL url;
	private Optional<BasicLoginConfiguration> basic = Optional.empty();
	private Optional<FormLoginConfiguration> form = Optional.empty();

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}


	public Optional<BasicLoginConfiguration> getBasic() {
		return basic;
	}

	public Optional<FormLoginConfiguration> getForm() {
		return form;
	}
}