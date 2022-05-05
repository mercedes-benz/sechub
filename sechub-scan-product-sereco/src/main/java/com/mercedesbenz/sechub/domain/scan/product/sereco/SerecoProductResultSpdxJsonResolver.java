// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.resolve.ProductResultSpdxJsonResolver;
import com.mercedesbenz.sechub.sereco.metadata.SerecoLicenseDocument;
import com.mercedesbenz.sechub.sereco.metadata.SerecoLicenseSpdx;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

@Component
public class SerecoProductResultSpdxJsonResolver implements ProductResultSpdxJsonResolver {
    private static final Logger LOG = LoggerFactory.getLogger(SerecoProductResultSpdxJsonResolver.class);

    /**
     * Fetches SPDX-Json from Sereco ProductResult
     *
     * @param serecoProductResult from Sereco
     * @return SpdxJson as String or <code>null</code>
     */
    @Override
    public String resolveSpdxJson(ProductResult serecoProductResult) {
        ProductIdentifier productIdentifier = serecoProductResult.getProductIdentifier();

        if (!productIdentifier.equals(ProductIdentifier.SERECO)) {
            throw new IllegalArgumentException("Must be of type Sereco, but was: " + productIdentifier);
        }

        String origin = serecoProductResult.getResult();
        SerecoMetaData data = JSONConverter.get().fromJSON(SerecoMetaData.class, origin);
        List<SerecoLicenseDocument> licenseDocuments = data.getLicenseDocuments();

        for (SerecoLicenseDocument licenseDocument : licenseDocuments) {
            SerecoLicenseSpdx spdx = licenseDocument.getSpdx();

            if (spdx == null) {
                continue;
            }

            String spdxJson = spdx.getJson();

            if (spdxJson != null) {
                return spdxJson;
            }
        }

        LOG.info("No SPDX report found in the product results.");

        return null;
    }
}
