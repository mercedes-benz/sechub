// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.SecHubEnvironment;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

import jakarta.annotation.security.RolesAllowed;

@Service
public class ProductResultService {

    @Autowired
    ProductResultRepository repository;

    @Autowired
    @Lazy
    DomainMessageService eventBus;

    @Autowired
    SecHubEnvironment sechubEnvironment;

    @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
    public List<ProductResult> fetchAllResultsForJob(UUID sechubJobUUID) {
        ProductResult probe = new ProductResult();
        probe.secHubJobUUID = sechubJobUUID;

        return repository.findAll(Example.of(probe));
    }

    @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
    public void deleteAllResultsForJob(UUID sechubJobUUID) {
        ProductResult probe = new ProductResult();
        probe.secHubJobUUID = sechubJobUUID;

        List<ProductResult> existingResults = repository.findAll(Example.of(probe));
        boolean purged = false;
        for (ProductResult result : existingResults) {
            repository.delete(result);
            purged = true;
        }

        if (purged) {
            /*
             * we only send purged event - when something existed before and was really
             * removed
             */
            sendJobResultsPurged(sechubJobUUID);
        }
    }

    @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
    public List<ProductResult> fetchAllResultsInProject(String projectiD) {
        ProductResult probe = new ProductResult();
        probe.projectId = projectiD;

        return repository.findAll(Example.of(probe));
    }

    @IsSendingAsyncMessage(MessageID.JOB_RESULTS_PURGED)
    private void sendJobResultsPurged(UUID jobUUID) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.JOB_RESULTS_PURGED);
        request.set(MessageDataKeys.SECHUB_JOB_UUID, jobUUID);
        request.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());
        eventBus.sendAsynchron(request);
    }

}
