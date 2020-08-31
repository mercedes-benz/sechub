// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface PDSHeartBeatRepository extends JpaRepository<PDSHeartBeat, UUID>{

    @Modifying
    @Transactional
    @Query("DELETE FROM PDSHeartBeat h WHERE h.updated < :date")
    int removeOlderThan(@Param("date") LocalDateTime date);
    
    
    /**
     * Finds all PDS heartbeats for given server id
     * @param serverId
     * @return list of heartbeats
     */
    List<PDSHeartBeat> findAllByServerId(String serverId); // spring data access to PDSHeartBeat.serverId
}
