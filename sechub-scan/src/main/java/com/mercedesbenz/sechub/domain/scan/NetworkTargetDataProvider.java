package com.mercedesbenz.sechub.domain.scan;

public interface NetworkTargetDataProvider {

    String getIdentifierWhenInternetTarget();

    String getIdentifierWhenIntranetTarget();
    
    String getBaseURLWhenInternetTarget();

    String getBaseURLWhenIntranetTarget();
    

    String getUsernameWhenInternetTarget();

    String getUsernameWhenIntranetTarget();

    
    String getPasswordWhenInternetTarget();

    String getPasswordWhenIntranetTarget();

    
    boolean isHavingUntrustedCertificateForIntranet();

    boolean isHavingUntrustedCertificateForInternet();
}