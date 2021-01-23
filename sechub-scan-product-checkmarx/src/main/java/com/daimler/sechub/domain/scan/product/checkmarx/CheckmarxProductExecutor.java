// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

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

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxMetaDataID;
import com.daimler.sechub.domain.scan.OneInstallSetupConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.product.AbstractCodeScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspection;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspector;
import com.daimler.sechub.sharedkernel.resilience.ResilienceCallback;
import com.daimler.sechub.sharedkernel.resilience.ResilienceContext;
import com.daimler.sechub.sharedkernel.resilience.ResilientActionExecutor;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.storage.core.JobStorage;

@Service
public class CheckmarxProductExecutor extends AbstractCodeScanProductExecutor<CheckmarxInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxProductExecutor.class);

    @Value("${sechub.adapter.checkmarx.scanresultcheck.period.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
    private int scanResultCheckPeriodInMinutes;

    @Value("${sechub.adapter.checkmarx.scanresultcheck.timeout.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
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

    ResilientActionExecutor<ProductResult> resilientActionExecutor;

    public CheckmarxProductExecutor() {
        /* we create here our own instance - only for this service! */
        this.resilientActionExecutor = new ResilientActionExecutor<>();

    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(checkmarxResilienceConsultant);
    }

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, CheckmarxInstallSetup setup,
            TargetRegistryInfo data) throws Exception {
        LOG.debug("Trigger checkmarx adapter execution");

        UUID jobUUID = context.getSechubJobUUID();
        String projectId = context.getConfiguration().getProjectId();

        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

        CheckmarxExecutorConfigSuppport configSupport = CheckmarxExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                systemEnvironment);

        CheckmarxResilienceCallback callBack = new CheckmarxResilienceCallback(configSupport, executorContext);

        /* start resilient */
        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            AdapterMetaData metaDataOrNull = executorContext.getCurrentMetaDataOrNull();

            try (InputStream sourceCodeZipFileInputStream = fetchInputStreamIfNecessary(storage, metaDataOrNull)) {

                /* @formatter:off */

                CheckmarxAdapterConfig checkMarxConfig = CheckmarxConfig.builder().
    					configure(createAdapterOptionsStrategy(context)).
    					configure(new OneInstallSetupConfigBuilderStrategy(setup)).
    					setAlwaysFullScan(callBack.alwaysFullScanEnabled).
    					setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
    					setScanResultTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
    					setFileSystemSourceFolders(data.getCodeUploadFileSystemFolders()).
    					setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
    					setTeamIdForNewProjects(setup.getTeamIdForNewProjects(projectId)).
    					setClientSecret(setup.getClientSecret()).
    					setEngineConfigurationName(setup.getEngineConfigurationName()).
    					setPresetIdForNewProjects(setup.getPresetIdForNewProjects(projectId)).
    					setProjectId(projectId).
    					setTraceID(context.getTraceLogIdAsString()).
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
                String xml = checkmarxAdapter.start(checkMarxConfig, executorContext.getCallBack());

                ProductResult productResult = executorContext.getCurrentProductResult(); // product result is set by callback
                productResult.setResult(xml);

                return productResult;
            }
        }, callBack);
        return Collections.singletonList(result);

    }

    private class CheckmarxResilienceCallback implements ResilienceCallback {

        private ProductExecutorContext executorContext;
        boolean alwaysFullScanEnabled;

        public CheckmarxResilienceCallback(CheckmarxExecutorConfigSuppport configSupport, ProductExecutorContext executorContext) {
            this.alwaysFullScanEnabled = configSupport.isAlwaysFullScanEnabled();
            this.executorContext = executorContext;
        }

        @Override
        public void beforeRetry(ResilienceContext context) {
            Boolean fallbackToFullScan = context.getValueOrNull(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN);
            if (fallbackToFullScan == null) {
                return;
            }
            LOG.debug("fallback to checkmarx fullscan recognized, alwaysFullScanEnabled before:{}",alwaysFullScanEnabled);
            
            alwaysFullScanEnabled = true;
            
            LOG.debug("fallback to checkmarx fullscan recognized, alwaysFullScanEnabled now:{}",alwaysFullScanEnabled);
            /*
             * we must remove the the old scan id inside metadata so the restart will do a
             * new scan and not reuse the old one! When we do not rest the file upload as well,
             * the next scan does complains about missing source locations
             */
            AdapterMetaData metaData = executorContext.getCurrentMetaDataOrNull();
            if (metaData != null) {
                String keyScanId = CheckmarxMetaDataID.KEY_SCAN_ID;
                String uploadKey = CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE;
                LOG.debug("start reset checkmarx adapter meta data for {} and {}", keyScanId, uploadKey);
                metaData.setValue(keyScanId, null);
                metaData.setValue(uploadKey, null);

                executorContext.getCallBack().persist(metaData);
                LOG.debug("persisted checkmarx adapter meta data");
            }
            /*
             * we reset the context information, so former parts will only by triggered
             * again, when the problem really come up again.
             */
            context.setValue(CheckmarxResilienceConsultant.CONTEXT_ID_FALLBACK_CHECKMARX_FULLSCAN, null);
        }
    };

    private InputStream fetchInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(CheckmarxMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch("sourcecode.zip");
    }

    @Override
    public ProductIdentifier getIdentifier() {
        return ProductIdentifier.CHECKMARX;
    }

    @Override
    protected CheckmarxInstallSetup getInstallSetup() {
        return installSetup;
    }

    @Override
    public int getVersion() {
        return 1;
    }

}
