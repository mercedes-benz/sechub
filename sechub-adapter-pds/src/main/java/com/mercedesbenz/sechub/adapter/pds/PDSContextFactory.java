// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;

interface PDSContextFactory {

    PDSContext create(PDSAdapterConfig config, PDSAdapter pdsAdapter, AdapterRuntimeContext runtimeContext);

}