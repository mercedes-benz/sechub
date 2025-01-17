// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.test.TestUtil;

/**
 * This is a simple test application for checkmarx.
 *
 * This test is a system test, which tests the entire Checkmarx adapter. The
 * test will reach out to the real Checkmarx product to create a new project and
 * will initiate a real scan against a working instance of Checkmarx.
 *
 * @author Albert Tregnaghi, Jeremias Eppler
 *
 */
public class TestCheckmarxAdapterApplication {

    public static void main(String[] args) throws Exception {
        System.setProperty("log4j.logger.org.apache.http", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "OFF");

        dump("https.proxyHost");
        dump("https.proxyPort");
        dump("https.nonProxyHosts");
        dump("http.proxyHost");
        dump("http.proxyPort");
        dump("http.nonProxyHosts");
        dump("javax.net.ssl.keyStore");
        dump("javax.net.ssl.trustStore");

        String user = ensureProperty("test.sechub.adapter.checkmarx.user");
        String password = ensureProperty("test.sechub.adapter.checkmarx.password");
        String baseUrl = ensureProperty("test.sechub.adapter.checkmarx.baseurl");
        String projectname = ensureProperty("test.sechub.adapter.checkmarx.projectName");
        String teamId = ensureProperty("test.sechub.adapter.checkmarx.teamid");
        Long presetId = Long.valueOf(ensureProperty("test.sechub.adapter.checkmarx.presetid"));

        String pathInOtherProject = ensurePropertyOrDefault("test.sechub.adapter..checkmarx.zipfilename", "zipfile_contains_only_one_simple_java_file.zip");
        // "zipfile_contains_only_test1.txt.zip"; // leads to FAILED in queue
        // "zipfile_contains_sechub_doc_java.zip"; // should work

        File zipFile = TestCheckmarxFileSupport.getTestfileSupport().createFileFromRoot("sechub-other/testsourcecode/" + pathInOtherProject);
        /* @formatter:off */
		CheckmarxAdapterConfig config =
				CheckmarxConfig.builder().
					setUser(user).
					setProjectId(projectname).
					setTeamIdForNewProjects(teamId).
					setPresetIdForNewProjects(presetId).
					setPasswordOrAPIToken(password).
					setSourceCodeZipFileInputStream(new FileInputStream(zipFile)).
					setTrustAllCertificates(true).
					setProductBaseUrl(baseUrl).
				build();
		/* @formatter:on */

        CheckmarxAdapterV1 adapter = new CheckmarxAdapterV1();
        AdapterExecutionResult adapterResult = adapter.start(config, new AdapterMetaDataCallback() {

            @Override
            public void persist(AdapterMetaData metaData) {
                System.out.println("update metadata:" + metaData);

            }

            @Override
            public AdapterMetaData getMetaDataOrNull() {
                return null;
            }
        });
        File file = TestUtil.createTempFileInBuildFolder("checkmarx-adaptertest-result", "xml").toFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(adapterResult.getProductResult());
        fileWriter.close();

        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("- RESULT:");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println(file.getAbsolutePath());

    }

    private static String ensureProperty(String key) {
        return ensurePropertyOrDefault(key, null, true);
    }

    private static String ensurePropertyOrDefault(String key, String defaultValue) {
        return ensurePropertyOrDefault(key, defaultValue, false);
    }

    private static String ensurePropertyOrDefault(String key, String defaultValue, boolean ignoreDefault) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            if (ignoreDefault) {
                throw new IllegalArgumentException(key + " not set in system properties!");
            }
            return defaultValue;
        }
        return value;
    }

    private static void dump(String systemPropertyName) {
        System.out.println(systemPropertyName + "=" + System.getProperty(systemPropertyName));

    }
}
