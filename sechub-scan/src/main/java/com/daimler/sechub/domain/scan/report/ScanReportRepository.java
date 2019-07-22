// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanReportRepository extends JpaRepository<ScanReport, UUID> {

	public ScanReport findBySecHubJobUUID(UUID secHubJobUUID);
}
