// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.domain.scan.product.ProductIdentifier.*;
import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.ProductResultRepository;
import com.daimler.sechub.domain.scan.report.ScanReportToSecHubResultTransformer;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

@Service
/**
 * This service will load the product result data from FARRADAY (and maybe also
 * other products or own implementations) and transform those data a sechub
 * result
 * 
 * @author Albert Tregnaghi
 *
 */
public class SecHubResultService {

	private static final Logger LOG = LoggerFactory.getLogger(SecHubResultService.class);

	@Autowired
	ProductResultRepository productResultRepository;

	@Autowired
	SecHubResultMerger resultMerger;
	
	@Autowired
	List<ScanReportToSecHubResultTransformer> transformers;

	/**
	 * Will fetch output from report products for wanted sechub job and returns a new created
	 * result.
	 * 
	 * @param context
	 * @return result never <code>null</code>
	 */
	public SecHubResult createResult(SecHubExecutionContext context) throws SecHubExecutionException {
		notNull(context, "Context may not be null!");

		UUID secHubJobUUID = context.getSechubJobUUID();
		return createResult(secHubJobUUID);
	}

    public SecHubResult createResult(UUID secHubJobUUID) throws SecHubExecutionException {
        notNull(secHubJobUUID, "secHubJobUUID may not be null!");
        List<ProductResult> productResults = productResultRepository.findAllProductResults(secHubJobUUID, SERECO);

		if (productResults.isEmpty()) {
			throw new SecHubExecutionException("No report result found for:" + secHubJobUUID);
		}
		int productResultAmount = productResults.size();
		if (productResultAmount > 1) {
			LOG.warn("Found {} report product results, only one will be transformed!", productResultAmount);
		}
		SecHubResult mergedResult = null;
		for (ProductResult productResult : productResults) {
			for (ScanReportToSecHubResultTransformer transformer : transformers) {
				if (transformer.canTransform(productResult.getProductIdentifier())) {
					LOG.info("Transformer {} is used to transform result", transformer.getClass().getSimpleName());
					 SecHubResult transformedResult = transformer.transform(productResult);
					 mergedResult=resultMerger.merge(mergedResult, transformedResult);
				}
			}
		}
		if (mergedResult==null) {
		    throw new SecHubExecutionException("No transformable report result format found for:" + secHubJobUUID);
		}
		return mergedResult;
    }
}
