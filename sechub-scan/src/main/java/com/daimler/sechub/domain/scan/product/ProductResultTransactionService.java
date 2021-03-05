// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.sechub.sharedkernel.UUIDTraceLogID;

@Service
public class ProductResultTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductResultTransactionService.class);

    @Autowired
    ProductResultRepository productResultRepository;

    /**
     * Persists the result. This will ALWAYS start a new transaction. So former
     * results will NOT get lost if this persistence fails. Necessary for debugging
     * and also the later possibility to relaunch already existing sechub jobs!
     * Reason: When a former scan did take a very long time and was done. The next
     * time another product exeuction fails because of problems inside the security
     * infrastructure we do not want to restart all parts again, but only the failed
     * / missing ones...<br>
     * <br>
     *
     * @see https://www.ibm.com/developerworks/java/library/j-ts1/index.html for
     *      details on REQUIRES_NEW when using ORM frameworks
     * @param traceLogID
     * @param productExecutor
     * @param productResult
     * @return 
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProductResult persistResult(UUIDTraceLogID traceLogID, ProductResult productResult) {
        LOG.debug("persisting result of product {}  , config uuid {} - traceLogId:{}",productResult.getProductIdentifier(), productResult.getProductExecutorConfigUUID(), productResult);
        return productResultRepository.save(productResult);
    }
    
}
