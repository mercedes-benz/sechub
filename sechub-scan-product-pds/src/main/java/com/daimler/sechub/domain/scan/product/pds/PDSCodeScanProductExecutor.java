// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.AdapterMetaData;
import com.daimler.sechub.adapter.pds.PDSAdapter;
import com.daimler.sechub.adapter.pds.PDSCodeScanConfig;
import com.daimler.sechub.adapter.pds.PDSCodeScanConfigImpl;
import com.daimler.sechub.adapter.pds.PDSMetaDataID;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.product.AbstractCodeScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspection;
import com.daimler.sechub.sharedkernel.metadata.MetaDataInspector;
import com.daimler.sechub.sharedkernel.resilience.ResilientActionExecutor;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.storage.core.JobStorage;

@Service
public class PDSCodeScanProductExecutor extends AbstractCodeScanProductExecutor<PDSInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCodeScanProductExecutor.class);

    @Autowired
    PDSAdapter pdsAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    StorageService storageService;
    
    @Autowired
    SystemEnvironment systemEnvironment;
    
    @Autowired
    MetaDataInspector scanMetaDataCollector;

    @Autowired
    PDSResilienceConsultant pdsResilienceConsultant;

    ResilientActionExecutor<ProductResult> resilientActionExecutor;

    public PDSCodeScanProductExecutor() {
        /* we create here our own instance - only for this service! */
        this.resilientActionExecutor = new ResilientActionExecutor<>();

    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(pdsResilienceConsultant);
    }

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, PDSInstallSetup setup,
            TargetRegistryInfo info) throws Exception {
        LOG.debug("Trigger PDS adapter execution");

        PDSExecutionConfigSuppport configSupport = new PDSExecutionConfigSuppport(executorContext.getExecutorConfig(),systemEnvironment);
        if (configSupport.isTargetTypeForbidden(info.getTargetType())){
            LOG.info("pds adapter does not accept target type:{} so cancel execution");
            return Collections.emptyList();
        }
        
        
        UUID jobUUID = context.getSechubJobUUID();
        String projectId = context.getConfiguration().getProjectId();

        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            AdapterMetaData metaDataOrNull = executorContext.getCurrentMetaDataOrNull();
            try (InputStream sourceCodeZipFileInputStream = fetchInputStreamIfNecessary(storage, metaDataOrNull)) {

                /* @formatter:off */

					Map<String, String> jobParams = configSupport.createJobParametersToSendToPDS();
					
                    PDSCodeScanConfig pdsCodeScanConfig =PDSCodeScanConfigImpl.builder().
							configure(createAdapterOptionsStrategy(context)).
							setTimeToWaitForNextCheckOperationInMinutes(configSupport.getScanResultCheckPeriodInMinutes(setup)).
							setScanResultTimeOutInMinutes(configSupport.getScanResultCheckTimeoutInMinutes(setup)).
							setFileSystemSourceFolders(info.getCodeUploadFileSystemFolders()).
							setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
							setUser(configSupport.getUser()).
							setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken()).
							setProjectId(projectId).
							setTraceID(context.getTraceLogIdAsString()).
							setJobParameters(jobParams).
							build();
					/* @formatter:on */

                /* inspect */
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_CODESCAN.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pdsCodeScanConfig.getTraceID());

                /* execute PDS by adapter and update product result */
                String xml = pdsAdapter.start(pdsCodeScanConfig, executorContext.getCallBack());

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
