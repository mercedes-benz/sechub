// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ProductResultCountService {

	@Autowired
	ProductResultRepository repository;

	@Autowired
	UserInputAssertion assertion;

	public long countProjectScanResults(String projectId) {
		assertion.isValidProjectId(projectId);

		ProductResult probe = new ProductResult();
		probe.projectId = projectId;
		Example<ProductResult> example = Example.of(probe);

		return repository.count(example);

	}


}
