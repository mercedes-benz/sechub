// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.encryption.UseCaseAdminStartsEncryptionRotation;

@Service
public class ScheduleCipherPoolDataTransactionService {

    @Autowired
    ScheduleCipherPoolDataRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @UseCaseAdminStartsEncryptionRotation(@Step(number = 4, name = "Service call", description = "Creates new cipher pool entry in database in own transaction"))
    public ScheduleCipherPoolData storeInOwnTransaction(ScheduleCipherPoolData poolData) throws ScheduleEncryptionException {
        return repository.save(poolData);
    }

}
