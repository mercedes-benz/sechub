package com.mercedesbenz.sechub.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.adapter.mock.NullMockDataIdentifierFactory;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile("!" + Profiles.MOCKED_PRODUCTS)
public class NotMockedProductsOnlyFactory {

    @Bean
    MockDataIdentifierFactory createMockDataIdentifierFactory() {
        return new NullMockDataIdentifierFactory();
    }
}
