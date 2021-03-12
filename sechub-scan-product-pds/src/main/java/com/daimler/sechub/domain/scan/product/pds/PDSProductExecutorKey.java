// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

/**
 * This kind of keys will be used by sechub PDS executors to handle communication configuration.
 * Those keys will be defined inside executor configurations
 * @author Albert Tregnaghi
 *
 */
public class PDSProductExecutorKey extends PDSSecHubConfigDataKey<PDSProductExecutorKey>{

    
    PDSProductExecutorKey(String id, String description) {
        super(id, description);
    }

}
