// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario13;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.as;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.waitForJobDone;
import static com.mercedesbenz.sechub.integrationtest.scenario13.Scenario13.PROJECT_1;
import static com.mercedesbenz.sechub.integrationtest.scenario13.Scenario13.USER_1;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;

public class PDSLicenseScanJobScenario13IntTest {
    public static final String PATH = "pds/licensescan/upload/zipfile_contains_inttest_licensescan_with_sample_spdx.json.zip";

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario13.class);

    @Test
    public void test_the_license_scan_module__start_a_new_scan_and_run_pds_license_scan_and_download_report_via_rest() {
        /* prepare */
        String configurationAsJson = IntegrationTestFileSupport.getTestfileSupport().loadTestFile("sechub-integrationtest-licensescanconfig.json");
        SecHubScanConfiguration configuration = SecHubScanConfiguration.createFromJSON(configurationAsJson);

        configuration.setProjectId("myTestProject");

        TestProject project = PROJECT_1;
        UUID jobUUID = as(USER_1).createJobAndReturnJobUUID(project, configuration);

        /* execute */
        as(USER_1).uploadSourcecode(project, jobUUID, PATH).approveJob(project, jobUUID);

        waitForJobDone(project, jobUUID, 30, true);

        /* test */
        String spdxReport = as(USER_1).getSpdxReport(project, jobUUID);

        /* @formatter:off */
        assertTrue(spdxReport.contains(
        		"  \"packages\": [\n" + 
        		"    {\n" +
        		"      \"packageName\": \"go1.16.4.linux-amd64\",\n" +
                "      \"SPDXID\": \"SPDXRef-golang-dist\",\n" +
        		"      \"downloadLocation\": \"https://golang.org/dl/go1.16.4.linux-amd64.tar.gz\",\n" +
                "      \"packageVersion\": \"1.16.4\",\n" +
        		"      \"filesAnalyzed\": \"false\",\n" +
                "      \"checksums\": [\n" +
        		"        {\n" +
                "          \"algorithm\": \"SHA256\",\n" +
                "          \"checksumValue\": \"7154e88f5a8047aad4b80ebace58a059e36e7e2e4eb3b383127a28c711b4ff59\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"packageLicenseConcluded\": \"NOASSERTION\",\n" +
                "      \"packageLicenseDeclared\": \"LicenseRef-Golang-BSD-plus-Patents\",\n" +
                "      \"packageCopyrightText\": \"Copyright (c) 2009 The Go Authors. All rights reserved.\"\n" +
                "    }"));
        /* @formatter:on */
    }
}
