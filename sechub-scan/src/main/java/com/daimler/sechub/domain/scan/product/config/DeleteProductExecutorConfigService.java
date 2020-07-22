package com.daimler.sechub.domain.scan.product.config;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorRemovesExecutorConfiguration;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class DeleteProductExecutorConfigService {


private static final Logger LOG = LoggerFactory.getLogger(DeleteProductExecutorConfigService.class);

    @Autowired
    ProductExecutorConfigRepository repository;
    
    @Autowired
    AuditLogService auditLogService;
    
    /* @formatter:off */
    @UseCaseAdministratorRemovesExecutorConfiguration(
            @Step(
                number=2,
                name="Service call",
                description="Service deletes an existing product executor configuration by its UUID"))
    public void deleteProductExecutorConfig(UUID uuid) {
        auditLogService.log("Wants to removed product execution {}",uuid);

        ProductExecutorConfig found = repository.getOne(uuid);
        ProductIdentifier productIdentifier = found.getProductIdentifier();
        Integer executorVersion = found.getExecutorVersion();
        
        repository.deleteById(uuid);
        
        LOG.info("Removed product execution {}, which was for product:{} V{}",uuid,productIdentifier,executorVersion);
    }

    /* @formatter:on */    

}
