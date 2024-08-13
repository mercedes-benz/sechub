// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ScheduleShutdownService implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleShutdownService.class);

    private ApplicationContext context;

    public void shutdownApplication() {
        if (context instanceof ConfigurableApplicationContext) {
            LOG.info("Will now trigger shutdown of application context");
            ((ConfigurableApplicationContext) context).close();
        } else {
            if (context == null) {
                LOG.error("Cannot shutdown application context because context null!");
            } else {
                LOG.error("Cannot shutdown application context because wrong context:" + context.getClass());
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;

    }
}