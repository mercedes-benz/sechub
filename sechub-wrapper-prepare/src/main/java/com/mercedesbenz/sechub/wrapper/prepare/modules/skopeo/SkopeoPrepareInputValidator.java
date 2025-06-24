// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.LogSanitizerProvider;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareAcceptFilter;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareInputValidationSupport;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareInputValidator;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperInputValidatorException;

@Component
public class SkopeoPrepareInputValidator implements PrepareInputValidator, LogSanitizerProvider, PrepareAcceptFilter {

    public static final String TYPE = "docker";
    private static final String SKOPEO_LOCATION_REGEX = "((docker://|https://)?([a-zA-Z0-9-_.].[a-zA-Z0-9-_.]/)?[a-zA-Z0-9-_.]+(:[a-zA-Z0-9-_.]+)?(/)?)+(@sha256:[a-f0-9]{64})?";
    private static final Pattern SKOPEO_LOCATION_PATTERN = Pattern.compile(SKOPEO_LOCATION_REGEX);
    private static final String SKOPEO_USERNAME_REGEX = "^[a-zA-Z0-9-_\\d](?:[a-zA-Z0-9-_\\d]|(?=[a-zA-Z0-9-_\\d])){0,38}$";
    private static final Pattern SKOPEO_USERNAME_PATTERN = Pattern.compile(SKOPEO_USERNAME_REGEX);
    private static final String SKOPEO_PASSWORD_REGEX = "^[A-Za-z0-9-_./+=\\d]{1,120}$";
    private static final Pattern SKOPEO_PASSWORD_PATTERN = Pattern.compile(SKOPEO_PASSWORD_REGEX);

    PrepareInputValidationSupport support;

    @Autowired
    PDSLogSanitizer logSanitizer;

    public SkopeoPrepareInputValidator() {
        /* @formatter:off */
        this.support = PrepareInputValidationSupport.builder().
                setType(TYPE).
                setLogSanitizerProvider(this).
                setLocationPattern(SKOPEO_LOCATION_PATTERN).
                setUserNamePattern(SKOPEO_USERNAME_PATTERN).
                setPasswordPattern(SKOPEO_PASSWORD_PATTERN).
                build();
        /* @formatter:on */
    }

    @Override
    public void validate(PrepareWrapperContext context) throws PrepareWrapperInputValidatorException {
        support.validate(context);
    }

    @Override
    public PDSLogSanitizer getLogSanitizer() {
        return logSanitizer;
    }

    @Override
    public boolean isAccepting(PrepareWrapperContext context) {
        return support.isAccepting(context);
    }
}
