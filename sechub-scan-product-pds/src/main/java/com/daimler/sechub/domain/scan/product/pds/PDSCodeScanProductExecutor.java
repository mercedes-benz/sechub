// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

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
import com.daimler.sechub.adapter.pds.PDSAdapter;
import com.daimler.sechub.adapter.pds.PDSAdapterConfig;
import com.daimler.sechub.adapter.pds.PDSMetaDataID;
import com.daimler.sechub.adapter.pds.PDSWebScanConfig;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.product.AbstractCodeScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspection;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspector;
import com.daimler.sechub.sharedkernel.resilience.ResilientActionExecutor;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.storage.core.JobStorage;

@Service
public class PDSCodeScanProductExecutor extends AbstractCodeScanProductExecutor<PDSInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCodeScanProductExecutor.class);

    @Value("${sechub.adapter.PDS.scanresultcheck.period.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
    private int scanResultCheckPeriodInMinutes;

    @Value("${sechub.adapter.PDS.scanresultcheck.timeout.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
    private int scanResultCheckTimeOutInMinutes;

    @Autowired
    PDSAdapter PDSAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    StorageService storageService;

    @Autowired
    MetaDataInspector scanMetaDataCollector;

    @Autowired
    PDSResilienceConsultant PDSResilienceConsultant;

    ResilientActionExecutor<ProductResult> resilientActionExecutor;

    public PDSCodeScanProductExecutor() {
        /* we create here our own instance - only for this service! */
        this.resilientActionExecutor = new ResilientActionExecutor<>();

    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(PDSResilienceConsultant);
    }

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, PDSInstallSetup setup,
            TargetRegistryInfo data) throws Exception {
        LOG.debug("Trigger PDS adapter execution");

        UUID jobUUID = context.getSechubJobUUID();
        String projectId = context.getConfiguration().getProjectId();

        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            AdapterMetaData metaDataOrNull = executorContext.getCurrentMetaDataOrNull();
            try (InputStream sourceCodeZipFileInputStream = fetchInputStreamIfNecessary(storage, metaDataOrNull)) {

                /* @formatter:off */

					PDSAdapterConfig pDSConfig =PDSWebScanConfig.builder().
//							configure(createAdapterOptionsStrategy(context)).
//							configure(new OneInstallSetupConfigBuilderStrategy(setup)).
//							setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
//							setScanResultTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
////							setFileSystemSourceFolders(data.getCodeUploadFileSystemFolders()).
////							setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
//							setTeamIdForNewProjects(setup.getTeamIdForNewProjects(projectId)).
//							setPresetIdForNewProjects(setup.getPresetIdForNewProjects(projectId)).
//							setProjectId(projectId).
							setTraceID(context.getTraceLogIdAsString()).
							build();
					/* @formatter:on */

                /* inspect */
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_CODESCAN.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pDSConfig.getTraceID());
//                inspection.notice("presetid", pDSConfig.getPresetIdForNewProjectsOrNull());
//                inspection.notice("teamid", pDSConfig.getTeamIdForNewProjects());

                /* execute PDS by adapter and update product result */
                String xml = PDSAdapter.start(pDSConfig, executorContext.getCallBack());

                ProductResult productResult = executorContext.getCurrentProductResult(); // product result is set by callback
                productResult.setResult(xml);

                return productResult;
            }
        });
        return Collections.singletonList(result);

    }

    private InputStream fetchInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch("sourcecode.zip");
    }

    @Override
    public ProductIdentifier getIdentifier() {
        return ProductIdentifier.PDS_CODESCAN;
    }

    @Override
    protected PDSInstallSetup getInstallSetup() {
        return installSetup;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
