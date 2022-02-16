// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;

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

    /*
     * FIXME Albert Tregnaghi, 2020-04-23: we must fix the security-context problem,
     * see https://github.com/Daimler/sechub/issues/216
     */
    // @RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
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
        request.set(MessageDataKeys.SECHUB_UUID, jobUUID);
        request.set(MessageDataKeys.ENVIRONMENT_BASE_URL, sechubEnvironment.getServerBaseUrl());
        eventBus.sendAsynchron(request);
    }

}
