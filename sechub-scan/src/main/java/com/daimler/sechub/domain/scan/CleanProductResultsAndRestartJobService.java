package com.daimler.sechub.domain.scan;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.scan.product.ProductResultService;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.JobMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.job.UseCaseAdministratorRestartsJobHard;

@Service
public class CleanProductResultsAndRestartJobService {

    @Autowired
    @Lazy
    DomainMessageService eventBus;
    
    @Autowired
    ProductResultService productResultService;

    @UseCaseAdministratorRestartsJobHard(@Step(number = 3, name = "Cleanup job and restart", description = "Cleanup former product resuls and trigger job restart"))
    public void cleanJobResultsAndRestart(UUID jobUUID, String ownerEmailAddress) {
        /* delete all former results*/
        productResultService.deleteAllResultsForJob(jobUUID);
        
        /* after no longer having results trigger restart */
        sendRequestJobRestartMessage(jobUUID, ownerEmailAddress);
        
    }

    @IsSendingAsyncMessage(MessageID.REQUEST_JOB_RESTART)
    private void sendRequestJobRestartMessage(UUID jobUUID, String ownerEmailAddress) {
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.REQUEST_JOB_RESTART);
        
        JobMessage jobMessage = new JobMessage();
        jobMessage.setJobUUID(jobUUID);
        jobMessage.setOwnerEmailAddress(ownerEmailAddress);
        
        request.set(MessageDataKeys.JOB_RESTART_DATA, jobMessage);
        
        eventBus.sendAsynchron(request);
    }
    
    
}
