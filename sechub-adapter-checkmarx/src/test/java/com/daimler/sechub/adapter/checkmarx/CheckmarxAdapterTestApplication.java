// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

import java.io.File;
import java.io.FileWriter;

/**
 * This is a simple test application for checkmarx
 * @author Albert Tregnaghi
 *
 */
public class CheckmarxAdapterTestApplication {

	public static void main(String[] args) throws Exception {
		System.setProperty("log4j.logger.org.apache.http","ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http","OFF");
		
		dump("https.proxyHost");
		dump("https.proxyPort");
		dump("https.nonProxyHosts");
		dump("http.proxyHost");
		dump("http.proxyPort");
		dump("http.nonProxyHosts");
		dump("javax.net.ssl.keyStore");
		dump("javax.net.ssl.trustStore");
		
		String user = System.getProperty("test.sechub.adapter.checkmarx.user");
		if (user==null || user.isEmpty()) {
			throw new IllegalArgumentException("user not set in system properties!");
		}
		String password = System.getProperty("test.sechub.adapter.checkmarx.password");
		if (password==null || password.isEmpty()) {
			throw new IllegalArgumentException("password not set in system properties!");
		}
		String baseUrl = System.getProperty("test.sechub.adapter.checkmarx.baseurl");
		if (baseUrl==null || baseUrl.isEmpty()) {
			throw new IllegalArgumentException("baseurl not set in system properties!");
		}
		String projectname = System.getProperty("test.sechub.adapter.checkmarx.projectName");
		String teamId = System.getProperty("test.sechub.adapter.checkmarx.teamId");
		
		String pathInOtherProject = "zipfile_contains_only_test1.txt.zip"; // leads to FAILED in queue
		pathInOtherProject="zipfile_contains_only_one_simple_java_file.zip"; // should work
		pathInOtherProject="zipfile_contains_sechub_doc_java.zip"; // should work
		
		File zipFile = CheckmarxTestFileSupport.getTestfileSupport().createFileFromRoot("sechub-other/testsourcecode/"+pathInOtherProject);
		/* @formatter:off */
		CheckmarxAdapterConfig config = 
				CheckmarxConfig.builder().
					setUser(user).
					setProjectId(projectname).
					setTeamIdForNewProjects(teamId).
					setPasswordOrAPIToken(password).
					setPathToZipFile(zipFile.getAbsolutePath()).
					setTrustAllCertificates(true).
					setProductBaseUrl(baseUrl).
				build();
		/* @formatter:on */
				
		CheckmarxAdapterV1 adapter = new CheckmarxAdapterV1();
		String data = adapter.start(config);
		File file = File.createTempFile("checkmarx-adaptertest-result", ".xml");
		FileWriter fileWriter= new FileWriter(file);
		fileWriter.write(data);
		fileWriter.close();

		System.out.println("-----------------------------------------------------------------------------------------------------------------");
		System.out.println("- RESULT:");
		System.out.println("-----------------------------------------------------------------------------------------------------------------");
		System.out.println(file.getAbsolutePath());
		
	}
	

	private static void dump(String systemPropertyName) {
		System.out.println(systemPropertyName + "=" + System.getProperty(systemPropertyName));

	}
}
