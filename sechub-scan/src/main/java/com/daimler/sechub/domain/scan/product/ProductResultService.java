// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;

@Service
public class ProductResultService {

	@Autowired
	ProductResultRepository repository;

	@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
	public List<ProductResult> fetchAllResultsForJob(UUID sechubJobUUID) {
		ProductResult probe = new ProductResult();
		probe.secHubJobUUID=sechubJobUUID;

		return repository.findAll(Example.of(probe));
	}
	
	@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
    public void deleteAllResultsForJob(UUID sechubJobUUID) {
        ProductResult probe = new ProductResult();
        probe.secHubJobUUID=sechubJobUUID;

        List<ProductResult> existingResults = repository.findAll(Example.of(probe));
        for (ProductResult result: existingResults) {
            repository.delete(result);
        }
        
    }


}
