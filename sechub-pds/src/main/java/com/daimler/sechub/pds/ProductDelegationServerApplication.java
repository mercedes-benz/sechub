package com.daimler.sechub.pds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class) // we do not want to have automatic resources in HAL & co
public class ProductDelegationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductDelegationServerApplication.class, args);
    }

}
