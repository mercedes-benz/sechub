// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapter;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapterConfig;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.CanceableProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;

public abstract class AbstractPDSProductExecutor extends AbstractProductExecutor implements CanceableProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPDSProductExecutor.class);

    @Autowired
    PDSAdapter pdsAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    PDSExecutorConfigSuppportServiceCollection serviceCollection;

    @Autowired
    MetaDataInspector scanMetaDataCollector;

    @Autowired
    PDSResilienceConsultant pdsResilienceConsultant;

    @Autowired
    PDSStorageContentProviderFactory contentProviderFactory;

    @Autowired
    MockDataIdentifierFactory mockDataIdentifierFactory;

    protected AbstractPDSProductExecutor(ProductIdentifier productIdentifier, int version, ScanType scanType) {
        super(productIdentifier, version, scanType);
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(pdsResilienceConsultant);
    }

    @Override
    public boolean cancel(ProductExecutorData data) throws Exception {

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        AdapterConfig config = data.getRememberedAdapterConfig();
        if (!(config instanceof PDSAdapterConfig)) {
            LOG.error("Cannot cancel product: Configuration is not a PDSAdapterConfig instance!");
            return false;
        }
        PDSAdapterConfig pdsAdapterConfig = (PDSAdapterConfig) config;
        return pdsAdapter.cancel(pdsAdapterConfig, executorContext.getCallback());
    }

    @Override
    protected void customize(ProductExecutorData data) {
        /* per default nothing - can be overridden in child classes */
    }

}
