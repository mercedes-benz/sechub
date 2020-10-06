package com.daimler.sechub.domain.scan.product.config;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.INTEGRATIONTEST)
@Service
public class IntegrationTestDeleteAllProductExecutorConfigService {


    @Autowired
    ProductExecutorConfigRepository repository;
    
    @Autowired
    AuditLogService auditLogService;
    
    /* @formatter:off */
    public void deleteAllProductExecutorConfigurations() {
        repository.deleteAll();
    }

    /* @formatter:on */    

}
