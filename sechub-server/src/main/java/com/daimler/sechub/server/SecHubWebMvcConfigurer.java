// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.daimler.sechub.sharedkernel.project.ProjectAccessLevelConverter;

@Configuration
public class SecHubWebMvcConfigurer implements WebMvcConfigurer{  

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(new SecHubServerMDCAsyncHandlerInterceptor());
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ProjectAccessLevelConverter());
    }

}
