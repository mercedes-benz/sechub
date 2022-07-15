package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.FileBasedAdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

@Service
public class CheckmarxWrapperScanService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperScanService.class);

    @Autowired
    CheckmarxAdapter adapter;

    @Autowired
    CheckmarxWrapperCLIEnvironment environment;

    @Autowired
    CodeScanPathCollector codeScanPathCollector;

    @Autowired
    CheckmarxWrapperContextFactory factory;

    public String startScan() throws Exception {
        LOG.info("Start scan");

        CheckmarxAdapterConfig config = createConfig();

        File metaDataFile;
        String pdsJobMetaDatafile = environment.getPdsJobMetaDatafile();

        if (pdsJobMetaDatafile == null || pdsJobMetaDatafile.isEmpty()) {
            LOG.warn("PDS job meta data file not set. Will create fallback temp file. For local tests okay but not for production!");

            metaDataFile = Files.createTempFile("fallback_pds_job_metadata_file", ".txt").toFile();
            LOG.warn("Temporary PDS job meta data file is now2: {}", metaDataFile);

        } else {
            metaDataFile = new File(pdsJobMetaDatafile);
        }

        AdapterMetaDataCallback adapterMetaDataCallBack = new FileBasedAdapterMetaDataCallback(metaDataFile);

        AdapterExecutionResult adapterResult = adapter.start(config, adapterMetaDataCallBack);

        PDSUserMessageSupport support = new PDSUserMessageSupport(environment.getPdsUserMessagesFolder());
        support.writeMessages(adapterResult.getProductMessages());

        return adapterResult.getProductResult();
    }

    private CheckmarxAdapterConfig createConfig() {
        /* @formatter:off */


        CheckmarxWrapperContext context = factory.create(environment);

        @SuppressWarnings("deprecation")
        CheckmarxAdapterConfig checkMarxConfig = CheckmarxConfig.builder().
//                configure(new SecHubAdapterOptionsBuilderStrategy(data, getScanType())).
                setTrustAllCertificates(environment.isTrustAllCertificatesEnabled()).
                setUser(environment.getUser()).
                setPasswordOrAPIToken(environment.getCheckmarxPassword()).
                setProductBaseUrl(environment.getCheckmarxProductBaseURL()).

                setAlwaysFullScan(environment.isAlwaysFullScanEnabled()).
                setTimeToWaitForNextCheckOperationInMinutes(environment.getScanResultCheckPeriodInMinutes()).
                setTimeOutInMinutes(environment.getScanResultCheckTimeOutInMinutes()).
                setFileSystemSourceFolders(context.createCodeUploadFileSystemFolders()). // to support mocked Checkmarx adapters we MUST use still the deprecated method!
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
