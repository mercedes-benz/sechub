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
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorDisablesExecutorConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorEnablesExecutorConfiguration;

@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
@Profile(Profiles.ADMIN_ACCESS)
@Service
public class ChangeEnableStateOfProductExecutorConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeEnableStateOfProductExecutorConfigService.class);

    @Autowired
    ProductExecutorConfigRepository repository;

    @Autowired
    AuditLogService auditLogService;

    /* @formatter:off */
    @UseCaseAdministratorEnablesExecutorConfiguration(
            @Step(
                number=2,
                name="Service call",
                description="Service enables an existing product executor configuration by its UUID"))
    public void enableProductExecutorConfig(UUID uuid) {
        auditLogService.log("Wants to enable product execution configuration {}",uuid);

        setEnabled(uuid,true);
    }
    /* @formatter:off */
    @UseCaseAdministratorDisablesExecutorConfiguration(
            @Step(
                    number=2,
                    name="Service call",
                    description="Service enables an existing product executor configuration by its UUID"))
    public void disableProductExecutorConfig(UUID uuid) {
        auditLogService.log("Wants to disable product execution configuration {}",uuid);
        
        setEnabled(uuid,false);
    }
    /* @formatter:on */

    private void setEnabled(UUID uuid, boolean enabled) {
        ProductExecutorConfig found = repository.getOne(uuid);

        ProductIdentifier productIdentifier = found.getProductIdentifier();
        Integer executorVersion = found.getExecutorVersion();
        found.setEnabled(enabled);

        repository.save(found);

        LOG.info("Changed enable state of product execution {} to {}, which was for product:{} V{}", uuid, enabled, productIdentifier, executorVersion);

    }

}
