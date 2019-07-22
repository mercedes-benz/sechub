// SPDX-License-Identifier: MIT
package com.daimler.sechub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;

import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;

/**
 * At the moment this is the ONLY spring boot application used for sechub. So we
 * have internal communication between domains and have to deploy only ONE
 * application. In future there can be made a seperation into different
 * springapplications to become more "microservice" like.<br>
 * <br>
 * When doing separtion we need to change the behaviour / implementation of
 * {@link DomainMessageService}
 * 
 * @author Albert Tregnaghi
 *
 */
@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class) // we do not want to have automatic resources in HAL & co
public class SecHubServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecHubServerApplication.class, args);
	}

}
