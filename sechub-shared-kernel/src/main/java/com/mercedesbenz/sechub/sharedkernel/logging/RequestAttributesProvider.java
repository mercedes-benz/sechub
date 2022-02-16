// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.logging;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class RequestAttributesProvider {

    public RequestAttributes getRequestAttributes() {
        return RequestContextHolder.getRequestAttributes();
    }
}
