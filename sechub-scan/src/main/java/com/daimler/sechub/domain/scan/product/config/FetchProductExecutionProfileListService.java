// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutionProfileList;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class FetchProductExecutionProfileListService {

    @Autowired
    ProductExecutionProfileRepository repository;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorFetchesExecutionProfileList(
            @Step(number = 2, 
            name = "Service call", 
            description = "Service fetches data and creates a list containing all executor profiles"))
    /* @formatter:on */
    public ProductExecutionProfilesList fetchProductExecutionProfileList() {
        auditLogService.log("Wants to fetch list of product execution profiles");
        
        ProductExecutionProfilesList configList = new ProductExecutionProfilesList();
        
        List<ProductExecutionProfile> data =  repository.findAll();
        for (ProductExecutionProfile profile : data) {
            
            ProductExecutionProfileListEntry entry = new ProductExecutionProfileListEntry();
            entry.id = profile.getId();
            entry.description=profile.getDescription();
            entry.enabled=profile.enabled;
            
            configList.getExecutionProfiles().add(entry);
        }
        
        return configList;
    }

}
