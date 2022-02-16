// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.whitelist;

import static com.daimler.sechub.domain.schedule.whitelist.ProjectWhitelistEntry.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.daimler.sechub.domain.schedule.whitelist.ProjectWhitelistEntry.ProjectWhiteListEntryCompositeKey;

public interface ProjectWhitelistEntryRepository extends JpaRepository<ProjectWhitelistEntry, ProjectWhiteListEntryCompositeKey> {

    @Query(value = "SELECT * FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    public List<ProjectWhitelistEntry> fetchWhiteListEntriesForProject(String projectId);

    @Modifying
    @Query(value = "DELETE FROM " + TABLE_NAME + " where " + COLUMN_PROJECT_ID + " = ?1", nativeQuery = true)
    public void deleteAllEntriesForProject(String projectId);
}
