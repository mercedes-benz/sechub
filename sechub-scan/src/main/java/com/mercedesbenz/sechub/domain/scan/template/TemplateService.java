// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesOrUpdatesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAllTemplateIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesTemplate;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

    private TemplateRepository repository;

    TemplateService(@Autowired TemplateRepository repository) {
        this.repository = repository;
    }

    @UseCaseAdminCreatesOrUpdatesTemplate(@Step(number = 2, name = "Service creates or updates template"))
    public void createOrUpdateTemplate(String templateId, TemplateDefinition newTemplateDefinition) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template id may not be null!");
        }
        if (newTemplateDefinition == null) {
            throw new IllegalArgumentException("Template definition may not be null!");
        }

        // first of all we always set template id, so we have never a clash here
        // even when somebody copied an existing template definition.
        newTemplateDefinition.setId(templateId);

        Optional<Template> templateOpt = repository.findById(templateId);
        Template template = null;
        ;
        if (templateOpt.isEmpty()) {
            // we found an existing template
            template = new Template(templateId);
        } else {
            template = templateOpt.get();

            TemplateDefinition existingDefinition = TemplateDefinition.from(template.getDefinition());
            TemplateType originType = existingDefinition.getType();

            if (!Objects.equals(originType, newTemplateDefinition.getType())) {
                // we reuse the existing type - so the type keeps immutable
                // which is important because it may not change when we
                // have templates assigned to projects (we only allow one template per type
                // for a project!)
                LOG.warn("Update will not set type '{}' to template '{}' because type is immutable. Will keep origin type '{}'", newTemplateDefinition.getType(), templateId,
                        originType);

                newTemplateDefinition.setType(originType);
            }
        }
        template.setDefinition(newTemplateDefinition.toFormattedJSON());

        repository.save(template);

        LOG.info("Template {} has been updated/created", templateId);
    }

    @UseCaseAdminDeletesTemplate(@Step(number = 2, name = "Service deletes template"))
    public void deleteTemplate(String templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template id may not be null!");
        }

        /* FIXME Albert Tregnaghi, 2024-10-22: delete relations to projects before! */
        repository.deleteById(templateId);
    }

    @UseCaseAdminFetchesTemplate(@Step(number = 2, name = "Service fetches template"))
    public TemplateDefinition fetchTemplate(String templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template id may not be null!");
        }
        Optional<Template> templateOpt = repository.findById(templateId);
        if (templateOpt.isEmpty()) {
            throw new NotFoundException("Template does not exist!");
        }
        String json = templateOpt.get().getDefinition();

        return TemplateDefinition.from(json);
    }

    @UseCaseAdminFetchesAllTemplateIds(@Step(number = 2, name = "Services fetches all template ids"))
    public List<String> fetchAllTemplateIds() {
        return repository.findAllTemplateIds();
    }

}
