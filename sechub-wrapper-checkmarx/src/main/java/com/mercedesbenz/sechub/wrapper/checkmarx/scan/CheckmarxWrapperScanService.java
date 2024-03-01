// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.FileBasedAdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConsultant;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientActionExecutor;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

@Service
public class CheckmarxWrapperScanService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperScanService.class);

    @Autowired
    CheckmarxAdapter adapter;

    @Autowired
    CheckmarxWrapperEnvironment environment;

    @Autowired
    CheckmarxWrapperScanContextFactory factory;

    public AdapterExecutionResult startScan() throws IOException, AdapterException {
        LOG.info("Start scan");

        ResilientActionExecutor<AdapterExecutionResult> resilientActionExecutor = createResilientActionExecutor();
        resilientActionExecutor.add(new CheckmarxResilienceConsultant(environment));

        CheckmarxAdapterConfig config = createConfig();

        File metaDataFile;
        String pdsJobMetaDatafile = environment.getPdsJobAdapterMetaDatafile();

        if (pdsJobMetaDatafile == null || pdsJobMetaDatafile.isEmpty()) {
            LOG.warn("PDS job meta data file not set. Will create fallback temp file. For local tests okay but not for production!");

            metaDataFile = Files.createTempFile("fallback_pds_job_metadata_file", ".txt").toFile();
            LOG.info("Temporary PDS job meta data file is now: {}", metaDataFile);

        } else {
            metaDataFile = new File(pdsJobMetaDatafile);
        }

        AdapterMetaDataCallback adapterMetaDataCallBack = new FileBasedAdapterMetaDataCallback(metaDataFile);

        try {
            return resilientActionExecutor.executeResilient(() -> adapter.start(config, adapterMetaDataCallBack));
        } catch (Exception e) {
            throw AdapterException.asAdapterException(adapter.getAdapterLogId(config), pdsJobMetaDatafile, e);
        }

    }

    /* method is used to have an hook point for unit testing */
    ResilientActionExecutor<AdapterExecutionResult> createResilientActionExecutor() {
        return new ResilientActionExecutor<>();
    }

    private CheckmarxAdapterConfig createConfig() throws IOException {
        /* @formatter:off */


        CheckmarxWrapperScanContext context = factory.create(environment);

        CheckmarxAdapterConfig checkMarxConfig = CheckmarxConfig.builder().
                setTrustAllCertificates(environment.isTrustAllCertificatesEnabled()).
                setUser(environment.getCheckmarxUser()).
                setPasswordOrAPIToken(environment.getCheckmarxPassword()).
                setProductBaseUrl(environment.getCheckmarxProductBaseURL()).

                setAlwaysFullScan(environment.isAlwaysFullScanEnabled()).
                setTimeToWaitForNextCheckOperationInMilliseconds(environment.getScanResultCheckPeriodInMilliseconds()).
                setTimeOutInMinutes(environment.getScanResultCheckTimeOutInMinutes()).
                setMockDataIdentifier(context.createMockDataIdentifier()).
                setSourceCodeZipFileInputStream(context.createSourceCodeZipFileInputStream()).
                setTeamIdForNewProjects(context.getTeamIdForNewProjects()).
                setClientSecret(environment.getClientSecret()).
                setEngineConfigurationName(environment.getEngineConfigurationName()).
                setPresetIdForNewProjects(context.getPresetIdForNewProjects()).
                setProjectId(context.getProjectId()).
                setTraceID(environment.getSecHubJobUUID()).
                build();
            /* @formatter:on */

        return checkMarxConfig;

        /* @formatter:on */
    }
}
