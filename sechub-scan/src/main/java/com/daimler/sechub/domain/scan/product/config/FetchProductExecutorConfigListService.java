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
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfigurationList;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class FetchProductExecutorConfigListService {

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    ProductExecutorConfigValidation validation;
    
    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorFetchesExecutorConfigurationList(
            @Step(number = 2, 
            name = "Service call", 
            description = "Service fetches data and creates a list containing all executor configurations"))
    /* @formatter:on */
    public ProductExecutorConfigList fetchProductExecutorConfigList() {
        auditLogService.log("Wants to fetch list of product execution configurations");
        
        ProductExecutorConfigList configList = new ProductExecutorConfigList();
        
        List<ProductExecutorConfig> data =  repository.findAll();
        for (ProductExecutorConfig config : data) {
            
            ProductExecutorConfigListEntry entry = new ProductExecutorConfigListEntry();
            
            entry.enabled=config.getEnabled();
            entry.name=config.getName();
            entry.uuid=config.getUUID();
            
            configList.getExecutorConfigurations().add(entry);
        }
        
        return configList;
    }

}
