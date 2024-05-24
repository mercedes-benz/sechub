package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractInputValidator;

@Component
public class SkopeoInputValidator extends AbstractInputValidator {

    public static final String TYPE = "docker";
    private static final String SKOPEO_LOCATION_REGEX = "((docker://|https://)?([a-zA-Z0-9-_.].[a-zA-Z0-9-_.]/)?[a-zA-Z0-9-_.]+(:[a-zA-Z0-9-_.]+)?(/)?)+(@sha256:[a-f0-9]{64})?";
    private static final Pattern SKOPEO_LOCATION_PATTERN = Pattern.compile(SKOPEO_LOCATION_REGEX);
    private static final String SKOPEO_USERNAME_REGEX = "^[a-zA-Z0-9-_\\d](?:[a-zA-Z0-9-_\\d]|(?=[a-zA-Z0-9-_\\d])){0,38}$";
    private static final Pattern SKOPEO_USERNAME_PATTERN = Pattern.compile(SKOPEO_USERNAME_REGEX);
    private static final String SKOPEO_PASSWORD_REGEX = "^[a-zA-Z0-9-_\\d]{0,80}$";
    private static final Pattern SKOPEO_PASSWORD_PATTERN = Pattern.compile(SKOPEO_PASSWORD_REGEX);

    public SkopeoInputValidator() {
        super(TYPE, SKOPEO_LOCATION_PATTERN, SKOPEO_USERNAME_PATTERN, SKOPEO_PASSWORD_PATTERN);
    }

}
