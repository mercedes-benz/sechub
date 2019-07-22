// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.daimler.sechub.sharedkernel.MustBeDocumented;

@ConditionalOnProperty(value = "sechub.config.scheduling.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
@MustBeDocumented("Scheduling can be turned off in tests by using condition!")
public class SchedulerConfig {

}
