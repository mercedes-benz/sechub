// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.access.ScanDeleteAnyAccessToProjectAtAllService;
import com.daimler.sechub.domain.scan.access.ScanGrantUserAccessToProjectService;
import com.daimler.sechub.domain.scan.access.ScanRevokeUserAccessAtAllService;
import com.daimler.sechub.domain.scan.access.ScanRevokeUserAccessFromProjectService;
import com.daimler.sechub.domain.scan.config.UpdateScanConfigService;
import com.daimler.sechub.domain.scan.product.ProductResultService;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier.MappingType;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsRecevingSyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.daimler.sechub.sharedkernel.messaging.MappingMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.ProjectMessage;
import com.daimler.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.UserMessage;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesMappingConfiguration;

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
    UpdateScanConfigService updateScanMappingService;

    @Autowired
    ProductResultService productResultService;

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
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @Override
    public DomainMessageSynchronousResult receiveSynchronMessage(DomainMessage request) {
        MessageID messageId = request.getMessageId();
        LOG.debug("received synchron domain request: {}", request);
        switch (messageId) {

        case REQUEST_PURGE_JOB_RESULTS:
            return handleJobRestartHardRequested(request);
            
        default:
            throw new IllegalStateException("unhandled message id:" + messageId);
        }
    }

    @IsRecevingSyncMessage(MessageID.REQUEST_PURGE_JOB_RESULTS)
    private DomainMessageSynchronousResult handleJobRestartHardRequested(DomainMessage request) {
        UUID jobUUID = request.get(MessageDataKeys.SECHUB_UUID);
        try {
            /* delete all former results */
            productResultService.deleteAllResultsForJob(jobUUID);
            return purgeDone(jobUUID);
        }catch(Exception e) {
            LOG.error("Was not able to purge results for job {}",e);
            return purgeFailed(jobUUID,e);
        }
    }
    
    @IsSendingSyncMessageAnswer(value=MessageID.JOB_RESULT_PURGE_FAILED, answeringTo = MessageID.REQUEST_PURGE_JOB_RESULTS, branchName = "failed")
    private DomainMessageSynchronousResult purgeFailed(UUID jobUUID, Exception e) {
        DomainMessageSynchronousResult result = new DomainMessageSynchronousResult(MessageID.JOB_RESULT_PURGE_FAILED,e);
        result.set(MessageDataKeys.SECHUB_UUID, jobUUID);
        return result;
    }
    
    @IsSendingSyncMessageAnswer(value=MessageID.JOB_RESULT_PURGE_DONE, answeringTo = MessageID.REQUEST_PURGE_JOB_RESULTS, branchName = "success")
    private DomainMessageSynchronousResult purgeDone(UUID jobUUID) {
        DomainMessageSynchronousResult result =  new DomainMessageSynchronousResult(MessageID.JOB_RESULT_PURGE_DONE);
        result.set(MessageDataKeys.SECHUB_UUID, jobUUID);
        return result;
    }

    @IsReceivingAsyncMessage(MessageID.MAPPING_CONFIGURATION_CHANGED)
    @UseCaseAdministratorUpdatesMappingConfiguration(@Step(number = 3, name = "Event handler", description = "Receives mapping configuration change event"))
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
