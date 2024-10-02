// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(SecurityConfiguration.class)
public class SecurityTestConfiguration {
}
