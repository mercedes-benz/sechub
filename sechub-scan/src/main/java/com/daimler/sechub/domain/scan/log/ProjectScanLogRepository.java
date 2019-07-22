// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectScanLogRepository extends JpaRepository<ProjectScanLog, UUID>, ProjectScanLogRepositoryCustom{

}
