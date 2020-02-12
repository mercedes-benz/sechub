// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.mapping;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorFetchesMappingConfiguration;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdministratorUpdatesMappingConfiguration;
import com.daimler.sechub.sharedkernel.validation.MappingDataValidation;
import com.daimler.sechub.sharedkernel.validation.MappingIdValidation;

import static com.daimler.sechub.sharedkernel.validation.AssertValidResult.*;

@Service
public class MappingService {

    @Autowired
    MappingRepository repository;

    @Autowired
    MappingIdValidation mappingIdValidation;

    @Autowired
    MappingDataValidation mappingDataValidation;

    public List<Mapping> fetchAllStatusEntries() {
        return repository.findAll();
    }

    @UseCaseAdministratorFetchesMappingConfiguration(@Step(number = 2, name = "Service call", description = "Services fetches data from database, if not set an empty mapping data result will be returned"))
    public MappingData fetchMappingData(String mappingId) {
        assertValid(mappingIdValidation.validate(mappingId), "Mapping ID invalid");

        Optional<Mapping> mapping = repository.findById(mappingId);
        if (!mapping.isPresent()) {
            return new MappingData();
        }
        String json = mapping.get().getData();
        return MappingData.fromString(json);

    }

    @UseCaseAdministratorUpdatesMappingConfiguration(@Step(number = 2, name = "Service call", description = "Services updates data in database"))
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
        repository.save(mappingObj);

    }

}
