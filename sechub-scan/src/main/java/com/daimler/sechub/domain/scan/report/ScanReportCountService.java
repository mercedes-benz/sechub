// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScanReportCountService {

	@Autowired
	ScanReportRepository repository;

	@Autowired
	UserInputAssertion assertion;

	public long countProjectProductResults(String projectId) {
		assertion.isValidProjectId(projectId);

		ScanReport probe = new ScanReport();
		probe.projectId = projectId;
		Example<ScanReport> example = Example.of(probe);

		return repository.count(example);

	}


}
