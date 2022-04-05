// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

import static java.util.Objects.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanConfigTransactionService {

    @Autowired
    ScanConfigRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ScanConfig saveConfigInOwnTransaction(ScanConfig config) {
        requireNonNull(config, "Config may not be null!");
        return repository.save(config);
    }

}
