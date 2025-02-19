// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.domain.scan.access.ScanDeleteAnyAccessToProjectAtAllService;
import com.mercedesbenz.sechub.domain.scan.access.ScanGrantUserAccessToProjectService;
import com.mercedesbenz.sechub.domain.scan.access.ScanRevokeUserAccessAtAllService;
import com.mercedesbenz.sechub.domain.scan.access.ScanRevokeUserAccessFromProjectService;
import com.mercedesbenz.sechub.domain.scan.config.ScanConfigService;
import com.mercedesbenz.sechub.domain.scan.config.UpdateScanMappingConfigurationService;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigAccessLevelService;
import com.mercedesbenz.sechub.domain.scan.template.TemplateService;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier.MappingType;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.mercedesbenz.sechub.sharedkernel.messaging.MappingMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.ProjectMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.UserMessage;
import com.mercedesbenz.sechub.sharedkernel.project.ProjectAccessLevel;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectTemplateData;
import com.mercedesbenz.sechub.sharedkernel.template.SecHubProjectToTemplate;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdmiUpdatesMappingConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseAdminChangesProjectAccessLevel;

@Component
public class ScanMessageHandler implements AsynchronMessageHandler, SynchronMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ScanMessageHandler.class);

    @Autowired
    ScanGrantUserAccessToProjectService grantService;

    @Autowired
    ScanRevokeUserAccessFromProjectService revokeUserFromProjectService;

    @Autowired
    ScanRevokeUserAccessAtAllService revokeUserService;

    @Autowired
    ScanDeleteAnyAccessToProjectAtAllService deleteAllProjectAccessService;

    @Autowired
    ProjectDataDeleteService projectDataDeleteService;

    @Autowired
    UpdateScanMappingConfigurationService updateScanMappingService;

    @Autowired
    ProductResultService productResultService;

    @Autowired
    ScanProjectConfigAccessLevelService projectAccessLevelService;

    @Autowired
    ScanConfigService configService;

    @Autowired
    TemplateService templateService;

    @Autowired
    ScanSecHubConfigurationRuntimeInspector configurationRuntimeInspector;

    @Override
    public void receiveAsyncMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received domain request: {}", request);

        switch (messageId) {
        case USER_ADDED_TO_PROJECT:
            handleUserAddedToProject(request);
            break;
        case USER_REMOVED_FROM_PROJECT:
            handleUserRemovedFromProject(request);
            break;
        case USER_DELETED:
            handleUserDeleted(request);
            break;
        case PROJECT_DELETED:
            handleProjectDeleted(request);
            break;
        case MAPPING_CONFIGURATION_CHANGED:
            handleMappingConfigurationChanged(request);
            break;
        case PROJECT_ACCESS_LEVEL_CHANGED:
            handleProcessAccessLevelChanged(request);
            break;
        case AUTO_CLEANUP_CONFIGURATION_CHANGED:
            handleAutoCleanUpConfigurationChanged(request);
            break;
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsReceivingAsyncMessage(MessageID.AUTO_CLEANUP_CONFIGURATION_CHANGED)
    private void handleAutoCleanUpConfigurationChanged(DomainMessage request) {
        AdministrationConfigMessage message = request.get(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA);
        configService.updateAutoCleanupInDays(message.getAutoCleanupInDays());
    }

    @Override
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received synchron domain request: {}", request);
        switch (messageId) {

        case REQUEST_PURGE_JOB_RESULTS:
            return handleJobRestartHardRequested(request);
        case REQUEST_ASSIGN_TEMPLATE_TO_PROJECT:
            return handleAssignTemplateToProjectRequest(request);
        case REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT:
            return handleUnassignTemplateFromProjectRequest(request);
        case REQUEST_FULL_CONFIGURATION_VALIDATION:
            return handleFullConfigurationValidation(request);
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsRecevingSyncMessage(MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT)
    private DomainMessageSynchronousResult handleAssignTemplateToProjectRequest(DomainMessage request) {
        SecHubProjectToTemplate projectToTemplate = request.get(MessageDataKeys.PROJECT_TO_TEMPLATE);
        String templateId = projectToTemplate.getTemplateId();
        String projectId = projectToTemplate.getProjectId();

        try {
            templateService.assignTemplateToProject(templateId, projectId);
            Set<String> templateIds = templateService.fetchAssignedTemplateIdsForProject(projectId);
            return templateAssignmentDone(projectId, templateIds);
        } catch (Exception e) {
            return templateAssignmentFailed(e);
        }

    }

    @IsRecevingSyncMessage(MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT)
    private DomainMessageSynchronousResult handleUnassignTemplateFromProjectRequest(DomainMessage request) {
        SecHubProjectToTemplate projectToTemplate = request.get(MessageDataKeys.PROJECT_TO_TEMPLATE);
        String templateId = projectToTemplate.getTemplateId();
        String projectId = projectToTemplate.getProjectId();

        try {
            templateService.unassignTemplateFromProject(templateId, projectId);
            Set<String> templateIds = templateService.fetchAssignedTemplateIdsForProject(projectId);
            return templateUnassignmentDone(projectId, templateIds);
        } catch (Exception e) {
            return templateUnassignmentFailed(e);
        }

    }

    @IsRecevingSyncMessage(MessageID.REQUEST_PURGE_JOB_RESULTS)
    private DomainMessageSynchronousResult handleJobRestartHardRequested(DomainMessage request) {
        UUID jobUUID = request.get(MessageDataKeys.SECHUB_JOB_UUID);
        try {
            /* delete all former results */
            productResultService.deleteAllResultsForJob(jobUUID);
            return purgeDone(jobUUID);
        } catch (Exception e) {
            LOG.error("Was not able to purge results for job {}", jobUUID, e);
            return purgeFailed(jobUUID, e);
        }
    }

    @IsRecevingSyncMessage(MessageID.REQUEST_FULL_CONFIGURATION_VALIDATION)
    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_FULL_CONFIGURATION_VALIDATION, answeringTo = MessageID.REQUEST_FULL_CONFIGURATION_VALIDATION, branchName = "success")
    private DomainMessageSynchronousResult handleFullConfigurationValidation(DomainMessage request) {
        SecHubConfiguration config = request.get(MessageDataKeys.SECHUB_UNENCRYPTED_CONFIG);

        /* start inspection */
        SecHubMessagesList inspectionList = configurationRuntimeInspector.inspect(config);

        /* return inspection messages inside result */
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.RESULT_FULL_CONFIGURATION_VALIDATION);
        result.set(MessageDataKeys.ERROR_MESSAGES, inspectionList);
        return result;
    }

    @IsSendingSyncMessageAnswer(value = MessageID.JOB_RESULT_PURGE_FAILED, answeringTo = MessageID.REQUEST_PURGE_JOB_RESULTS, branchName = "failed")
    private DomainMessageSynchronousResult purgeFailed(UUID jobUUID, Exception e) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.JOB_RESULT_PURGE_FAILED, e);
        result.set(MessageDataKeys.SECHUB_JOB_UUID, jobUUID);
        return result;
    }

    @IsSendingSyncMessageAnswer(value = MessageID.JOB_RESULT_PURGE_DONE, answeringTo = MessageID.REQUEST_PURGE_JOB_RESULTS, branchName = "success")
    private DomainMessageSynchronousResult purgeDone(UUID jobUUID) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.JOB_RESULT_PURGE_DONE);
        result.set(MessageDataKeys.SECHUB_JOB_UUID, jobUUID);
        return result;
    }

    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT, answeringTo = MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT, branchName = "success")
    private DomainMessageSynchronousResult templateAssignmentDone(String projectId, Set<String> assignedTemplates) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT);
        SecHubProjectTemplateData templates = new SecHubProjectTemplateData();
        templates.setProjectId(projectId);
        templates.getTemplateIds().addAll(assignedTemplates);

        result.set(MessageDataKeys.PROJECT_TEMPLATES, templates);
        return result;
    }

    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT, answeringTo = MessageID.REQUEST_ASSIGN_TEMPLATE_TO_PROJECT, branchName = "failed")
    private DomainMessageSynchronousResult templateAssignmentFailed(Exception failure) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.RESULT_ASSIGN_TEMPLATE_TO_PROJECT, failure);
        return result;
    }

    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT, answeringTo = MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT, branchName = "success")
    private DomainMessageSynchronousResult templateUnassignmentDone(String projectId, Set<String> assignedTemplates) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT);
        SecHubProjectTemplateData templates = new SecHubProjectTemplateData();
        templates.setProjectId(projectId);
        templates.getTemplateIds().addAll(assignedTemplates);

        result.set(MessageDataKeys.PROJECT_TEMPLATES, templates);
        return result;
    }

    @IsSendingSyncMessageAnswer(value = MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT, answeringTo = MessageID.REQUEST_UNASSIGN_TEMPLATE_FROM_PROJECT, branchName = "failed")
    private DomainMessageSynchronousResult templateUnassignmentFailed(Exception failure) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.RESULT_UNASSIGN_TEMPLATE_FROM_PROJECT, failure);
        return result;
    }

    @IsReceivingAsyncMessage(MessageID.MAPPING_CONFIGURATION_CHANGED)
    @UseCaseAdmiUpdatesMappingConfiguration(@Step(number = 3, name = "Event handler", description = "Receives mapping configuration change event"))
    private void handleMappingConfigurationChanged(DomainMessage request) {
        MappingMessage data = request.get(MessageDataKeys.CONFIG_MAPPING_DATA);

        String mappingId = data.getMappingId();
        MappingIdentifier found = MappingIdentifier.getIdentifierOrNull(mappingId);
        if (found == null) {
            LOG.error("Mapping identifier with id:{} does not exist!", mappingId);
            return;
        }
        /* filter only relevant parts - message may contain uninteresting stuff */
        if (!found.hasTypeContainedIn(MappingType.ADAPTER_CONFIGURATION, MappingType.COMMON_CONFIGURATION)) {
            LOG.debug("Mapping with id:{} is not relevant for cluster configuration and so ignored.", mappingId);
            return;
        }

        updateScanMappingService.updateScanMapping(mappingId, data.getMappingData());
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_ACCESS_LEVEL_CHANGED)
    @UseCaseAdminChangesProjectAccessLevel(@Step(number = 3, name = "Event handler", description = "Receives change project access level event"))
    private void handleProcessAccessLevelChanged(DomainMessage request) {
        ProjectMessage data = request.get(MessageDataKeys.PROJECT_ACCESS_LEVEL_CHANGE_DATA);

        String projectId = data.getProjectId();
        ProjectAccessLevel formerAccessLevel = data.getFormerAccessLevel();
        ProjectAccessLevel newAccessLevel = data.getNewAccessLevel();

        projectAccessLevelService.changeProjectAccessLevel(projectId, newAccessLevel, formerAccessLevel);
    }

    @IsReceivingAsyncMessage(MessageID.USER_ADDED_TO_PROJECT)
    private void handleUserAddedToProject(DomainMessage request) {
        UserMessage data = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
        grantService.grantUserAccessToProject(data.getUserId(), data.getProjectId());
    }

    @IsReceivingAsyncMessage(MessageID.USER_REMOVED_FROM_PROJECT)
    private void handleUserRemovedFromProject(DomainMessage request) {
        UserMessage data = request.get(MessageDataKeys.PROJECT_TO_USER_DATA);
        revokeUserFromProjectService.revokeUserAccessFromProject(data.getUserId(), data.getProjectId());
    }

    @IsReceivingAsyncMessage(MessageID.USER_DELETED)
    private void handleUserDeleted(DomainMessage request) {
        UserMessage data = request.get(MessageDataKeys.USER_DELETE_DATA);
        revokeUserService.revokeUserAccess(data.getUserId());
    }

    @IsReceivingAsyncMessage(MessageID.PROJECT_DELETED)
    private void handleProjectDeleted(DomainMessage request) {
        ProjectMessage data = request.get(MessageDataKeys.PROJECT_DELETE_DATA);
        /* first cut access */
        deleteAllProjectAccessService.deleteAnyAccessDataForProject(data.getProjectId());
        /* now delete data */
        projectDataDeleteService.deleteAllDataForProject(data.getProjectId());
    }

}
