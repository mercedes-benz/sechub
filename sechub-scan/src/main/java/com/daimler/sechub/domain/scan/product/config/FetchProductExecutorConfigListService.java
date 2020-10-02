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
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesExecutorConfigList;

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
    @UseCaseAdministratorFetchesExecutorConfigList(
            @Step(number = 1, 
            name = "Service call", 
            description = "Service fetches list containing all executor configurations"))
    /* @formatter:on */
    public List<ProductExecutorConfig> fetchProductExecutorConfigurations() {
        auditLogService.log("Wants to fetch list of product execution configurations");
        return repository.findAll();
    }

}
