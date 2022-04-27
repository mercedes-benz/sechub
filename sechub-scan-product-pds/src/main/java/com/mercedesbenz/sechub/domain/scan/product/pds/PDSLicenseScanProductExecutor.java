// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapter;
import com.mercedesbenz.sechub.adapter.pds.PDSLicenseScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSLicenseScanConfigImpl;
import com.mercedesbenz.sechub.adapter.pds.PDSMetaDataID;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

public class PDSLicenseScanProductExecutor extends AbstractProductExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(PDSLicenseScanProductExecutor.class);

    private static final String SOURCECODE_ZIP_CHECKSUM = "sourcecode.zip.checksum";

    private static final String SOURCECODE_ZIP = "sourcecode.zip";

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

    public PDSLicenseScanProductExecutor() {
        super(ProductIdentifier.PDS_LICENSESCAN, 1, ScanType.LICENSE_SCAN);
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(pdsResilienceConsultant);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
        LOG.debug("Trigger PDS adapter execution");

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                systemEnvironment);

        SecHubExecutionContext context = data.getSechubExecutionContext();

        UUID jobUUID = context.getSechubJobUUID();
        String projectId = context.getConfiguration().getProjectId();

        JobStorage storage = storageService.getJobStorage(projectId, jobUUID);

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            AdapterMetaData metaDataOrNull = executorContext.getCurrentMetaDataOrNull();

            /* we reuse existing file upload checksum done by sechub */
            String sourceZipFileChecksum = fetchFileUploadChecksumIfNecessary(storage, metaDataOrNull);

            try (InputStream sourceCodeZipFileInputStream = fetchInputStreamIfNecessary(storage, metaDataOrNull)) {

                /* @formatter:off */

                    Map<String, String> jobParams = configSupport.createJobParametersToSendToPDS(context.getConfiguration());

                    PDSLicenseScanConfig pdsLicenseScanConfig = PDSLicenseScanConfigImpl.builder().
                            setPDSProductIdentifier(configSupport.getPDSProductIdentifier()).
                            setTrustAllCertificates(configSupport.isTrustAllCertificatesEnabled()).
                            setProductBaseUrl(configSupport.getProductBaseURL()).
                            setSecHubJobUUID(context.getSechubJobUUID()).

                            setSecHubConfigModel(context.getConfiguration()).

                            configure(createAdapterOptionsStrategy(data)).

                            setTimeToWaitForNextCheckOperationInMilliseconds(configSupport.getTimeToWaitForNextCheckOperationInMilliseconds(installSetup)).
                            setTimeOutInMinutes(configSupport.getTimeoutInMinutes(installSetup)).

                            setFileSystemSourceFolders(data.getCodeUploadFileSystemFolders()).
                            setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
                            setSourceZipFileChecksum(sourceZipFileChecksum).

                            setUser(configSupport.getUser()).
                            setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken()).
                            setProjectId(projectId).

                            setTraceID(context.getTraceLogIdAsString()).
                            setJobParameters(jobParams).

                            build();
                    /* @formatter:on */

                /* inspect */
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_LICENSESCAN.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pdsLicenseScanConfig.getTraceID());

                /* execute PDS by adapter and update product result */
                String pdsResult = pdsAdapter.start(pdsLicenseScanConfig, executorContext.getCallback());

                ProductResult productResult = executorContext.getCurrentProductResult(); // product result is set by callback
                productResult.setResult(pdsResult);

                return productResult;
            }
        });
        return Collections.singletonList(result);

    }

    private InputStream fetchInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch(SOURCECODE_ZIP);
    }

    private String fetchFileUploadChecksumIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        try (InputStream inputStream = storage.fetch(SOURCECODE_ZIP_CHECKSUM); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    @Override
    protected void customize(ProductExecutorData data) {

    }
}
