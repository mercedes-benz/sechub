package com.daimler.sechub.pds.job;

import org.springframework.stereotype.Component;

import com.daimler.sechub.pds.PDSNotAcceptableException;

@Component
public class PDSConfigurationValidator {


    public void assertPDSConfigurationValid(PDSConfiguration configuration) {
       String message = createValidationErrorMessage(configuration);
       if (message==null) {
           return;
       }
       throw new PDSNotAcceptableException("Configuration invalid:"+message);
        
    }
    private String createValidationErrorMessage(PDSConfiguration configuration) {
        if (configuration==null) {
            return "may not be null!";
        }
        if (configuration.getSechubJobUUID()==null) {
            return "sechub job UUID not set!";
        }
        return null;
    }
}
