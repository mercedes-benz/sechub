// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ScheduleCipherPoolDataRepository extends JpaRepository<ScheduleCipherPoolData, Integer> {

    @Query("select " + ScheduleCipherPoolData.PROPERTY_ID + " from #{#entityName}")
    public Set<Long> fetchAllCipherPoolIds();
}
