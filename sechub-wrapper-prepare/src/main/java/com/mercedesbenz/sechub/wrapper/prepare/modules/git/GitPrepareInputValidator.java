// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

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
public class GitPrepareInputValidator implements PrepareInputValidator, LogSanitizerProvider, PrepareAcceptFilter {

    private static final String TYPE = "git";

    private static final String GIT_LOCATION_REGEX = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?$";
    private static final Pattern GIT_LOCATION_PATTERN = Pattern.compile(GIT_LOCATION_REGEX);

    private static final String GIT_USERNAME_REGEX = "^[a-zA-Z0-9-_\\d](?:[a-zA-Z0-9-_\\d]|(?=[a-zA-Z0-9-_\\d])){0,38}$";
    private static final Pattern GIT_USERNAME_PATTERN = Pattern.compile(GIT_USERNAME_REGEX);

    private static final String GIT_PASSWORD_REGEX = "^(gh[ps]_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9]{22}_[a-zA-Z0-9]{59})$";
    private static final Pattern GIT_PASSWORD_PATTERN = Pattern.compile(GIT_PASSWORD_REGEX);

    private PrepareInputValidationSupport support;

    @Autowired
    PDSLogSanitizer logSanitizer;

    public GitPrepareInputValidator() {
        /* @formatter:off */
        this.support = PrepareInputValidationSupport.builder().
                setType(TYPE).
                setLogSanitizerProvider(this).
                setLocationPattern(GIT_LOCATION_PATTERN).
                setUserNamePattern(GIT_USERNAME_PATTERN).
                setPasswordPattern(GIT_PASSWORD_PATTERN).
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
