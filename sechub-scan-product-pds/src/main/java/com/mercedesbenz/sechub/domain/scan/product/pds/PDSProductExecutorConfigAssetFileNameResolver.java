// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.scan.JobParameterProvider;
import com.mercedesbenz.sechub.domain.scan.asset.ProductExecutorConfigAssetFileNameResolver;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;

@Component
public class PDSProductExecutorConfigAssetFileNameResolver implements ProductExecutorConfigAssetFileNameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PDSProductExecutorConfigAssetFileNameResolver.class);

    @Override
    public String resolveAssetFilename(ProductExecutorConfig config) {
        JobParameterProvider provider = new JobParameterProvider(config.getSetup().getJobParameters());
        String pdsProductId = PDSExecutorConfigSupport.getPDSProductIdentifier(provider);
        if (pdsProductId == null) {
            return null;
        }
        String filename = pdsProductId + ".zip";
        LOG.debug("resolved file name for PDS product id '{}' is '{}'", pdsProductId, filename);

        return filename;
    }

}
