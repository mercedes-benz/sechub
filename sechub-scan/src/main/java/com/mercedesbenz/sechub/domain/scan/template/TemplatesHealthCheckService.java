package com.mercedesbenz.sechub.domain.scan.template;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition;
import com.mercedesbenz.sechub.domain.scan.asset.AssetDetailData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetFileData;
import com.mercedesbenz.sechub.domain.scan.asset.AssetService;
import com.mercedesbenz.sechub.domain.scan.asset.ProductExecutorConfigAssetFileNameService;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfile;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutionProfileRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

@Service
public class TemplatesHealthCheckService {

    private static final String AT_LEAST_ONE_CONFIG_IS_ENABLED = "At least one executor config is enabled.";
    private static final String AT_LEAST_ONE_CONFIG_IS_NOT_ENABLED = "At least one executor config is not enabled.";
    private static final String AT_LEAST_ONE_PROFILE_IS_ENABLED = "At least one profile is enabled.";
    private static final String AT_LEAST_ONE_PROFILE_IS_NOT_ENABLED = "At least one profile is not enabled.";
    private static final String AT_LEAST_ONE_PROFILE_AND_EXECPUTOR_COMBINATION_ENABLED = "At least one combination of executor and profile is enabled.";

    private static final Logger LOG = LoggerFactory.getLogger(TemplatesHealthCheckService.class);

    /* necessary for template data */
    private final TemplateService templateService;

    /* necessary for asset access */
    private final AssetService assetService;

    /* necessary for profile searching */
    private final ProductExecutionProfileRepository profileRepository;

    /* necessary to filter relevant template definitions */
    private final RelevantScanTemplateDefinitionFilter templateDefinitionFilter;

    /*
     * necessary to filter/resolve asset file names for product executor
     * configurations
     */
    private final ProductExecutorConfigAssetFileNameService assetFileNameService;

/* @formatter:off */
    public TemplatesHealthCheckService(
            TemplateService templateService,
            AssetService assetService,
            ProductExecutionProfileRepository profileRepository,
            RelevantScanTemplateDefinitionFilter templateDefinitionFilter,
            ProductExecutorConfigAssetFileNameService assetFileNameService

    ) {
        this.templateService = requireNonNull(templateService);
        this.assetService = requireNonNull(assetService);
        this.profileRepository = requireNonNull(profileRepository);
        this.templateDefinitionFilter = requireNonNull(templateDefinitionFilter);
        this.assetFileNameService = requireNonNull(assetFileNameService);
    }
    /* @formatter:on */

    public TemplatesHealthCheckResult executeHealthCheck() {
        TemplatesHealthCheckResult result = new TemplatesHealthCheckResult();

        Set<String> assignedTemplateIds = templateService.fetchAllAssignedTemplateIds();

        Set<String> allTemplateIds = new HashSet<>(templateService.fetchAllTemplateIds());

        /*
         * Step 1: check if there is a template defined which is not assigned to a
         * project (means never used)
         */
        handleUnassignedTemplates(result, assignedTemplateIds, allTemplateIds);

        /*
         * Step 2: check there is a template id assigned to a project but template does
         * not exist
         */
        handleNonExistingButAssignedTemplates(result, assignedTemplateIds, allTemplateIds);

        /**
         * Step 3: handle problems between template definitions and executor
         * configurations. An example: A PDS executor configuration has a defined
         * pdsProductIdentifierId which leads to the filename which is necessary to be
         * inside the asset. The template only knows the asset id.
         *
         * When an administrator configures the setup and uses a wrong upload file name,
         * the executor will not work!
         */
        handleTemplateExecutorConfigProblems(result, allTemplateIds);

        /* finalize */
        addCalculatedStatus(result);

        return result;
    }

    private void handleTemplateExecutorConfigProblems(TemplatesHealthCheckResult result, Set<String> allTemplateIds) {
        Map<String, TemplateHealthCheckEntry> templateExecutorConfigProblemMap = new HashMap<>();

        /*
         * Step 3 :check for problems of template + executor configurations (template
         * knows asset, executor are responsible for file name...)
         */
        for (String templateId : allTemplateIds) {
            LOG.debug("Inspect template: {}", templateId);

            TemplateDefinition templateDefinition = templateService.fetchTemplateDefinition(templateId);
            String assetId = templateDefinition.getAssetId();

            List<AssetFileData> assetFileDataList = null;
            try {

                AssetDetailData details = assetService.fetchAssetDetails(assetId);
                if (details != null) {
                    assetFileDataList = details.getFiles();
                }
            } catch (NotFoundException e) {
                LOG.trace("Did not found asset details for asset with id:{}", e);
            } catch (Exception e) {
                LOG.error("Unexpected problem appeared, will handle like not found", e);
            }
            if (assetFileDataList == null) {
                assetFileDataList = new ArrayList<>();
            }
            Set<String> projectIds = templateService.fetchProjectsUsingTemplate(templateId);

            for (String projectId : projectIds) {
                List<ProductExecutionProfile> profiles = profileRepository.findExecutionProfilesForProject(projectId);

                for (ProductExecutionProfile profile : profiles) {
                    Set<ProductExecutorConfig> configurations = profile.getConfigurations();

                    for (ProductExecutorConfig config : configurations) {

                        ProductIdentifier productIdentifier = config.getProductIdentifier();
                        ScanType scanType = productIdentifier.getType();

                        boolean productSupportsTemplates = templateDefinitionFilter.isProductAbleToHandleTemplates(productIdentifier);
                        if (!productSupportsTemplates) {
                            continue;
                        }

                        boolean scanTypeSupportsTemplates = templateDefinitionFilter.isScanTypeSupportingTemplate(scanType, templateDefinition);
                        if (!scanTypeSupportsTemplates) {
                            continue;
                        }
                        String fileName = assetFileNameService.resolveAssetFileName(config);
                        if (fileName == null) {
                            fileName = "<error: assetFileNameService was not able to resolve filename for config/>"; // just to have a value inside output
                                                                                                                     // result.
                        }

                        boolean exists = false;
                        for (AssetFileData fileData : assetFileDataList) {
                            if (fileName.equals(fileData.getFileName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {

                            boolean configNotActive = Boolean.FALSE.equals(config.getEnabled());
                            boolean profileNotActive = Boolean.FALSE.equals(profile.getEnabled());

                            TemplateHealthCheckEntry problem = ensureProblemInMap(templateExecutorConfigProblemMap, templateDefinition, config);
                            problem.getProfiles().add(profile.getId()); // it is a set to duplication does not matter
                            problem.getProjects().add(projectId);

                            if (problem.getFileName() == null) {
                                /* not already set - do it */
                                problem.setFileName(fileName);
                                problem.setDescription("The file '" + problem.getAssetId() + "/" + problem.getFileName() + "' does not exist!");
                                problem.setSolution("Upload a file '" + problem.getFileName() + "' to asset folder '" + problem.getAssetId() + "'");
                            }

                            if (!TemplateHealthCheckProblemType.ERROR.equals(problem.getType())) {
                                /* not already marked as an error - mark it */
                                if (configNotActive || profileNotActive) {
                                    problem.setType(TemplateHealthCheckProblemType.WARNING);
                                } else {
                                    problem.setType(TemplateHealthCheckProblemType.ERROR);
                                    problem.getHints().add(AT_LEAST_ONE_PROFILE_AND_EXECPUTOR_COMBINATION_ENABLED);
                                }
                            }

                            if (configNotActive) {
                                problem.getHints().add(AT_LEAST_ONE_CONFIG_IS_NOT_ENABLED);
                            } else {
                                problem.getHints().add(AT_LEAST_ONE_CONFIG_IS_ENABLED);
                            }
                            if (profileNotActive) {
                                problem.getHints().add(AT_LEAST_ONE_PROFILE_IS_NOT_ENABLED);
                            } else {
                                problem.getHints().add(AT_LEAST_ONE_PROFILE_IS_ENABLED);
                            }
                        }
                    }

                }
            }
        }
        /* append all */
        result.getEntries().addAll(templateExecutorConfigProblemMap.values());
    }

    private void addCalculatedStatus(TemplatesHealthCheckResult result) {
        result.setStatus(TemplatesHealthCheckStatus.OK); // initial ...

        for (TemplateHealthCheckEntry entry : result.getEntries()) {

            switch (entry.getType()) {
            case ERROR:
                result.setStatus(TemplatesHealthCheckStatus.ERROR);
                return; /* we break here - error level reached */
            case WARNING:
                result.setStatus(TemplatesHealthCheckStatus.WARNING);
                break;
            case INFO:
            default:
                /* just do nothing */
            }
        }
    }

    private void handleNonExistingButAssignedTemplates(TemplatesHealthCheckResult result, Set<String> assignedTemplateIds, Set<String> allTemplateIds) {
        for (String assignedTemplateId : assignedTemplateIds) {
            if (!allTemplateIds.contains(assignedTemplateId)) {
                /*
                 * Problem detected - the assignment is not valid because template does not
                 * exist
                 */
                TemplateHealthCheckEntry entry = new TemplateHealthCheckEntry();
                entry.setTemplateId(assignedTemplateId);
                entry.setDescription("A template with id '" + assignedTemplateId + "' does not exist but is assigned to project(s)");
                entry.setSolution("Either unassign template id from project(s) or create a template with the identifier.");
                entry.setType(TemplateHealthCheckProblemType.ERROR);

                Set<String> projectIds = templateService.fetchProjectsUsingTemplate(assignedTemplateId);
                entry.getProjects().addAll(projectIds);

                result.getEntries().add(entry);

            }

        }
    }

    private void handleUnassignedTemplates(TemplatesHealthCheckResult result, Set<String> assignedTemplateIds, Set<String> allTemplateIds) {
        List<String> unassignedTemplateIds = allTemplateIds.stream().filter(element -> !assignedTemplateIds.contains(element)).toList();

        for (String unassignedTemplateId : unassignedTemplateIds) {
            TemplateHealthCheckEntry problem = new TemplateHealthCheckEntry();
            problem.setType(TemplateHealthCheckProblemType.INFO);
            problem.setTemplateId(unassignedTemplateId);
            problem.setDescription("The template is defined, but not assigned to any project");
            problem.setSolution("This is just a hint that the template is not assigned/used.");

            result.getEntries().add(problem);
        }
    }

    private TemplateHealthCheckEntry ensureProblemInMap(Map<String, TemplateHealthCheckEntry> map, TemplateDefinition definition,
            ProductExecutorConfig config) {
        String templateId = definition.getId();
        String configId = config.getUUID().toString();

        String identifier = templateId + "_" + configId;

        TemplateHealthCheckEntry executorConfigProblem = map.get(identifier);
        if (executorConfigProblem == null) {

            executorConfigProblem = new TemplateHealthCheckEntry();
            executorConfigProblem.setExecutorConfigUUID(configId);
            executorConfigProblem.setTemplateId(templateId);
            executorConfigProblem.setAssetId(definition.getAssetId());

            map.put(identifier, executorConfigProblem);

        }
        return executorConfigProblem;
    }

}
