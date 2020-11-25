// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.mapping;

import static com.daimler.sechub.sharedkernel.validation.AssertValidResult.*;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesMappingConfiguration;
import com.daimler.sechub.sharedkernel.validation.MappingDataValidation;
import com.daimler.sechub.sharedkernel.validation.MappingIdValidation;

@Service
public class FetchMappingService {

    @Autowired
    MappingRepository repository;

    @Autowired
    MappingIdValidation mappingIdValidation;

    @Autowired
    MappingDataValidation mappingDataValidation;

    @UseCaseAdminFetchesMappingConfiguration(@Step(number = 2, name = "Service call", description = "Services fetches data from database, if not set an empty mapping data result will be returned"))
    public MappingData fetchMappingData(String mappingId) {
        assertValid(mappingIdValidation.validate(mappingId), "Mapping ID invalid");

        Optional<Mapping> mapping = repository.findById(mappingId);
        if (!mapping.isPresent()) {
            return new MappingData();
        }
        String json = mapping.get().getData();
        return MappingData.fromString(json);

    }

}
