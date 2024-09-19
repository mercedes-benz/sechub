// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.mapping;

import static com.mercedesbenz.sechub.sharedkernel.validation.AssertValidResult.assertValid;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.config.UseCaseAdminFetchesMappingConfiguration;
import com.mercedesbenz.sechub.sharedkernel.validation.MappingDataValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.MappingIdValidation;

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
        if (mapping.isEmpty()) {
            return new MappingData();
        }
        String json = mapping.get().getData();
        return MappingData.fromString(json);

    }

}
