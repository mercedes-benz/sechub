// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHasherTestApplication {

    public static void main(String[] args) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String input = args[0];
        String encoded = encoder.encode(input);
        System.out.println("given:" + input);
        System.out.println("encoded:" + encoded);
    }
}
