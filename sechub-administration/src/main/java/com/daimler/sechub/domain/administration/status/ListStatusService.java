package com.daimler.sechub.domain.administration.status;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ListStatusService {

	StatusEntryRepository repository;

	public List<StatusEntry> fetchAllStatusEntries(){
		return repository.findAll();
	}

}
