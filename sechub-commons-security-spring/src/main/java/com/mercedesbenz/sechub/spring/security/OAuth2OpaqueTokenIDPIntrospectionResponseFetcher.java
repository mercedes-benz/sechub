package com.mercedesbenz.sechub.spring.security;

public interface OAuth2OpaqueTokenIDPIntrospectionResponseFetcher {

    OAuth2OpaqueTokenIntrospectionResponse fetchOpaqueTokenIntrospectionFromIDP(String opaqueToken);

}