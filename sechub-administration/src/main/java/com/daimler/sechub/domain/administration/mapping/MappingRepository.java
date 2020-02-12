 // SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.mapping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daimler.sechub.sharedkernel.error.NotFoundException;

public interface MappingRepository extends JpaRepository<Mapping, String>{

	public default Mapping findOrFailStatusEntry(String key) {
		Optional<Mapping> foundUser  = findById(key);
		if (! foundUser.isPresent()) {
			throw new NotFoundException("ScanMapping '" + key + "' not found!");
		}
		return foundUser.get();
	}

}
