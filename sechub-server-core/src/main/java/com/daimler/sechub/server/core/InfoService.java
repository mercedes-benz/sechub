// SPDX-License-Identifier: MIT
package com.daimler.sechub.server.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class InfoService {

	@Autowired(required = false)
	BuildProperties buildProperties;

	/**
	 * @return version as string, always like "major.minor.hotfix{additional chars}"
	 *         - e.g. "1.0.0" or "0.6.0-alpha"
	 */
	public String getVersionAsString() {
		if (buildProperties == null) {
			/*
			 * means we got no injection by ProjectInfoAutoConfiguration - means we are not
			 * inside a build jar - so using fallback properties
			 */
			buildProperties = buildFallbackProperties();
		}
		return buildProperties.getVersion();
	}

	public BuildProperties buildFallbackProperties() {

		Properties properties = new Properties();
		properties.put("version", "0.0.0-development");
		properties.put("name", "sechub-server[development]");
		properties.put("time", SimpleDateFormat.getDateTimeInstance().format(new Date()));

		return new BuildProperties(properties);
	}
}
