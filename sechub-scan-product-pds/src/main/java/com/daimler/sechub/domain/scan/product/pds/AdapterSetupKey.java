// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.commons.pds.AbstractPDSKey;

/**
 * This kind of keys will be used by sechub PDS executors to setup adapters
 * 
 * @author Albert Tregnaghi
 *
 */
public class AdapterSetupKey extends AbstractPDSKey<AdapterSetupKey> implements SecHubProductExecutionPDSKey{

    AdapterSetupKey(String id, String description) {
        super(id, description);
    }

}
