// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.config;

import static com.daimler.sechub.sharedkernel.validation.AssertValidResult.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesMappingConfiguration;
import com.daimler.sechub.sharedkernel.validation.MappingDataValidation;
import com.daimler.sechub.sharedkernel.validation.MappingIdValidation;

@Service
public class UpdateScanConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateScanConfigService.class);
    
    @Autowired
    ScanMappingRepository repository;

    @Autowired
    ScanConfigService scanConfigService;
    
    @Autowired
    MappingIdValidation mappingIdValidation;

    @Autowired
    MappingDataValidation mappingDataValidation;
    
    @Autowired
    @Lazy
    DomainMessageService eventBus;

    @UseCaseAdministratorUpdatesMappingConfiguration(@Step(number=4,name="Service call",description="Updates scan mapping in DB"))
    public void updateScanMapping(String mappingId, MappingData mappingData) {
        assertValid(mappingIdValidation.validate(mappingId), "Mapping ID invalid");
        assertValid(mappingDataValidation.validate(mappingData), "Mapping Data invalid");

        updateInDatabase(mappingId, mappingData);
        
        /* that's all - refresh is done by ScanConfigRefreshTriggerService - but only when something has changed ...*/
    }
    
    private void updateInDatabase(String mappingId, MappingData mappingData) {
        Optional<ScanMapping> mapping = repository.findById(mappingId);
        ScanMapping mappingObj = null;
        if (!mapping.isPresent()) {
            mappingObj = new ScanMapping(mappingId);
        } else {
            mappingObj = mapping.get();
        }
        String json = mappingData.toJSON();
        mappingObj.setData(json);
        
        repository.save(mappingObj);
        LOG.info("Updated scan mapping in database. Id:{} ws updated to:{}", mappingId, json);
        
    }

}
