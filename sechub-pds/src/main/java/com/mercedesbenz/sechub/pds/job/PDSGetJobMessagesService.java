// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.pds.job.PDSJobAssert.*;
import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.pds.usecase.PDSStep;
import com.mercedesbenz.sechub.pds.usecase.UseCaseUserFetchesJobMessages;

@Service
public class PDSGetJobMessagesService {

    @Autowired
    PDSJobRepository repository;

    @UseCaseUserFetchesJobMessages(@PDSStep(name = "service call", description = "Fetches job messages from database. When job is not already done a failure will be shown", number = 2))
    @RolesAllowed({ PDSRoleConstants.ROLE_SUPERADMIN, PDSRoleConstants.ROLE_USER })
    public String getJobMessages(UUID jobUUID) {
        notNull(jobUUID, "job uuid may not be null!");

        PDSJob job = assertJobFound(jobUUID, repository);
        assertJobIsInState(job, PDSJobStatusState.DONE, PDSJobStatusState.FAILED);

        String json = job.getMessages();
        if (json != null) {
            return json;
        }
        /* create an empty message list and convert to json */
        return new SecHubMessagesList().toJSON();
    }

}
