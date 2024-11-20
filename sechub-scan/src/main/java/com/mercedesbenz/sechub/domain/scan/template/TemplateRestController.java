// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesOrUpdatesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAllTemplateIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesTemplate;

import jakarta.annotation.security.RolesAllowed;

@RestController
@EnableAutoConfiguration
@RequestMapping(APIConstants.API_ADMINISTRATION)
@RolesAllowed({ RoleConstants.ROLE_SUPERADMIN })
@Profile(Profiles.ADMIN_ACCESS)
public class TemplateRestController {

    @Autowired
    TemplateService templateService;

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    LogSanitizer logSanitizer;

    @UseCaseAdminCreatesOrUpdatesTemplate(@Step(number = 1, next = 2, name = "REST API call to create or update template", needsRestDoc = true))
    @RequestMapping(path = "/template/{templateId}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void createOrUpdate(@RequestBody TemplateDefinition templateDefinition, @PathVariable("templateId") String templateId) {

        auditLogService.log("starts create/update of template: {}", logSanitizer.sanitize(templateId, -1));

        templateService.createOrUpdateTemplate(templateId, templateDefinition);

    }

    @UseCaseAdminDeletesTemplate(@Step(number = 1, next = 2, name = "REST API call to delete a template", needsRestDoc = true))
    @RequestMapping(path = "/template/{templateId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("templateId") String templateId) {

        auditLogService.log("starts delete of template: {}", logSanitizer.sanitize(templateId, -1));

        templateService.deleteTemplate(templateId);

    }

    @UseCaseAdminFetchesTemplate(@Step(number = 1, next = 2, name = "REST API call to fetch template", needsRestDoc = true))
    @RequestMapping(path = "/template/{templateId}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public TemplateDefinition fetchTemplate(@PathVariable("templateId") String templateId) {

        auditLogService.log("fetches template definition for template: {}", logSanitizer.sanitize(templateId, -1));

        return templateService.fetchTemplateDefinition(templateId);

    }

    @UseCaseAdminFetchesAllTemplateIds(@Step(number = 1, next = 2, name = "REST API call to fetch template list", needsRestDoc = true))
    @RequestMapping(path = "/templates", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<String> fetchAllTemplateIds() {
        return templateService.fetchAllTemplateIds();

    }
}
