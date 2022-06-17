// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxMetaDataID;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubAdapterOptionsBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;
import com.mercedesbenz.sechub.sharedkernel.resilience.ResilientActionExecutor;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

@Service
public class CheckmarxProductExecutor extends AbstractProductExecutor {

    static final Logger LOG = LoggerFactory.getLogger(CheckmarxProductExecutor.class);

    @Value("${sechub.adapter.checkmarx.scanresultcheck.period.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_CHECK_IN_MINUTES)
    private int scanResultCheckPeriodInMinutes;

    @Value("${sechub.adapter.checkmarx.scanresultcheck.timeout.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT_IN_MINUTES)
    private int scanResultCheckTimeOutInMinutes;

    @Autowired
    CheckmarxAdapter checkmarxAdapter;

    @Autowired
    CheckmarxInstallSetup installSetup;

    @Autowired
    StorageService storageService;

    @Autowired
    MetaDataInspector scanMetaDataCollector;

    @Autowired
    SystemEnvironment systemEnvironment;

    @Autowired
    CheckmarxResilienceConsultant checkmarxResilienceConsultant;

    public CheckmarxProductExecutor() {
        super(ProductIdentifier.CHECKMARX, 1, ScanType.CODE_SCAN);
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(checkmarxResilienceConsultant);
    }

    // just to make it accessible inside test
    ResilientActionExecutor<ProductResult> fetchResilientExecutor() {
        return this.resilientActionExecutor;
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
        LOG.debug("Trigger checkmarx adapter execution");

        UUID jobUUID = data.getSechubExecutionContext().getSechubJobUUID();
        String projectId = data.getSechubExecutionContext().getConfiguration().getProjectId();

        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

        CheckmarxExecutorConfigSuppport configSupport = CheckmarxExecutorConfigSuppport
                .createSupportAndAssertConfigValid(data.getProductExecutorContext().getExecutorConfig(), systemEnvironment);

        CheckmarxResilienceCallback callback = new CheckmarxResilienceCallback(configSupport, data.getProductExecutorContext());

        /* start resilient */
        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            AdapterMetaData metaDataOrNull = data.getProductExecutorContext().getCurrentMetaDataOrNull();

            try (InputStream sourceCodeZipFileInputStream = fetchInputStreamIfNecessary(storage, metaDataOrNull)) {

                /* @formatter:off */

                @SuppressWarnings("deprecation")
                CheckmarxAdapterConfig checkMarxConfig = CheckmarxConfig.builder().
    					configure(new SecHubAdapterOptionsBuilderStrategy(data, getScanType())).
    					setTrustAllCertificates(installSetup.isHavingUntrustedCertificate()).
    					setUser(configSupport.getUser()).
    					setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken()).
    					setProductBaseUrl(configSupport.getProductBaseURL()).

    					setAlwaysFullScan(callback.isAlwaysFullScanEnabled()).
    					setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
    					setTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
    					setFileSystemSourceFolders(data.getCodeUploadFileSystemFolders()). // to support mocked Checkmarx adapters we MUST use still the deprecated method!
    					setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
    					setTeamIdForNewProjects(configSupport.getTeamIdForNewProjects(projectId)).
    					setClientSecret(configSupport.getClientSecret()).
    					setEngineConfigurationName(configSupport.getEngineConfigurationName()).
    					setPresetIdForNewProjects(configSupport.getPresetIdForNewProjects(projectId)).
    					setProjectId(projectId).
    					setTraceID(data.getSechubExecutionContext().getTraceLogIdAsString()).
    					build();
					/* @formatter:on */

                /* inspect */
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.CHECKMARX.name());
                inspection.notice(MetaDataInspection.TRACE_ID, checkMarxConfig.getTraceID());
                inspection.notice("presetid", checkMarxConfig.getPresetIdForNewProjectsOrNull());
                inspection.notice("engineconfigurationname", checkMarxConfig.getEngineConfigurationName());
                inspection.notice("teamid", checkMarxConfig.getTeamIdForNewProjects());
                inspection.notice("alwaysFullScanEnabled", checkMarxConfig.isAlwaysFullScanEnabled());

                /* execute checkmarx by adapter and update product result */
                String xml = checkmarxAdapter.start(checkMarxConfig, data.getProductExecutorContext().getCallback());

                ProductResult productResult = data.getProductExecutorContext().getCurrentProductResult(); // product result is set by callback
                productResult.setResult(xml);

                return productResult;
            }
        }, callback);
        return Collections.singletonList(result);

    }

    private InputStream fetchInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch(FILENAME_SOURCECODE_ZIP);
    }

    @Override
    protected void customize(ProductExecutorData data) {

    }

}
