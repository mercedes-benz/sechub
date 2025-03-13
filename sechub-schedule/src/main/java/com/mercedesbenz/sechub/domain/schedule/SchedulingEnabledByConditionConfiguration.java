// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;

@ConditionalOnProperty(value = "sechub.config.scheduling.enable", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
@MustBeDocumented(value = "Scheduling can be turned off in tests by using this condition! If any value is contained inside this system property, no scheduling will be done. WARNING: For integration tests this property may NOT be set!", scope = SCOPE_JOB)
public class SchedulingEnabledByConditionConfiguration {

}
