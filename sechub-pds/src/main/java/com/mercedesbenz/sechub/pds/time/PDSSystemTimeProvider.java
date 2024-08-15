// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.time;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

/**
 * This component returns system time data. The reason why this is inside own
 * class: It's possible to mock system time inside unit tests! Just use the
 * component in your implementation you want to test and replace it with a mock.
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class PDSSystemTimeProvider {

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }
}
