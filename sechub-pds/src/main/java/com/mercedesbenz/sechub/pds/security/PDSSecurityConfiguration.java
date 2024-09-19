// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.security;

import javax.crypto.SealedObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableWebSecurity
@Order(1)
public class PDSSecurityConfiguration {

    private static final String KEY_TECHUSER_USERID = "pds.techuser.userid";
    private static final String KEY_TECHUSER_APITOKEN = "pds.techuser.apitoken";

    private static final String KEY_ADMIN_USERID = "pds.admin.userid";
    private static final String KEY_ADMIN_APITOKEN = "pds.admin.apitoken";

    @PDSMustBeDocumented(value = "Techuser user id.", scope = "credentials.")
    @Value("${" + KEY_TECHUSER_USERID + "}")
    String techUserId;

    @PDSMustBeDocumented(value = "Techuser user api token.", scope = "credentials")
    @Value("${" + KEY_TECHUSER_APITOKEN + "}")
    String techUserApiToken;

    @PDSMustBeDocumented(value = "Administrator user id.", scope = "credentials")
    @Value("${" + KEY_ADMIN_USERID + "}")
    String adminUserId;

    @PDSMustBeDocumented(value = "Administrator api token.", scope = "credentials")
    @Value("${" + KEY_ADMIN_APITOKEN + "}")
    String adminApiToken;

    @Autowired
    private Environment springEnvironment;

    private SealedObject sealedTechUserApiToken;
    private SealedObject sealedAdminApiToken;

    /**
     * Creates an initialized PDS security configuration (can be used outside spring
     * container to create such an object)
     *
     * @param techUserId       technical user id
     * @param techUserApiToken technical user token
     * @param adminUserId      administrator id
     * @param adminApiToken    administrator token
     *
     * @return configuration object
     */
    public static PDSSecurityConfiguration create(String techUserId, String techUserApiToken, String adminUserId, String adminApiToken) {
        PDSSecurityConfiguration config = new PDSSecurityConfiguration();
        config.techUserId = techUserId;
        config.techUserApiToken = techUserApiToken;

        config.adminUserId = adminUserId;
        config.adminApiToken = adminApiToken;

        config.initUserDetails();

        return config;
    }

    @Bean
    public UserDetailsManager initUserDetails() {
        /* @formatter:off */

        PDSPasswordTransformer pdsPasswordTransformer = new PDSPasswordTransformer();

        UserDetails user =
                User.builder()
                        .username(techUserId)
                        .password(pdsPasswordTransformer.transformPassword(techUserApiToken))
                        .roles(PDSRoles.USER.getRole())
                        .build();

        sealedTechUserApiToken = CryptoAccess.CRYPTO_STRING.seal(techUserApiToken);

        /* remove unsecured field after start */
        techUserApiToken = null;

        UserDetails admin =
                User.builder()
                        .username(adminUserId)
                        .password(pdsPasswordTransformer.transformPassword(adminApiToken))
                        .roles(PDSRoles.SUPERADMIN.getRole())
                        .build();

        sealedAdminApiToken = CryptoAccess.CRYPTO_STRING.seal(adminApiToken);

        /* remove unsecured field after start */
        adminApiToken = null;

        /* @formatter:on */
        return new InMemoryUserDetailsManager(user, admin);
    }

    public void registerOnlyAllowedAsEnvironmentVariables(SecureEnvironmentVariableKeyValueRegistry registry) {
        if (springEnvironment != null && springEnvironment.matchesProfiles(PDSProfiles.INTEGRATIONTEST)) {
            /*
             * on integration test we accept credentials from configuration file or as
             * system properties - not marked as sensitive
             */
            return;
        }
        registry.register(registry.newEntry().key(KEY_TECHUSER_USERID).notNullValue(techUserId));
        registry.register(registry.newEntry().key(KEY_TECHUSER_APITOKEN).notNullValue(CryptoAccess.CRYPTO_STRING.unseal(sealedTechUserApiToken)));

        registry.register(registry.newEntry().key(KEY_ADMIN_USERID).notNullValue(adminUserId));
        registry.register(registry.newEntry().key(KEY_ADMIN_APITOKEN).notNullValue(CryptoAccess.CRYPTO_STRING.unseal(sealedAdminApiToken)));

    }

}