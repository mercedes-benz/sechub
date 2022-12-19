// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.util.Optional;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

public interface PDSJobRepositoryCustom {

    Optional<PDSJob> findNextJobToExecute();

    long countJobsOfServerInState(String serverId, PDSJobStatusState state);
}
