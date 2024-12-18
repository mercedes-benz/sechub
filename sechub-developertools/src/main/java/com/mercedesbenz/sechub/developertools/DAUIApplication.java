// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class) // we do not want to have automatic resources in HAL & co
public class DAUIApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(DAUIApplication.class);
        builder.headless(false);

        builder.run(args);
    }
}
