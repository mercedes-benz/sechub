// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.mapping;

import static com.daimler.sechub.sharedkernel.validation.AssertValidResult.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MappingMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdmiUpdatesMappingConfiguration;
import com.daimler.sechub.sharedkernel.validation.MappingDataValidation;
import com.daimler.sechub.sharedkernel.validation.MappingIdValidation;

@Service
public class UpdateMappingService {

    @Autowired
    MappingRepository repository;
    
    @Autowired
    MappingTransactionService mappingTransactionService;

    @Autowired
    MappingIdValidation mappingIdValidation;

    @Autowired
    MappingDataValidation mappingDataValidation;
    
    @Autowired
    DomainMessageService eventBus;

    @UseCaseAdmiUpdatesMappingConfiguration(@Step(number = 2, name = "Service call", description = "Services updates data in database and sends event"))
    public void updateMapping(String mappingId, MappingData mappingData) {
        assertValid(mappingIdValidation.validate(mappingId), "Mapping ID invalid");
        assertValid(mappingDataValidation.validate(mappingData), "Mapping Data invalid");

        Optional<Mapping> mapping = repository.findById(mappingId);
        Mapping mappingObj = null;
        if (!mapping.isPresent()) {
            mappingObj = new Mapping(mappingId);
        } else {
            mappingObj = mapping.get();
        }
        mappingObj.setData(mappingData.toJSON());
        
        mappingTransactionService.saveMappingInOwnTransaction(mappingObj);

        sendEvent(mappingObj);
    }

    @IsSendingAsyncMessage(MessageID.MAPPING_CONFIGURATION_CHANGED)
    private void sendEvent(Mapping mapping) {
       
        MappingMessage mappingMessage = new MappingMessage();
        mappingMessage.setMappingId(mapping.getId());
        mappingMessage.setMappingData(MappingData.fromString(mapping.getData()));
        
        DomainMessage request = DomainMessageFactory.createEmptyRequest(MessageID.MAPPING_CONFIGURATION_CHANGED);
        request.set(MessageDataKeys.CONFIG_MAPPING_DATA, mappingMessage);
        
        eventBus.sendAsynchron(request);
        
    }

}
