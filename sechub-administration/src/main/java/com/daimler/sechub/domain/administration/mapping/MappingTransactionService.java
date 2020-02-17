package com.daimler.sechub.domain.administration.mapping;

import static java.util.Objects.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MappingTransactionService {
    @Autowired
    MappingRepository repository;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mapping saveMappingInOwnTransaction(Mapping mapping) {
        requireNonNull(mapping, "Mapping may not be null!");
        return repository.save(mapping);
    }
    
}
