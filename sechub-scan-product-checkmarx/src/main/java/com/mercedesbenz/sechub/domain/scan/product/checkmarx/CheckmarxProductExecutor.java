// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxMetaDataID;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConsultant;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientActionExecutor;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubAdapterOptionsBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;
import com.mercedesbenz.sechub.sharedkernel.storage.SecHubStorageService;
import com.mercedesbenz.sechub.storage.core.JobStorage;

import jakarta.annotation.PostConstruct;

/**
 * This class is marked as deprecated. Why? The product executor shall no longer
 * be used in production. We use the PDS solution for checkmarx which does a
 * similar logic inside CheckmarxWrapperScanService.
 *
 * We kept this class, because for existing integration tests (scenario 1-3) a
 * complete migration was much work and would also lead to slower test execution
 * (because communication with PDS instances take time as well).
 *
 * @author Albert Tregnaghi
 *
 */

@Service
@Deprecated
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
    SecHubStorageService storageService;

    @Autowired
    MetaDataInspector scanMetaDataCollector;

    @Autowired
    SystemEnvironmentVariableSupport systemEnvironmentVariableSupport;

    @Autowired
    SecHubDirectCheckmarxResilienceConfiguration environmentBasedResilienceConfig;

    public CheckmarxProductExecutor() {
        super(ProductIdentifier.CHECKMARX, 1, ScanType.CODE_SCAN);
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(new CheckmarxResilienceConsultant(environmentBasedResilienceConfig));
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

        JobStorage storage = storageService.createJobStorageForProject(projectId, jobUUID);

        CheckmarxExecutorConfigSuppport configSupport = CheckmarxExecutorConfigSuppport
                .createSupportAndAssertConfigValid(data.getProductExecutorContext().getExecutorConfig(), systemEnvironmentVariableSupport);

        AdapterMetaDataCallback metaDataCallback = data.getProductExecutorContext().getCallback();

        CheckmarxResilienceCallback callback = new CheckmarxResilienceCallback(configSupport.isAlwaysFullScanEnabled(), metaDataCallback);

        /* start resilient */
        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            AdapterMetaData metaDataOrNull = data.getProductExecutorContext().getCurrentMetaDataOrNull();

            try (InputStream sourceCodeZipFileInputStream = fetchInputStreamIfNecessary(storage, metaDataOrNull)) {

                /* @formatter:off */

                CheckmarxAdapterConfig checkMarxConfig = CheckmarxConfig.builder().
    					configure(new SecHubAdapterOptionsBuilderStrategy(data, getScanType())).
    					setTrustAllCertificates(installSetup.isHavingUntrustedCertificate()).
    					setUser(configSupport.getUser()).
    					setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken()).
    					setProductBaseUrl(configSupport.getProductBaseURL()).

    					setAlwaysFullScan(callback.isAlwaysFullScanEnabled()).
    					setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
    					setTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
    					setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
    					setTeamIdForNewProjects(configSupport.getTeamIdForNewProjects(projectId)).
    					setClientSecret(configSupport.getClientSecret()).
    					setEngineConfigurationName(configSupport.getEngineConfigurationName()).
    					setPresetIdForNewProjects(configSupport.getPresetIdForNewProjects(projectId)).
    					setProjectId(projectId).
    					setTraceID(data.getSechubExecutionContext().getTraceLogIdAsString()).
    					setMockDataIdentifier(data.getMockDataIdentifier()).
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
                AdapterExecutionResult adapterResult = checkmarxAdapter.start(checkMarxConfig, metaDataCallback);

                return updateCurrentProductResult(adapterResult, data.getProductExecutorContext());
            }
        }, callback);
        return Collections.singletonList(result);

    }

    private InputStream fetchInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.getValueAsBoolean(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE)) {
            return null;
        }
        return storage.fetch(FILENAME_SOURCECODE_ZIP);
    }

    @Override
    protected void customize(ProductExecutorData data) {

    }

}
