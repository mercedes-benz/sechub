package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractInputValidator;

@Component
public class GitInputValidator extends AbstractInputValidator {

    private static final String TYPE = "git";
    private static final String GIT_LOCATION_REGEX = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?$";
    private static final Pattern GIT_LOCATION_PATTERN = Pattern.compile(GIT_LOCATION_REGEX);
    private static final String GIT_USERNAME_REGEX = "^[a-zA-Z0-9-_\\d](?:[a-zA-Z0-9-_\\d]|(?=[a-zA-Z0-9-_\\d])){0,38}$";
    private static final Pattern GIT_USERNAME_PATTERN = Pattern.compile(GIT_USERNAME_REGEX);
    private static final String GIT_PASSWORD_REGEX = "^(gh[ps]_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9]{22}_[a-zA-Z0-9]{59})$";
    private static final Pattern GIT_PASSWORD_PATTERN = Pattern.compile(GIT_PASSWORD_REGEX);

    public GitInputValidator() {
        super(TYPE, GIT_LOCATION_PATTERN, GIT_USERNAME_PATTERN, GIT_PASSWORD_PATTERN);
    }
}
