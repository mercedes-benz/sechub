// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Configuration
@Profile(Profiles.OAUTH2)
class JwtDecoderConfiguration {

    @Bean
    JwtDecoder jwtDecoder(OAuth2Properties oAuth2Properties) {
        return NimbusJwtDecoder.withJwkSetUri(oAuth2Properties.getJwkSetUri()).build();
    }
}
