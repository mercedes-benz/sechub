package com.daimler.sechub.domain.scan.product.pds;

/**
 * This kind of keys will be used by sechub PDS executors to setup adapters
 * @author Albert Tregnaghi
 *
 */
public class PDSProductAdapterKey extends PDSProductExecutorKey{

    
    PDSProductAdapterKey(String key, String description) {
        super(key, description);
    }

}
