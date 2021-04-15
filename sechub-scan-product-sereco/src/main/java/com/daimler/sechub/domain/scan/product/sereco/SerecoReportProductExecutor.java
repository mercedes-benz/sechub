// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import static com.daimler.sechub.sereco.ImportParameter.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.SecHubRuntimeException;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.ProductResultRepository;
import com.daimler.sechub.domain.scan.report.ScanReportProductExecutor;
import com.daimler.sechub.sereco.Sereco;
import com.daimler.sechub.sereco.Workspace;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

@Component
public class SerecoReportProductExecutor implements ScanReportProductExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(SerecoReportProductExecutor.class);

	@Autowired
	ProductResultRepository productResultRepository;

	@Autowired
	Sereco sechubReportCollector;

	/* @formatter:off */
    private static ProductIdentifier[] supportedProductIdentifiers = new ProductIdentifier[] { 
            ProductIdentifier.NESSUS, 
            ProductIdentifier.NETSPARKER, 
            ProductIdentifier.CHECKMARX,
            
            ProductIdentifier.PDS_CODESCAN,
            ProductIdentifier.PDS_WEBSCAN,
            ProductIdentifier.PDS_INFRASCAN};
    /* @formatter:on */
    

	@Override
	public ProductIdentifier getIdentifier() {
		return ProductIdentifier.SERECO;
	}

	@Override
	public List<ProductResult> execute(SecHubExecutionContext context,ProductExecutorContext executorContext) throws SecHubExecutionException {
		return Collections.singletonList(createReport(context, executorContext));
	}

	private ProductResult createReport(SecHubExecutionContext context, ProductExecutorContext executorContext) {
		if (context == null) {
			throw new IllegalArgumentException("context may not be null!");
		}
		String projectId = context.getConfiguration().getProjectId();

		UUID secHubJobUUID = context.getSechubJobUUID();
		UUIDTraceLogID traceLogId = UUIDTraceLogID.traceLogID(secHubJobUUID);

		LOG.debug("{} start sereco execution", traceLogId);

		/* load the results by job uuid */
		ProductIdentifier[] supportedProducts = getSupportedProducts();
		List<ProductResult> foundProductResults = productResultRepository.findAllProductResults(secHubJobUUID,
				supportedProducts);

		if (foundProductResults.isEmpty()) {
			LOG.warn("{} no product results for {} found, will return an empty sereco JSON as result! ", traceLogId, getSupportedProducts());
			return new ProductResult(secHubJobUUID, projectId, executorContext.getExecutorConfig(), "{}");
		}

		return createReport(projectId, secHubJobUUID, traceLogId, executorContext, foundProductResults);
	}

	private ProductResult createReport(String projectId, UUID secHubJobUUID, UUIDTraceLogID traceLogId,
			ProductExecutorContext executorContext, List<ProductResult> foundProductResults) {
		Workspace workspace = sechubReportCollector.createWorkspace(projectId);

		for (ProductResult productResult : foundProductResults) {
			importProductResult(traceLogId, workspace, productResult);
		}
		String json = workspace.createReport();
		/* fetch + return all vulnerabilities as JSON */
		return new ProductResult(secHubJobUUID, projectId, executorContext.getExecutorConfig(), json);
	}

	private void importProductResult(UUIDTraceLogID traceLogId, Workspace workspace, ProductResult productResult) {
		String importData = productResult.getResult();
		String productId = productResult.getProductIdentifier().name();

		LOG.debug("{} found product result for '{}'", traceLogId, productId);

		UUID uuid = productResult.getUUID();
		String docId = uuid.toString();
		LOG.debug("{} start to import result '{}' from product '{}' , config:{}", traceLogId, docId, productId, productResult.getProductExecutorConfigUUID());

		/* @formatter:off */
		try {
			workspace.doImport(builder().
						productId(productId).
						importData(importData).
						importId(docId)
					.build());
		} catch (IOException e) {
			throw new SecHubRuntimeException("Import into workspace failed:" + docId, e);
		}
		/* @formatter:on */
	}

	/* @formatter:off */
	private ProductIdentifier[] getSupportedProducts() {
	    return supportedProductIdentifiers;
	}
	/* @formatter:on */

	
	@Override
    public int getVersion() {
        return 1;
    }
}
