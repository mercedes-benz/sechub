// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.status;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListStatusService {

    @Autowired
    StatusEntryRepository repository;

    public List<StatusEntry> fetchAllStatusEntries() {
        return repository.findAll();
    }

}
