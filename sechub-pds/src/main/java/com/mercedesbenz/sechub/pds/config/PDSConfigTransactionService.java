// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static java.util.Objects.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PDSConfigTransactionService {

    @Autowired
    PDSConfigRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PDSConfig saveConfigInOwnTransaction(PDSConfig config) {
        requireNonNull(config, "Config may not be null!");
        return repository.save(config);
    }

}
