// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.status;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;

public interface StatusEntryRepository extends JpaRepository<StatusEntry, String> {

    public default Optional<StatusEntry> findByStatusEntryKey(StatusEntryKey key) {
        return findById(key.getStatusEntryKey());
    }

    public default StatusEntry findOrFailStatusEntry(StatusEntryKey key) {
        return findOrFailStatusEntry(key.getStatusEntryKey());
    }

    public default StatusEntry findOrFailStatusEntry(String key) {
        Optional<StatusEntry> foundUser = findById(key);
        if (!foundUser.isPresent()) {
            throw new NotFoundException("StatusEntry '" + key + "' not found!");
        }
        return foundUser.get();
    }

}
