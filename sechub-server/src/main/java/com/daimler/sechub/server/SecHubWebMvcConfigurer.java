package com.daimler.sechub.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecHubWebMvcConfigurer implements WebMvcConfigurer{  

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(new SecHubServerMDCAsyncHandlerInterceptor());
    }

}
