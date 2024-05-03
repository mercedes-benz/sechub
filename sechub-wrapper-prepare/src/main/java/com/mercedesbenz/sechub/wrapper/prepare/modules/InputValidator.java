package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.util.List;

public interface InputValidator {

    boolean validateLocation(String location);

    void validateUsername(String username);

    void validatePassword(String password);

    void validateLocationCharacters(String url, List<String> forbiddenCharacters);

}
