package com.daimler.sechub.domain.scan.product.sereco;

import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Responsible class for identifying if a vulnerability identified by a product is handled by 
 * a false positive meta data configuration  
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoFalsePositiveCodeScanFinder {

    public boolean isFound(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        throw new RuntimeException("implement me...");
    }

}
