// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminAssignsTemplateToProject;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminCreatesOrUpdatesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminDeletesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesAllTemplateIds;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminUnassignsTemplateFromProject;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

    private final TemplateRepository repository;

    private final ScanProjectConfigService configService;

    private final TemplateTypeScanConfigIdResolver scanProjectConfigIdResolver;

    private final UserInputAssertion inputAssertion;

    private DomainMessageService eventBus;

    @Lazy
    TemplateService(TemplateRepository repository, ScanProjectConfigService configService, UserInputAssertion inputAssertion,
            TemplateTypeScanConfigIdResolver resolver, DomainMessageService eventBus) {
        this.repository = repository;
        this.configService = configService;
        this.scanProjectConfigIdResolver = resolver;
        this.inputAssertion = inputAssertion;
        this.eventBus = eventBus;
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

        if (templateOpt.isEmpty()) {
            // we did not find an existing template, so we create one
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
                LOG.warn("Update will not set type '{}' to template '{}' because type is immutable. Will keep origin type '{}'",
                        newTemplateDefinition.getType(), templateId, originType);

                newTemplateDefinition.setType(originType);
            }
        }
        template.setDefinition(newTemplateDefinition.toFormattedJSON());

        repository.save(template);

        LOG.info("Template {} has been updated/created", templateId);
    }

    @UseCaseAdminDeletesTemplate(@Step(number = 2, name = "Service removes all assignments and deletes template completely"))
    @IsSendingAsyncMessage(MessageID.TEMPLATE_DELETED)
    @Transactional
    public void deleteTemplate(String templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template id may not be null!");
        }
        Set<String> allTemplateConfigIds = scanProjectConfigIdResolver.resolveAllPossibleConfigIds();
        configService.deleteAllConfigurationsOfGivenConfigIdsAndValue(allTemplateConfigIds, templateId);

        int numberOfDeletedEntries = repository.deleteTemplateById(templateId);
        if (numberOfDeletedEntries == 0) {
            // we can throw an exception here, without being aware about the transaction
            // rollback
            // because nothing was deleted, so rollback does not matter
            throw new NotFoundException("No template data available for template id:" + templateId);
        }

        DomainMessage message = new DomainMessage(MessageID.TEMPLATE_DELETED);
        SecHubProjectToTemplate data = new SecHubProjectToTemplate();
        data.setTemplateId(templateId);

        message.set(MessageDataKeys.PROJECT_TO_TEMPLATE, data);
        eventBus.sendAsynchron(message);

    }

    /**
     * Fetches template definition by given template id
     *
     * @param templateId template identifier
     * @return definition, never <code>null</code>
     *
     * @throws NotFoundException if no template exists for given identifier
     */
    @UseCaseAdminFetchesTemplate(@Step(number = 2, name = "Service fetches template"))
    public TemplateDefinition fetchTemplateDefinition(String templateId) {
        inputAssertion.assertIsValidTemplateId(templateId);

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

    @UseCaseAdminAssignsTemplateToProject(@Step(number = 3, name = "service assigns project to template in scan domain"))
    public void assignTemplateToProject(String templateId, String projectId) {
        inputAssertion.assertIsValidTemplateId(templateId);
        inputAssertion.assertIsValidProjectId(projectId);

        LOG.debug("try to assign template '{}' to project '{}'", templateId, projectId);

        TemplateDefinition template = fetchTemplateDefinition(templateId);
        ScanProjectConfigID key = scanProjectConfigIdResolver.resolve(template.getType());

        configService.set(projectId, key, templateId);

        LOG.info("assigned template '{}' to project '{}'", templateId, projectId);
    }

    @UseCaseAdminUnassignsTemplateFromProject(@Step(number = 3, name = "service unassigns project from template in scan domain"))
    public void unassignTemplateFromProject(String templateId, String projectId) {
        inputAssertion.assertIsValidTemplateId(templateId);
        inputAssertion.assertIsValidProjectId(projectId);

        LOG.debug("try to unassign template '{}' from project '{}'", templateId, projectId);

        TemplateDefinition template = fetchTemplateDefinition(templateId);
        ScanProjectConfigID key = scanProjectConfigIdResolver.resolve(template.getType());

        ScanProjectConfig config = configService.get(projectId, key);
        String value = config.getData();

        if (!templateId.equals(value)) {
            LOG.warn("Cannot unassign template {} from project {} , because for '{}' the (other) template '{}' is set!", templateId, projectId, key.getId(),
                    value);
            return;
        }

        configService.unset(projectId, key);

        LOG.info("unassigned template '{}' from project '{}'", templateId, projectId);
    }

    /**
     * Fetches all template identifiers for given project
     *
     * @param projectId project identifier
     * @return set with identifiers, never <code>null</code>
     */
    public Set<String> fetchAssignedTemplateIdsForProject(String projectId) {
        Set<String> result = new LinkedHashSet<>();
        for (TemplateType type : TemplateType.values()) {
            ScanProjectConfigID configId = scanProjectConfigIdResolver.resolve(type);
            ScanProjectConfig config = configService.get(projectId, configId, false);
            if (config == null) {
                continue;
            }
            String templateId = config.getData();
            result.add(templateId);
        }
        return result;
    }

    /**
     * Fetches all template definitions for given project
     *
     * @param projectId project identifier
     * @return list of template definitions, never <code>null</code>
     */
    public List<TemplateDefinition> fetchAllTemplateDefinitionsForProject(String projectId) {
        List<TemplateDefinition> result = new ArrayList<>();
        Set<String> templateIds = fetchAssignedTemplateIdsForProject(projectId);
        for (String templateId : templateIds) {
            TemplateDefinition templateDefinition = fetchTemplateDefinition(templateId);
            result.add(templateDefinition);
        }
        return result;
    }

    public Set<String> fetchProjectsUsingTemplate(String templateId) {
        inputAssertion.assertIsValidTemplateId(templateId);

        Set<String> possibleConfigIds = scanProjectConfigIdResolver.resolveAllPossibleConfigIds();

        Set<String> projectIds = configService.findAllProjectsWhereConfigurationHasGivenData(possibleConfigIds, templateId);
        return projectIds;
    }

    public Set<String> fetchAllAssignedTemplateIds() {
        Set<String> result = new TreeSet<>();

        for (TemplateType templateType : TemplateType.values()) {
            ScanProjectConfigID scanConfigType = scanProjectConfigIdResolver.resolve(templateType);
            List<String> list = configService.findAllData(scanConfigType);
            result.addAll(list);
        }

        return result;
    }

}
