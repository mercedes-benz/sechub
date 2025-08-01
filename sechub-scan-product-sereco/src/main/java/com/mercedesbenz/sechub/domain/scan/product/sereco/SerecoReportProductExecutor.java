// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;
import static com.mercedesbenz.sechub.sereco.ImportParameter.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.AdapterMetaDataConverter;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorCallback;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.sereco.Sereco;
import com.mercedesbenz.sechub.sereco.Workspace;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Component
public class SerecoReportProductExecutor implements ProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SerecoReportProductExecutor.class);

    @Autowired
    ProductResultRepository productResultRepository;

    @Autowired
    Sereco sechubReportCollector;

    private final static ScanType SCAN_TYPE = ScanType.REPORT;

    private final static int VERSION = 1;

    private static final ProductIdentifier PRODUCT_IDENTIFIER = ProductIdentifier.SERECO;

    private static ProductIdentifier[] supportedProductIdentifiers = createSupportedProductIdentifiers();

    private static ProductIdentifier[] createSupportedProductIdentifiers() {

        List<ProductIdentifier> supportedProductIds = new ArrayList<>();
        for (ProductIdentifier productIdentifier : ProductIdentifier.values()) {
            switch (productIdentifier) {
            case SERECO:
            case PDS_ANALYTICS:
            case UNKNOWN:
                // we do not support these as findings for SERECO nor user messages in report
                break;
            default:
                // everything else is supported
                supportedProductIds.add(productIdentifier);
                break;
            }
        }
        return supportedProductIds.toArray(new ProductIdentifier[supportedProductIds.size()]);
    }

    @Override
    public ProductIdentifier getIdentifier() {
        return PRODUCT_IDENTIFIER;
    }

    @Override
    public List<ProductResult> execute(SecHubExecutionContext context, ProductExecutorContext executorContext) throws SecHubExecutionException {
        return Collections.singletonList(createReport(context, executorContext));
    }

    private ProductResult createReport(SecHubExecutionContext context, ProductExecutorContext executorContext) {
        LocalDateTime started = LocalDateTime.now();
        if (context == null) {
            throw new IllegalArgumentException("context may not be null!");
        }
        String projectId = context.getConfiguration().getProjectId();

        UUID secHubJobUUID = context.getSechubJobUUID();
        UUIDTraceLogID traceLogId = UUIDTraceLogID.traceLogID(secHubJobUUID);

        LOG.debug("{} start sereco execution", traceLogId);

        /* load the results by job uuid */
        ProductIdentifier[] supportedProducts = getSupportedProducts();
        List<ProductResult> foundProductResults = productResultRepository.findAllProductResults(secHubJobUUID, supportedProducts);

        ProductResult result;
        if (foundProductResults.isEmpty()) {
            LOG.warn("{} no product results for {} found, will return an empty sereco JSON as result! ", traceLogId, getSupportedProducts());
            result = new ProductResult(secHubJobUUID, projectId, executorContext.getExecutorConfig(), "{}");
        } else {
            result = createReport(projectId, secHubJobUUID, context.getConfiguration(), traceLogId, executorContext, foundProductResults);
        }

        result.getMetaData();

        result.setStarted(started);
        result.setEnded(LocalDateTime.now());

        return result;
    }

    private ProductResult createReport(String projectId, UUID secHubJobUUID, SecHubConfiguration sechubConfig, UUIDTraceLogID traceLogId,
            ProductExecutorContext executorContext, List<ProductResult> foundProductResults) {
        Workspace workspace = sechubReportCollector.createWorkspace(projectId);

        for (ProductResult productResult : foundProductResults) {
            importProductResult(traceLogId, sechubConfig, workspace, productResult, executorContext);
        }
        String json = workspace.createReport();

        /* fetch + return all vulnerabilities as JSON */
        return new ProductResult(secHubJobUUID, projectId, executorContext.getExecutorConfig(), json);
    }

    private void importProductResult(UUIDTraceLogID traceLogId, SecHubConfiguration sechubConfig, Workspace workspace, ProductResult productResult,
            ProductExecutorContext executorContext) {
        String importData = productResult.getResult();

        String productId = productResult.getProductIdentifier().name();

        if (importData == null) {
            LOG.info("For SecHub job: {} the product: {} did return not even an empty string - so we skip here gracefully.", traceLogId.getPlainId(),
                    productId);
            return;
        }
        List<SecHubMessage> productMessages = new ArrayList<>();
        String messagesJson = productResult.getMessages();
        if (messagesJson != null) {
            SecHubMessagesList messagesList = SecHubMessagesList.fromJSONString(messagesJson);
            List<SecHubMessage> messages = messagesList.getSecHubMessages();
            if (messages != null) {
                productMessages.addAll(messages);
            }
        }

        LOG.debug("{} found product result for '{}'", traceLogId, productId);

        boolean canceled = false;

        ProductExecutorCallback callback = executorContext.getCallback();
        // we now use the product result meta data and check if the product has been
        // been canceled
        AdapterMetaDataConverter metaDataConverter = callback.getMetaDataConverter();
        AdapterMetaData metaData = metaDataConverter.convertToMetaDataOrNull(productResult.getMetaData());
        if (metaData != null) {
            canceled = metaData.getValueAsBoolean(META_DATA_KEY_PRODUCT_CANCELED);
        }

        UUID uuid = productResult.getUUID();
        String docId = uuid != null ? uuid.toString() : "<no uuid set>";
        LOG.debug("{} start to import result '{}' from product '{}' , config:{}", traceLogId, docId, productId, productResult.getProductExecutorConfigUUID());

        /* @formatter:off */
		try {
			workspace.doImport(sechubConfig, builder().
			            canceled(canceled).
						productId(productId).
						importData(importData).
						importProductMessages(productMessages).
						importId(docId)
					.build());
		} catch (IOException e) {
			throw new SecHubRuntimeException("Import into workspace failed:" + docId, e);
		}
		/* @formatter:on */
    }

    ProductIdentifier[] getSupportedProducts() {
        return supportedProductIdentifiers;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public ScanType getScanType() {
        return SCAN_TYPE;
    }

    @Override
    public String toString() {
        return "AbstractProductExecutor [" + (PRODUCT_IDENTIFIER != null ? "PRODUCT_IDENTIFIER=" + PRODUCT_IDENTIFIER + ", " : "") + "VERSION=" + VERSION + ", "
                + (SCAN_TYPE != null ? "SCAN_TYPE=" + SCAN_TYPE : "") + "]";
    }
}
