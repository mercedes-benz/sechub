// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.encryption.EncryptionSupport;
import com.mercedesbenz.sechub.commons.encryption.PersistentCipherFactory;

/**
 * This factory creates some "plain old java" objects and inject them into
 * spring boot container. These objects are from libraries where we do not have
 * spring annotations inside for automatic injection.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class ScheduleEncryptionPojoFactory {

    @Bean
    PersistentCipherFactory createPersistentCipherFactory() {
        return new PersistentCipherFactory();
    }

    @Bean
    EncryptionSupport createEncryptionSupport() {
        return new EncryptionSupport();
    }
}
