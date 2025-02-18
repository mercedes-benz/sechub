// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.model.template.TemplateData;
import com.mercedesbenz.sechub.commons.model.template.TemplateDataResolver;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;
import com.mercedesbenz.sechub.commons.model.template.TemplateUsageValidator;
import com.mercedesbenz.sechub.commons.model.template.TemplateUsageValidator.TemplateUsageValidatorResult;
import com.mercedesbenz.sechub.domain.scan.template.RelevantScanTemplateDefinitionFilter;
import com.mercedesbenz.sechub.domain.scan.template.TemplateService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Component
public class ScanSecHubConfigurationRuntimeInspector {

    private static final Logger LOG = LoggerFactory.getLogger(ScanSecHubConfigurationRuntimeInspector.class);

    private TemplateService templateService;
    private TemplateDataResolver templateDataResolver;
    private RelevantScanTemplateDefinitionFilter templateDefinitionFilter;
    private SecHubConfigurationModelSupport configurationModelSupport;
    private TemplateUsageValidator templateUsageValidator;

    ScanSecHubConfigurationRuntimeInspector(TemplateService templateService, RelevantScanTemplateDefinitionFilter templateDefinitionFilter,
            TemplateDataResolver templateDataResolver, SecHubConfigurationModelSupport configurationModelSupport,
            TemplateUsageValidator templateUsageValidator) {

        this.templateService = templateService;
        this.templateDefinitionFilter = templateDefinitionFilter;
        this.templateDataResolver = templateDataResolver;
        this.configurationModelSupport = configurationModelSupport;
        this.templateUsageValidator = templateUsageValidator;
    }

    /**
     * Inspects given configuration for runtime failures/problems.
     *
     * @param config
     * @return list, never <code>null</code>
     */
    public SecHubMessagesList inspect(SecHubConfiguration config) {
        LOG.debug("Start config inspection");
        SecHubMessagesList messagesList = new SecHubMessagesList();

        appendTemplateRelatedProblems(config, messagesList);

        return messagesList;
    }

    private void appendTemplateRelatedProblems(SecHubConfiguration config, SecHubMessagesList messagesList) {
        String projectId = config.getProjectId();
        List<TemplateDefinition> templateDefinitions = templateService.fetchAllTemplateDefinitionsForProject(projectId);

        if (templateDefinitions.isEmpty()) {
            /*
             * no templates defined for this project at all - we will do no further
             * processing here! If the user has defined template data or not, we stop here!
             */
            LOG.debug("No template definitions found for project: {} - skip processing", projectId);
            return;
        }

        /* check which scan types are wanted by configuration */
        Set<ScanType> scanTypes = configurationModelSupport.collectScanTypes(config);

        for (ScanType scanType : scanTypes) {
            List<TemplateDefinition> relevantDefinitions = templateDefinitionFilter.filter(templateDefinitions, scanType, config);

            for (TemplateDefinition relevant : relevantDefinitions) {
                inspectTemplateUsage(config, messagesList, relevant);

            }
        }

    }

    private void inspectTemplateUsage(SecHubConfiguration config, SecHubMessagesList messagesList, TemplateDefinition relevant) {

        LOG.debug("Inspect usage of templte: {} project: {}", relevant.getId(), config.getProjectId());

        TemplateType type = relevant.getType();
        TemplateData dataForType = templateDataResolver.resolveTemplateData(type, config);

        if (dataForType == null) {
            handleNoTemplateDataFound(type, relevant, messagesList);
        } else {
            handleTemplateDataUsage(messagesList, relevant, dataForType);
        }
    }

    private void handleTemplateDataUsage(SecHubMessagesList messagesList, TemplateDefinition relevant, TemplateData dataForType) {
        TemplateUsageValidatorResult result = templateUsageValidator.validate(relevant, dataForType);
        if (!result.isValid()) {
            messagesList.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.ERROR, result.getMessage()));
        }
    }

    private void handleNoTemplateDataFound(TemplateType type, TemplateDefinition relevant, SecHubMessagesList messagesList) {
        /* create info message with hint for mandatory variables */
        boolean mandatoryVariableDetected = false;
        StringBuilder sb = new StringBuilder();
        for (TemplateVariable variable : relevant.getVariables()) {
            if (variable.isOptional()) {
                continue;
            }
            if (!mandatoryVariableDetected) {
                sb.append("Please provide following mandatory variables:");
                mandatoryVariableDetected = true;
            }
            sb.append("\n- ");
            sb.append(variable.getName());
        }
        if (!mandatoryVariableDetected) {
            /* no variables necessary - so it is okay that user has not defined anything */
            return;
        }
        String message = """
                Template data missing. This is necessary to provide %s for the project.
                Please provide this kind of data inside your configuration file!
                %s
                """.formatted(type.getId(), sb);

        messagesList.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.ERROR, message));

    }

}
