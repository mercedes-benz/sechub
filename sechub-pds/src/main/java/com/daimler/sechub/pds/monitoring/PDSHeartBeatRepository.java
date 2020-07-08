package com.daimler.sechub.pds.monitoring;

import java.time.LocalDateTime;
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
}
