// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PDSJobRepository extends JpaRepository<PDSJob, UUID>, PDSJobRepositoryCustom {

}
