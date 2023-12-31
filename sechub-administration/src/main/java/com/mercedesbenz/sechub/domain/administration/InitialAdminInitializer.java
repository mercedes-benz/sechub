// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.administration.user.InternalInitialDataService;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
public class InitialAdminInitializer {

    @Value("${sechub.initialadmin.userid}")
    @MustBeDocumented(value = "Userid of initial administrator")
    String initialAdminUserid;

    @Value("${sechub.initialadmin.email}")
    @MustBeDocumented(value = "Mail of initial administrator")
    String initialAdminEmailAddress;

    @Value("${sechub.initialadmin.apitoken:}") // : so default is empty, making this optional.
    @MustBeDocumented(value = "An apitoken for initial admin, will only be used in DEV and INTEGRATIONTEST profiles and is optional!")
    String initialAdminApiToken;

    @Autowired
    APITokenGenerator apiTokenGenerator;

    @Bean
    @Order(500)
    @Profile(Profiles.INITIAL_ADMIN_STATIC) // used in INTEGRATIONTEST profile
    public CommandLineRunner initialIntegrationTestAdmin(InternalInitialDataService internalService) {
        return args -> {
            /*
             * we use {noop} variant for integration tests - why? Because {bcrypt} algorith
             * does slow down integration tests ! Integration tests do not contain any
             * production data and also run only on a non productive system, so in this case
             * the usage is okay, because automated integration tests run much faster.
             */
            internalService.createInitialAdmin(initialAdminUserid, initialAdminEmailAddress, "{noop}" + initialAdminApiToken);
            /*
             * an additional test user, has no rights initial. Only for integration tests,
             * so password here plain and not configurable
             */
            internalService.createInitialTestUser("int-test_onlyuser", "int-test_onlyuser@test.sechub.example.org", "{noop}int-test_onlyuser-pwd");
        };
    }

    @Bean
    @Order(500)
    @Profile({ Profiles.INITIAL_ADMIN_PREDEFINED }) // used in DEV profile
    public CommandLineRunner initialSecHubAdmDevelopmentOnly(InternalInitialDataService internalService) {
        return args -> {
            internalService.createInitialAdmin(initialAdminUserid, initialAdminEmailAddress, initialAdminApiToken);
        };
    }

    @Bean
    @Order(500)
    @Profile({ Profiles.INITIAL_ADMIN_CREATED }) // used inside PROD profile
    public CommandLineRunner initialSecHubAdm(InternalInitialDataService internalService) {
        return args -> {
            internalService.createInitialAdmin(initialAdminUserid, initialAdminEmailAddress, UUID.randomUUID().toString()); // uses SecureRandom
        };
    }

}
