// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Represents a strategy to check if a vulnerability identified by a product is
 * handled by a false positive meta data configuration
 *
 * @author Albert Tregnaghi
 *
 */
public interface SerecoJobDataFalsePositiveStrategy {

    public boolean isFalsePositive(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData);
}
